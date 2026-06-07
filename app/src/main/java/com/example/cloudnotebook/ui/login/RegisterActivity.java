package com.example.cloudnotebook.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityRegisterBinding;
import com.example.cloudnotebook.viewmodel.LoginViewModel;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 返回登录
        binding.tvBackLogin.setOnClickListener(v -> finish());

        // 注册按钮
        binding.btnRegister.setOnClickListener(v -> {
            String user = binding.etUsername.getText().toString().trim();
            String pwd = binding.etPassword.getText().toString().trim();
            String pwdConfirm = binding.etPasswordConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(pwdConfirm)) {
                Toast.makeText(this, "请完整填写信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pwd.equals(pwdConfirm)) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(user, pwd);
        });

        // 注册结果监听
        viewModel.errorMsg.observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (msg.contains("注册成功")) {
                finish(); // 返回登录
            }
        });
    }
}