package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cloudnotebook.repository.NoteRepository;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.utils.SharedPrefsHelper;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository repository;
    private MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();

    public NoteViewModel(Application application){
        super(application);
        String userId = new SharedPrefsHelper(application).getUserId();
        repository = new NoteRepository(application, userId);
    }

    // 获取所有笔记
    public LiveData<List<Note>> getAllNotes(){
        return repository.getAllNotes();
    }

    // 按分类查询
    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    // ======================
    // ✅ 搜索功能（修复）
    // ======================
    public LiveData<List<Note>> searchNotes(String query){
        return repository.searchNotes(query);
    }

    // 插入笔记
    public void insert(Note note, Runnable afterInsert) {
        repository.insertLocal(note, () -> {
            if (afterInsert != null) afterInsert.run();
        });
    }

    // 更新笔记
    public void update(Note note) {
        repository.updateLocal(note, null);
    }

    // 删除
    public void softDelete(List<Integer> ids) {
        repository.softDeleteNotes(ids, null);
    }

    // 上传单条
    public void uploadNote(Note note) {
        repository.uploadNote(note, new NoteRepository.OnCloudCallback() {
            @Override public void onSuccess() { syncStatus.postValue(true); }
            @Override public void onError(String error) { syncStatus.postValue(false); }
        });
    }

    // 批量同步
    public void syncAllUnsyncedNotes() {
        repository.syncAllUnsyncedNotes(null);
    }

    // 拉取云端
    public void pullFromCloud() {
        repository.pullNotesFromCloud(getApplication(), new NoteRepository.OnCloudCallback() {
            @Override public void onSuccess() { syncStatus.postValue(true); }
            @Override public void onError(String error) { syncStatus.postValue(false); }
        });
    }

    public MutableLiveData<Boolean> getSyncStatus() { return syncStatus; }
}