package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.cloudnotebook.repository.NoteRepository;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

/**
 * 同步专用ViewModel
 * 作用：统一处理云端同步逻辑（上传本地未同步笔记）
 */
public class SyncViewModel extends AndroidViewModel {

    // 数据仓库
    private NoteRepository repository;

    // 构造方法：获取用户ID，初始化仓库
    public SyncViewModel(Application application){
        super(application);
        String userId = new SharedPrefsHelper(application).getUserId();
        // 用户ID不为空才初始化仓库
        if(userId != null) {
            repository = new NoteRepository(application, userId);
        }
    }

    // 执行同步：把本地未上传的笔记全部推送到云端
    public void performSync(){
        if(repository != null) {
            repository.syncAllUnsyncedNotes(null);
        }
    }
}