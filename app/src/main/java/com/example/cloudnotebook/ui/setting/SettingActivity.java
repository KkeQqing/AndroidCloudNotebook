package com.example.cloudnotebook.ui.setting;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.cloudnotebook.R;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivitySettingBinding;
import com.example.cloudnotebook.utils.BgImageHelper;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

import cn.bmob.v3.BmobUser;

/**
 * 设置页面
 * 功能：
 * 1. 更换APP主题（5套主题）
 * 2. 设置自定义背景图片
 * 3. 清除缓存
 * 4. 退出登录（Bmob云端退出）
 * 5. 显示版本号
 */
public class SettingActivity extends BaseActivity {
    // 视图绑定对象
    private ActivitySettingBinding binding;

    // 常量：打开相册请求码
    private static final int REQUEST_IMAGE = 1001;

    // 常量：权限申请请求码
    private static final int PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化视图绑定
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ====================== 主题颜色适配 ======================
        // 设置所有卡片背景色
        binding.cardClear.setCardBackgroundColor(themeCardColor);
        binding.cardLogout.setCardBackgroundColor(themeCardColor);
        binding.cardBg.setCardBackgroundColor(themeCardColor);
        binding.cardTheme.setCardBackgroundColor(themeCardColor);

        // 设置按钮文字颜色
        binding.btnChangeBg.setTextColor(themeMainColor);
        binding.btnChangeTheme.setTextColor(themeMainColor);

        // 显示当前版本号
        binding.tvVersion.setText("版本: 1.0.0");

        // ====================== 清除缓存按钮 ======================
        binding.btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        });

        // ====================== 退出登录 ======================
        binding.btnLogout.setOnClickListener(v -> {
            // 清空本地保存的用户信息
            new SharedPrefsHelper(this).clear();
            // Bmob云端退出登录
            BmobUser.logOut();
            // 跳转到登录页并关闭当前页
            jumpActivityFinish(com.example.cloudnotebook.ui.login.LoginActivity.class);
        });

        // ====================== 更换背景 ======================
        binding.btnChangeBg.setOnClickListener(v -> {
            // Android 13+ 动态申请读取图片权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
                    return;
                }
            }

            // 打开系统相册选择图片
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        // ====================== 切换主题 ======================
        binding.btnChangeTheme.setOnClickListener(v -> {
            // 主题选项
            String[] themes = {
                    "默认白色",
                    "清新蓝",
                    "活力橙",
                    "森林绿",
                    "优雅紫"
            };

            // 弹出主题选择对话框
            new AlertDialog.Builder(this)
                    .setTitle("选择主题")
                    .setItems(themes, (dialog, which) -> {
                        // 保存用户选择的主题
                        new SharedPrefsHelper(this).setTheme(which);
                        Toast.makeText(this, "切换成功，重启生效", Toast.LENGTH_SHORT).show();

                        // 重启APP使主题生效
                        Intent intent = new Intent(this, com.example.cloudnotebook.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .show();
        });
    }

    // ====================== 相册选择图片返回结果 ======================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 判断是选择图片的请求 + 结果OK + data不为空
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            // 获取选中图片的Uri
            Uri imageUri = data.getData();
            // 保存到本地，作为全局背景
            BgImageHelper.saveBackgroundImage(this, imageUri);
            Toast.makeText(this, "背景设置成功！重启APP生效", Toast.LENGTH_SHORT).show();

            // 重启主页，背景生效
            Intent intent = new Intent(this, com.example.cloudnotebook.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    // ====================== 权限申请结果回调 ======================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            // 用户允许权限 → 打开相册
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            } else {
                // 拒绝权限 → 提示
                Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}