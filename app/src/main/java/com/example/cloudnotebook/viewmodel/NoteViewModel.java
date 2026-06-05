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
 * 笔记页面 ViewModel
 * 作用：连接页面与数据仓库，处理所有笔记业务逻辑
 */
public class NoteViewModel extends AndroidViewModel {
    // 数据仓库（本地+云端）
    private NoteRepository repository;
    // 同步状态：true=成功，false=失败
    private MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();

    // 构造：初始化仓库，获取当前登录用户ID
    public NoteViewModel(Application application){
        super(application);
        String userId = new SharedPrefsHelper(application).getUserId();
        repository = new NoteRepository(application, userId);
    }

    // 获取所有笔记
    public LiveData<List<Note>> getAllNotes(){
        return repository.getAllNotes();
    }

    // 按分类获取笔记
    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    // 搜索笔记
    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    // 插入笔记到本地
    public void insert(Note note) {
        repository.insertLocal(note, null);
    }

    // 更新本地笔记
    public void update(Note note) {
        repository.updateLocal(note, null);
    }

    // 软删除笔记
    public void softDelete(List<Integer> ids) {
        repository.softDeleteNotes(ids, null);
    }

    // 上传单条笔记到云端
    public void uploadNote(Note note) {
        repository.uploadNote(note, new NoteRepository.OnCloudCallback() {
            @Override public void onSuccess() { syncStatus.postValue(true); }
            @Override public void onError(String error) { syncStatus.postValue(false); }
        });
    }

    // 批量同步本地未上传笔记
    public void syncAllUnsyncedNotes() {
        repository.syncAllUnsyncedNotes(null);
    }

    // 从云端拉取笔记到本地
    public void pullFromCloud() {
        repository.pullNotesFromCloud(getApplication(), new NoteRepository.OnCloudCallback() {
            @Override public void onSuccess() { syncStatus.postValue(true); }
            @Override public void onError(String error) { syncStatus.postValue(false); }
        });
    }

    // 获取同步状态
    public MutableLiveData<Boolean> getSyncStatus() { return syncStatus; }
}