package com.example.cloudnotebook.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.cloudnotebook.room.dao.NoteDao;
import com.example.cloudnotebook.room.database.AppDatabase;
import com.example.cloudnotebook.room.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.cloudnotebook.network.BmobNote;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 笔记数据仓库层（Repository）
 * 架构职责：统一管理本地数据（Room） + 云端数据（Bmob）
 * 外部调用方（ViewModel/Activity）无需关心数据来源，只需调用仓库方法
 * 实现：本地离线可用 + 云端同步备份 + 多用户数据隔离
 */
public class NoteRepository {
    // Room数据库操作接口，负责本地笔记增删改查
    private NoteDao noteDao;

    // 固定3线程的线程池，用于执行数据库/网络等耗时操作，避免阻塞主线程导致卡顿
    private ExecutorService executor = Executors.newFixedThreadPool(3);

    // 当前登录用户ID，用于数据隔离，每个用户只能操作自己的笔记
    private String userId;

    /**
     * 构造方法
     * @param context 上下文，用于获取数据库实例
     * @param userId 当前登录用户ID
     */
    public NoteRepository(Context context, String userId){
        // 获取单例数据库，并拿到DAO操作对象
        noteDao = AppDatabase.getInstance(context).noteDao();
        this.userId = userId;
    }

    /**
     * 获取当前用户的所有笔记
     * @return LiveData可观察数据，数据变化时自动通知页面刷新
     */
    public LiveData<List<Note>> getAllNotes(){
        return noteDao.getAllNotes(userId);
    }

    /**
     * 根据分类筛选当前用户的笔记
     * @param category 分类名称（工作/学习/生活等）
     * @return 对应分类的笔记列表
     */
    public LiveData<List<Note>> getNotesByCategory(String category){
        return noteDao.getNotesByCategory(userId, category);
    }

    /**
     * 搜索笔记（标题+内容模糊查询）
     * @param query 搜索关键词
     * @return 匹配的笔记列表
     */
    public LiveData<List<Note>> searchNotes(String query){
        return noteDao.searchNotes(userId, query);
    }

    /**
     * 本地插入笔记（仅保存到Room，不同步云端）
     * @param note 待插入的笔记
     * @param callback 操作完成回调
     */
    public void insertLocal(Note note, OnLocalOperationCallback callback){
        executor.execute(()->{
            // 绑定当前用户ID，保证数据归属正确
            note.setUserId(userId);
            // 插入数据库并返回自增ID，回填给本地笔记对象
            long newLocalId = noteDao.insertAndReturnId(note);
            note.setLocalId((int) newLocalId);
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 本地更新笔记
     * 自动标记为未同步，等待联网后上传云端
     * @param note 待更新的笔记
     * @param callback 操作完成回调
     */
    public void updateLocal(Note note, OnLocalOperationCallback callback){
        executor.execute(()->{
            note.setUpdateTime(System.currentTimeMillis());
            note.setSync(false); // 标记需要同步到云端
            noteDao.update(note);
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 本地软删除笔记（不真实删除，仅标记已删除）
     * 便于云端同步删除状态
     * @param ids 要删除的笔记本地ID集合
     * @param callback 操作完成回调
     */
    public void softDeleteNotes(List<Integer> ids, OnLocalOperationCallback callback){
        executor.execute(()->{
            // 将List转为int数组，适配Room的参数要求
            int[] idArray = ids.stream().mapToInt(i->i).toArray();
            // 执行软删除：设置删除状态和删除时间
            noteDao.softDeleteNotes(System.currentTimeMillis(), idArray);
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 上传单条笔记到Bmob云端
     * 智能判断：无serverId=新增，有serverId=更新
     * 上传成功后自动更新本地同步状态和serverId
     * @param note 本地笔记
     * @param callback 云端操作回调
     */
    public void uploadNote(Note note, OnCloudCallback callback) {
        BmobObject bmobNote = convertToBmob(note);

        // 无serverId：执行新增操作
        if (note.getServerId() == null || note.getServerId().isEmpty()) {
            bmobNote.save(new SaveListener<String>() {
                @Override
                public void done(String objectId, BmobException e) {
                    if (e == null) {
                        // 子线程保存serverId和同步状态到本地数据库
                        executor.execute(() -> {
                            note.setServerId(objectId);
                            note.setSync(true);
                            noteDao.update(note);
                        });
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                }
            });
        } else {
            // 有serverId：执行更新操作
            bmobNote.update(note.getServerId(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        executor.execute(() -> {
                            note.setSync(true);
                            noteDao.update(note);
                        });
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 本地Note → 转换为Bmob云端对象
     * 字段一一对应，确保云端数据格式正确
     */
    private cn.bmob.v3.BmobObject convertToBmob(Note note) {
        cn.bmob.v3.BmobObject obj = new cn.bmob.v3.BmobObject("Note");
        obj.setValue("userId", note.getUserId());
        obj.setValue("title", note.getTitle());
        obj.setValue("content", note.getContent());
        obj.setValue("category", note.getCategory());
        obj.setValue("createTime", note.getCreateTime());
        obj.setValue("updateTime", note.getUpdateTime());
        obj.setValue("isDeleted", note.isDeleted());
        obj.setValue("localId", note.getLocalId());
        obj.setValue("serverId", note.getServerId());
        return obj;
    }

    /**
     * 批量同步本地未同步笔记到云端
     * 上传所有isSync=false的笔记
     * @param callback 同步完成回调
     */
    public void syncAllUnsyncedNotes(OnCloudCallback callback) {
        executor.execute(() -> {
            // 查询当前用户所有未同步笔记
            List<Note> unsynced = noteDao.getUnsyncedNotes(userId);
            // 循环上传
            for (Note n : unsynced) {
                uploadNote(n, null);
            }
            if (callback != null) callback.onSuccess();
        });
    }

    /**
     * 从Bmob云端拉取笔记并同步到本地Room
     * 同步策略：
     * 1. 本地无 → 直接插入
     * 2. 云端更新 → 覆盖本地
     * 3. 本地更新 → 保留本地
     * @param context 上下文
     * @param callback 拉取完成回调
     */
    public void pullNotesFromCloud(Context context, OnCloudCallback callback) {
        BmobQuery<BmobNote> query = new BmobQuery<>();
        // 只拉取当前用户的数据
        query.addWhereEqualTo("userId", userId);

        query.findObjects(new FindListener<BmobNote>() {
            @Override
            public void done(List<BmobNote> list, BmobException e) {
                if (e == null) {
                    executor.execute(() -> {
                        for (BmobNote obj : list) {
                            // 转换为本地实体类
                            Note cloudNote = convertToEntity(obj);
                            // 根据serverId查询本地是否存在
                            Note local = noteDao.getNoteByServerId(cloudNote.getServerId());

                            if (local == null) {
                                // 本地无数据：直接插入
                                cloudNote.setUserId(userId);
                                cloudNote.setSync(true);
                                noteDao.insert(cloudNote);
                            } else if (cloudNote.getUpdateTime() > local.getUpdateTime()) {
                                // 云端数据更新：覆盖本地
                                local.setTitle(cloudNote.getTitle());
                                local.setContent(cloudNote.getContent());
                                local.setCategory(cloudNote.getCategory());
                                local.setUpdateTime(cloudNote.getUpdateTime());
                                local.setSync(true);
                                noteDao.update(local);
                            }
                        }
                        if (callback != null) callback.onSuccess();
                    });
                } else {
                    if (callback != null) callback.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * Bmob云端对象 → 本地Room Note实体转换
     */
    private Note convertToEntity(BmobNote obj) {
        Note note = new Note();
        note.setServerId(obj.getObjectId()); // 云端唯一ID
        note.setUserId(obj.getUserId());
        note.setTitle(obj.getTitle());
        note.setContent(obj.getContent());
        note.setCategory(obj.getCategory());
        note.setCreateTime(obj.getCreateTime());
        note.setUpdateTime(obj.getUpdateTime());
        note.setDeleted(obj.getDeleted());
        note.setSync(true); // 标记为已同步
        return note;
    }

    /**
     * 本地数据库操作回调（增/删/改完成通知）
     */
    public interface OnLocalOperationCallback {
        void onSuccess();
    }

    /**
     * 云端操作回调（上传/拉取/更新 成功/失败通知）
     */
    public interface OnCloudCallback {
        void onSuccess();
        void onError(String error);
    }

}