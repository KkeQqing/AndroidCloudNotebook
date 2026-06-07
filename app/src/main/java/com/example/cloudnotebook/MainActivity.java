package com.example.cloudnotebook;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityMainBinding;
import com.example.cloudnotebook.ui.category.CategoryFragment;
import com.example.cloudnotebook.ui.home.HomeFragment;
import com.example.cloudnotebook.ui.setting.SettingActivity;
import com.example.cloudnotebook.worker.SyncWorker;

import java.util.concurrent.TimeUnit;

/**
 * 主页面（主页）
 * 功能：
 * 1. 底部导航栏切换 首页 / 分类 / 设置
 * 2. 加载对应 Fragment 内容
 * 3. 主题颜色适配底部导航
 * 4. 启动后台定时同步任务（WorkManager）
 */
public class MainActivity extends BaseActivity {

    // ViewBinding 视图绑定
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化视图绑定
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ====================== 主题颜色适配 ======================
        // 底部导航图标 + 文字颜色跟随全局主题
        binding.bottomNavigation.setItemIconTintList(ColorStateList.valueOf(themeMainColor));
        binding.bottomNavigation.setItemTextColor(ColorStateList.valueOf(themeMainColor));

        // ====================== 启动后台定时同步 ======================
        // 每15分钟自动同步一次笔记到云端
        startSyncWorkManager();

        // ====================== 默认显示首页 Fragment ======================
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // ====================== 底部导航切换监听 ======================
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            // 首页
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            }
            // 分类页
            else if (id == R.id.nav_category) {
                fragment = new CategoryFragment();
            }
            // 设置页（直接跳转 Activity，不使用 Fragment）
            else if (id == R.id.nav_setting) {
                jumpActivity(SettingActivity.class);
                return false;
            }

            // 切换到选中的 Fragment
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            return true;
        });
    }

    /**
     * 启动 WorkManager 后台定时同步任务
     * 作用：每隔 15 分钟，在有网络时自动同步笔记到云端
     */
    private void startSyncWorkManager() {
        // 约束条件：仅在有网络时执行
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 构建定时任务：每隔15分钟执行一次 SyncWorker
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        // 加入任务队列
        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}