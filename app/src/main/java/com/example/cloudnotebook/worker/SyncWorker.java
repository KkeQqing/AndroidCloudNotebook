package com.example.cloudnotebook.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.cloudnotebook.viewmodel.SyncViewModel;

/**
 * SyncWorker：后台定时同步任务
 * 作用：由 WorkManager 调度，在【后台线程】中执行笔记云同步
 * 场景：APP 启动后，每 15 分钟自动执行一次数据同步（本地 ↔ 云端）
 */
public class SyncWorker extends Worker {

    /**
     * 构造方法：系统自动调用
     * @param context 上下文（可获取Application、资源等）
     * @param params 任务参数（可传递配置）
     */
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * 后台任务执行的核心方法
     * 注意：
     * 1. 运行在【工作线程】，可以直接执行网络请求、数据库操作
     * 2. 不能更新 UI（因为不是主线程）
     * 3. 执行完成后必须返回 Result 结果
     */
    @NonNull
    @Override
    public Result doWork() {
        // ======================
        // 【核心逻辑】执行后台同步
        // ======================
        // 1. 获取全局 Application 上下文
        // 2. 创建 SyncViewModel，调用同步方法
        SyncViewModel viewModel = new SyncViewModel((android.app.Application) getApplicationContext());

        // 执行同步：本地笔记 → 上传云端 / 云端笔记 → 下载本地
        viewModel.performSync();

        // ======================
        // 返回任务执行结果
        // Result.success()：执行成功
        // Result.failure()：执行失败
        // Result.retry()：需要重试
        // ======================
        return Result.success();
    }
}