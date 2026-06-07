package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cloudnotebook.repository.NoteRepository;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.utils.SharedPrefsHelper;
import java.util.List;

/**
 * 笔记业务 ViewModel
 * 职责：连接 UI 页面与数据仓库 Repository
 * 页面只调用 ViewModel 方法，不直接操作数据库/云端
 */
public class NoteViewModel extends AndroidViewModel {

    // 数据仓库：统一管理本地 Room + 云端 Bmob
    private NoteRepository repository;

    // 同步状态：通知页面云同步成功/失败
    private MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();

    /**
     * 构造方法
     * 1. 获取当前登录用户 ID
     * 2. 初始化数据仓库
     */
    public NoteViewModel(Application application) {
        super(application);
        // 获取本地保存的当前用户ID
        String userId = new SharedPrefsHelper(application).getUserId();
        // 创建仓库实例
        repository = new NoteRepository(application, userId);
    }

    // ====================== 查询操作 ======================

    /**
     * 获取当前用户所有笔记（未删除）
     */
    public LiveData<List<Note>> getAllNotes() {
        return repository.getAllNotes();
    }

    /**
     * 根据分类查询笔记
     */
    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    /**
     * 搜索笔记（标题 + 内容模糊查询）
     */
    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    // ====================== 增删改操作 ======================

    /**
     * 插入笔记
     * @param afterInsert 插入成功后执行的回调
     */
    public void insert(Note note, Runnable afterInsert) {
        repository.insertLocal(note, () -> {
            if (afterInsert != null) afterInsert.run();
        });
    }

    /**
     * 更新笔记
     */
    public void update(Note note) {
        repository.updateLocal(note, null);
    }

    /**
     * 软删除笔记（批量）
     */
    public void softDelete(List<Integer> ids) {
        repository.softDeleteNotes(ids, null);
    }

    // ====================== 云端同步 ======================

    /**
     * 上传单条笔记到云端
     */
    public void uploadNote(Note note) {
        repository.uploadNote(note, new NoteRepository.OnCloudCallback() {
            @Override
            public void onSuccess() {
                syncStatus.postValue(true);
            }

            @Override
            public void onError(String error) {
                syncStatus.postValue(false);
            }
        });
    }

    /**
     * 批量同步所有未同步笔记
     */
    public void syncAllUnsyncedNotes() {
        repository.syncAllUnsyncedNotes(null);
    }

    /**
     * 从云端拉取笔记覆盖本地
     */
    public void pullFromCloud() {
        repository.pullNotesFromCloud(getApplication(), new NoteRepository.OnCloudCallback() {
            @Override
            public void onSuccess() {
                syncStatus.postValue(true);
            }

            @Override
            public void onError(String error) {
                syncStatus.postValue(false);
            }
        });
    }

    /**
     * 获取同步状态，供页面监听提示
     */
    public MutableLiveData<Boolean> getSyncStatus() {
        return syncStatus;
    }
}