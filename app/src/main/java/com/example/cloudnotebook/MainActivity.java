package com.example.cloudnotebook;

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
 * 主页面
 * 作用：底部导航栏 + 切换 首页、分类、设置三个页面
 * 继承 BaseActivity ：使用通用的跳转、基础功能
 */
public class MainActivity extends BaseActivity {

    // 视图绑定：自动关联 activity_main.xml 里的所有控件
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 调用父类初始化

        // 1. 加载主页面布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ======================
        // 启动 后台定时同步（每15分钟）
        // ======================
        startSyncWorkManager();

        // 2. APP一打开，默认显示【首页】
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()) // 替换容器为首页
                .commit();

        // 3. 底部导航栏点击切换监听
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null; // 声明要切换的页面
            int id = item.getItemId(); // 获取当前点击的按钮ID

            // 判断点击的是哪个按钮
            if (id == R.id.nav_home) {
                // 点击首页 → 切换到首页Fragment
                fragment = new HomeFragment();
            } else if (id == R.id.nav_category) {
                // 点击分类 → 切换到分类Fragment
                fragment = new CategoryFragment();
            } else if (id == R.id.nav_setting) {
                // 点击设置 → 跳转到设置Activity（使用BaseActivity的跳转方法）
                jumpActivity(SettingActivity.class);
                return false; // 返回false，表示不选中底部导航按钮
            }

            // 如果选中的是首页/分类，就替换页面
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

            return true; // 返回true，正常选中底部导航按钮
        });
    }

    // ======================
    // WorkManager 后台同步
    // ======================
    private void startSyncWorkManager() {
        // 同步条件：有网络时才同步
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 每 15 分钟同步一次（系统最小限制）
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        // 启动任务
        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}