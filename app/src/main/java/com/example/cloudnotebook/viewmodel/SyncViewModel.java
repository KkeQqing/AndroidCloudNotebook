package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.cloudnotebook.repository.NoteRepository;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

/**
 * 云同步专用 ViewModel
 * 职责：专门处理【本地 → 云端】的数据同步
 * 独立出来，让同步逻辑更单一、更清晰
 */
public class SyncViewModel extends AndroidViewModel {

    // 数据仓库：执行实际同步操作
    private NoteRepository repository;

    /**
     * 构造方法
     * 1. 获取当前登录用户的 userId
     * 2. 初始化 NoteRepository（数据仓库）
     */
    public SyncViewModel(Application application){
        super(application);

        // 从本地存储获取当前登录用户ID
        String userId = new SharedPrefsHelper(application).getUserId();

        // 只有用户已登录（userId不为空），才创建仓库实例
        if(userId != null) {
            repository = new NoteRepository(application, userId);
        }
    }

    /**
     * 执行同步操作
     * 作用：将本地所有【未同步】的笔记（isSync = false）一次性上传到 Bmob 云端
     */
    public void performSync(){
        // 防止空指针：仓库不为空才执行同步
        if(repository != null) {
            repository.syncAllUnsyncedNotes(null);
        }
    }
}