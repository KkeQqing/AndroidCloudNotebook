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
 * Repository 层（数据仓库层）
 * 作用：统一管理本地数据（Room）和云端数据（Bmob）
 * 外部（ViewModel/Activity）只需要调用仓库，不用关心数据从哪来
 */
public class NoteRepository {
    // 1. Room 数据库操作接口（增删改查都靠它）
    private NoteDao noteDao;

    // 2. 线程池：3条后台线程，执行耗时操作（数据库/网络）
    // 防止在主线程操作数据库导致卡顿或崩溃
    private ExecutorService executor = Executors.newFixedThreadPool(3);

    // 3. 当前登录用户的 ID，用于只查询该用户的笔记
    private String userId;

    /**
     * 构造方法
     * @param context 上下文（用来获取数据库实例）
     * @param userId 当前登录用户ID
     */
    public NoteRepository(Context context, String userId){
        // 获取数据库单例，然后拿到 Dao 操作对象
        noteDao = AppDatabase.getInstance(context).noteDao();
        // 把用户ID保存起来，所有查询都带上，实现多用户隔离
        this.userId = userId;
    }

    /**
     * 获取当前用户的所有笔记
     * @return LiveData 可观察数据，数据变化时自动通知页面更新
     */
    public LiveData<List<Note>> getAllNotes(){
        return noteDao.getAllNotes(userId);
    }

    /**
     * 根据分类查询笔记（工作、学习、生活等）
     * @param category 分类名称
     * @return 该分类下的所有笔记
     */
    public LiveData<List<Note>> getNotesByCategory(String category){
        return noteDao.getNotesByCategory(userId, category);
    }

    /**
     * 搜索笔记（根据标题/内容模糊查询）
     * @param query 搜索关键词
     * @return 匹配的笔记列表
     */
    public LiveData<List<Note>> searchNotes(String query){
        return noteDao.searchNotes(userId, query);
    }

    /**
     * 【本地插入笔记】
     * 作用：只把笔记保存到本地 Room 数据库，不立即上传云端
     * @param note 要插入的笔记对象
     * @param callback 操作完成的回调（通知页面插入成功）
     */
    public void insertLocal(Note note, OnLocalOperationCallback callback){
        // 让数据库操作在 子线程 执行，不卡UI
        executor.execute(()->{
            // 给笔记设置当前用户ID，保证数据属于当前登录用户
            note.setUserId(userId);

            // 调用 Room 的 DAO 执行插入操作
            noteDao.insert(note);

            // 如果页面传了回调，通知页面：插入成功
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 【本地更新笔记】
     * 作用：更新本地笔记，并且标记为“未同步”，下次联网再上传
     * @param callback 操作完成回调
     */
    public void updateLocal(Note originalNote, OnLocalOperationCallback callback){
        executor.execute(()->{
            // ---------------- 关键修复：克隆新对象，不修改外部对象 ----------------
            Note note = new Note();
            note.setLocalId(originalNote.getLocalId());
            note.setServerId(originalNote.getServerId());
            note.setTitle(originalNote.getTitle());
            note.setContent(originalNote.getContent());
            note.setCategory(originalNote.getCategory());
            note.setUserId(originalNote.getUserId());
            note.setCreateTime(originalNote.getCreateTime());
            note.setDeleted(originalNote.isDeleted());
            // -------------------------------------------------------------------

            note.setUpdateTime(System.currentTimeMillis());
            note.setSync(false);

            noteDao.update(note);
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 【本地软删除笔记】
     * 作用：不是真删除，而是把 isDeleted = true，方便云端同步
     * @param ids 要删除的本地笔记 id 集合
     * @param callback 操作完成回调
     */
    public void softDeleteNotes(List<Integer> ids, OnLocalOperationCallback callback){
        executor.execute(()->{
            // 把 List<Integer> 转换成 int[] （因为 Room 接收数组）
            int[] idArray = ids.stream().mapToInt(i->i).toArray();

            // 执行软删除：将 isDeleted = true，并更新删除时间
            noteDao.softDeleteNotes(System.currentTimeMillis(), idArray);

            // 通知页面删除成功
            if(callback != null) callback.onSuccess();
        });
    }

    /**
     * 上传笔记到云端（Bmob）
     * 自动判断：没有serverId → 新增；有serverId → 更新
     *
     * @param note     要上传的本地笔记
     * @param callback 云端操作回调（成功/失败）
     */
    public void uploadNote(Note note, OnCloudCallback callback) {
        // 1. 把本地的 Note 对象 → 转换成 Bmob 能识别的云端对象
        cn.bmob.v3.BmobObject bmobNote = convertToBmob(note);

        // 2. 判断：本地笔记是否【从未同步过云端】
        // serverId 为空 → 代表云端还没有这条数据 → 执行新增
        if (note.getServerId() == null || note.getServerId().isEmpty()) {

            // 云端新增数据
            bmobNote.save(new SaveListener<String>() {
                @Override
                public void done(String objectId, BmobException e) {
                    if (e == null) {
                        // 云端新增成功！
                        // 把 Bmob 返回的 唯一ID(objectId) 保存到本地 note 的 serverId 字段
                        note.setServerId(objectId);

                        // 标记本地笔记：已同步云端
                        note.setSync(true);

                        // 把更新后的 serverId 和 sync 状态 保存回本地Room
                        executor.execute(() -> noteDao.update(note));

                        // 通知外部：上传成功
                        if (callback != null) callback.onSuccess();
                    } else {
                        // 上传失败 → 通知外部失败原因
                        if (callback != null) callback.onError(e.getMessage());
                    }
                }
            });

        } else {
            // 3. 有 serverId → 代表云端已有这条数据 → 执行【更新】
            bmobNote.update(note.getServerId(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        // 云端更新成功
                        // 标记本地笔记：已同步
                        note.setSync(true);

                        // 保存同步状态到Room
                        executor.execute(() -> noteDao.update(note));

                        // 通知外部成功
                        if (callback != null) callback.onSuccess();
                    } else {
                        // 更新失败
                        if (callback != null) callback.onError(e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * 本地 Note → 转换为 Bmob 云端对象
     * 作用：把 Room 实体类的字段 一一对应 赋值给 Bmob 表字段
     */
    private cn.bmob.v3.BmobObject convertToBmob(Note note) {
        // 指定要上传到 Bmob 的表名：Note
        cn.bmob.v3.BmobObject obj = new cn.bmob.v3.BmobObject("Note");

        // 一一赋值，字段名必须和 Bmob 后台表字段完全一致
        obj.setValue("userId", note.getUserId());
        obj.setValue("title", note.getTitle());
        obj.setValue("content", note.getContent());
        obj.setValue("category", note.getCategory());
        obj.setValue("createTime", note.getCreateTime());
        obj.setValue("updateTime", note.getUpdateTime());
        obj.setValue("isDeleted", note.isDeleted());

        // 返回转换好的云端对象
        return obj;
    }

    /**
     * 批量同步【本地未同步】的笔记到云端
     * 作用：一次性把所有 isSync = false 的笔记全部上传到 Bmob
     * @param callback 同步完成的回调（通知页面同步结束）
     */
    public void syncAllUnsyncedNotes(OnCloudCallback callback) {
        // 1. 在子线程中执行（数据库查询 + 循环上传，都是耗时操作）
        executor.execute(() -> {
            // 2. 查询本地数据库：获取【当前用户所有未同步】的笔记（isSync = false）
            List<Note> unsynced = noteDao.getUnsyncedNotes(userId);

            // 3. 循环遍历每一条未同步笔记，逐个上传到云端
            for (Note n : unsynced) {
                // 这里传 null：不需要单条笔记的上传回调，只要整体批量完成即可
                uploadNote(n, null);
            }

            // 4. 全部上传任务发起后，通知外部：批量同步完成
            if (callback != null) callback.onSuccess();
        });
    }

    /**
     * 从 Bmob 云端拉取当前用户的所有笔记，并同步到本地 Room 数据库
     * 同步策略（非常关键）：
     * 1. 本地没有这条笔记 → 直接插入
     * 2. 本地有，但云端更新时间更晚 → 用云端覆盖本地
     * 3. 本地更新时间更晚 → 不覆盖（保留本地最新数据）
     *
     * @param context  上下文
     * @param callback 云端拉取操作的回调（成功/失败）
     */
    public void pullNotesFromCloud(Context context, OnCloudCallback callback) {
        // 1. 创建Bmob查询对象，指定查询的是【BmobNote】（云端实体类）
        BmobQuery<BmobNote> query = new BmobQuery<>();

        // 2. 设置查询条件：只拉取【当前登录用户】的笔记
        query.addWhereEqualTo("userId", userId);

        // 3. 执行云端网络请求，获取数据
        query.findObjects(new FindListener<BmobNote>() {
            @Override
            public void done(List<BmobNote> list, BmobException e) {
                // 请求结果返回
                if (e == null) {
                    // ======================
                    // 4. 云端请求成功
                    // ======================
                    // 开启子线程，操作本地数据库（防止卡顿UI）
                    executor.execute(() -> {

                        // 5. 遍历所有从云端获取到的笔记
                        for (BmobNote obj : list) {

                            // 6. 将【云端BmobNote对象】 → 转换为【本地Room Note对象】
                            Note cloudNote = convertToEntity(obj);

                            // 7. 根据【云端ID(serverId)】查询本地数据库是否已有这条数据
                            Note local = noteDao.getNoteByServerId(cloudNote.getServerId());

                            if (local == null) {
                                // ======================
                                // 情况1：本地没有这条数据 → 直接插入本地数据库
                                // ======================
                                cloudNote.setUserId(userId);
                                cloudNote.setSync(true);  // 标记为已同步
                                noteDao.insert(cloudNote);

                            } else if (cloudNote.getUpdateTime() > local.getUpdateTime()) {
                                // ======================
                                // 情况2：本地有，但云端数据更新（时间更大）→ 覆盖本地
                                // ======================
                                local.setTitle(cloudNote.getTitle());
                                local.setContent(cloudNote.getContent());
                                local.setCategory(cloudNote.getCategory());
                                local.setUpdateTime(cloudNote.getUpdateTime());
                                local.setSync(true);
                                noteDao.update(local);
                            }

                            // 情况3：云端数据更旧 → 不做任何操作，保留本地数据
                        }

                        // 8. 全部同步完成 → 通知外部（页面/ViewModel）
                        if (callback != null) callback.onSuccess();

                    });

                } else {
                    // ======================
                    // 9. 云端请求失败（网络错误/权限不足）
                    // ======================
                    if (callback != null) callback.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * 【云端对象 → 本地实体】转换器
     * 作用：把 Bmob 云端查询到的 BmobNote，转换为 Room 能识别的本地 Note
     *
     * @param obj 云端返回的 BmobNote 对象
     * @return 本地 Room 可用的 Note 对象
     */
    private Note convertToEntity(BmobNote obj) {
        // 1. 创建一个空的本地 Note 对象
        Note note = new Note();

        // 2. 设置云端返回的唯一ID（用于后续更新/删除）
        note.setServerId(obj.getObjectId());

        // 3. 把云端的字段一一赋值给本地 Note
        note.setUserId(obj.getUserId());
        note.setTitle(obj.getTitle());
        note.setContent(obj.getContent());
        note.setCategory(obj.getCategory());
        note.setCreateTime(obj.getCreateTime());
        note.setUpdateTime(obj.getUpdateTime());
        note.setDeleted(obj.getDeleted());

        // 4. 这条数据来自云端 → 标记为【已同步】
        note.setSync(true);

        // 5. 返回转换好的本地对象
        return note;
    }

    /**
     * 本地数据库操作回调接口
     * 作用：监听 Room 数据库【增、删、改】操作的完成状态
     * 子线程执行完数据库操作后，通过此接口通知主线程（页面/ViewModel）操作成功
     */
    public interface OnLocalOperationCallback {

        /**
         * 数据库操作成功的回调方法
         * 当 插入/更新/软删除 执行完毕后，会调用此方法通知外部
         */
        void onSuccess();
    }
    /**
     * 云端操作回调接口
     * 专门用于监听 Bmob 云端数据操作（上传、更新、删除、拉取）的结果
     * 告诉页面/ViewModel：云端操作是成功了，还是失败了
     */
    public interface OnCloudCallback {

        /**
         * 云端操作成功回调
         * 例如：上传笔记成功、更新云端数据成功
         */
        void onSuccess();

        /**
         * 云端操作失败回调
         * @param error 失败的错误信息（如：网络异常、上传失败、权限不足等）
         */
        void onError(String error);
    }

}
