package com.example.cloudnotebook.ui.setting;

import android.os.Bundle;
import android.widget.Toast;

import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivitySettingBinding;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

import cn.bmob.v3.BmobUser;

/**
 * 设置页面
 * 功能：展示版本号、清除缓存、退出登录
 * 继承自 BaseActivity，使用 ViewBinding 绑定布局
 */
public class SettingActivity extends BaseActivity {
    // ViewBinding 对象，用于绑定 activity_setting.xml 布局中的所有控件
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 ViewBinding
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        // 设置布局视图
        setContentView(binding.getRoot());

        // 显示当前应用版本号
        binding.tvVersion.setText("版本: 1.0.0");

        // 清除缓存按钮点击事件（模拟实现）
        binding.btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        });

        // 退出登录按钮点击事件
        binding.btnLogout.setOnClickListener(v -> {
            // 1. 清空本地 SharedPreferences 存储的用户信息
            new SharedPrefsHelper(this).clear();

            // 2. Bmob后端云 退出登录
            BmobUser.logOut();

            // 3. 跳转到登录页面，并关闭当前所有页面（使用基类封装方法）
            jumpActivityFinish(com.example.cloudnotebook.ui.login.LoginActivity.class);
        });
    }
}