package com.example.cloudnotebook.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import com.example.cloudnotebook.MainActivity;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityLoginBinding;
import com.example.cloudnotebook.viewmodel.LoginViewModel;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // ======================
        // 修复 1：方法名统一
        // ======================
        if (viewModel.isAlreadyLogin()) {
            jumpActivityFinish(MainActivity.class);
            return;
        }

        // 自动填充账号密码
        String lastUser = viewModel.getLastUser();
        String lastPwd = viewModel.getLastPwd();
        if (!TextUtils.isEmpty(lastUser)) {
            binding.etUsername.setText(lastUser);
        }
        if (!TextUtils.isEmpty(lastPwd)) {
            binding.etPassword.setText(lastPwd);
        }

        // 登录
        binding.btnLogin.setOnClickListener(v -> {
            String[] userPwd = getUserAndPwd();
            String user = userPwd[0];
            String pwd = userPwd[1];
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(user, pwd);
        });

        // 注册
        binding.tvRegister.setOnClickListener(v -> {
            String[] userPwd = getUserAndPwd();
            String user = userPwd[0];
            String pwd = userPwd[1];
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.register(user, pwd);
        });

        // 登录成功
        viewModel.loginSuccess.observe(this, success -> {
            if (success) {
                jumpActivityFinish(MainActivity.class);
            }
        });

        // ======================
        // 修复 2：名字统一
        // ======================
        viewModel.errorMsg.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    private String[] getUserAndPwd() {
        String user = binding.etUsername.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        return new String[]{user, pwd};
    }
}