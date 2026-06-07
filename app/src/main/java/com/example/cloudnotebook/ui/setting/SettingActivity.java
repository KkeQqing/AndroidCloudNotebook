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

public class SettingActivity extends BaseActivity {
    private ActivitySettingBinding binding;
    private static final int REQUEST_IMAGE = 1001;
    private static final int PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置卡片颜色
        binding.cardClear.setCardBackgroundColor(themeCardColor);
        binding.cardLogout.setCardBackgroundColor(themeCardColor);
        binding.cardBg.setCardBackgroundColor(themeCardColor);
        binding.cardTheme.setCardBackgroundColor(themeCardColor);

        binding.btnChangeBg.setTextColor(themeMainColor);
        binding.btnChangeTheme.setTextColor(themeMainColor);

        binding.tvVersion.setText("版本: 1.0.0");

        binding.btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new SharedPrefsHelper(this).clear();
            BmobUser.logOut();
            jumpActivityFinish(com.example.cloudnotebook.ui.login.LoginActivity.class);
        });

        binding.btnChangeBg.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
                    return;
                }
            }

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        // ======================
        // 主题切换
        // ======================
        binding.btnChangeTheme.setOnClickListener(v -> {
            String[] themes = {
                    "默认白色",
                    "清新蓝",
                    "活力橙",
                    "森林绿",
                    "优雅紫"
            };

            new AlertDialog.Builder(this)
                    .setTitle("选择主题")
                    .setItems(themes, (dialog, which) -> {
                        new SharedPrefsHelper(this).setTheme(which);
                        Toast.makeText(this, "切换成功，重启生效", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, com.example.cloudnotebook.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            BgImageHelper.saveBackgroundImage(this, imageUri);
            Toast.makeText(this, "背景设置成功！重启APP生效", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, com.example.cloudnotebook.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            } else {
                Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}