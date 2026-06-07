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

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 底部导航颜色跟随主题
        binding.bottomNavigation.setItemIconTintList(ColorStateList.valueOf(themeMainColor));
        binding.bottomNavigation.setItemTextColor(ColorStateList.valueOf(themeMainColor));

        startSyncWorkManager();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_category) {
                fragment = new CategoryFragment();
            } else if (id == R.id.nav_setting) {
                jumpActivity(SettingActivity.class);
                return false;
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            return true;
        });
    }

    private void startSyncWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}