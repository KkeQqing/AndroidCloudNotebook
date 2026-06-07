package com.example.cloudnotebook.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityRegisterBinding;
import com.example.cloudnotebook.viewmodel.LoginViewModel;

/**
 * 用户注册页面
 * 功能：
 * 1. 用户名 + 密码注册
 * 2. 两次密码输入一致性校验
 * 3. 输入框非空校验
 * 4. 注册成功自动返回登录页
 * 5. 错误信息 Toast 提示
 */
public class RegisterActivity extends BaseActivity {

    // ViewBinding 视图绑定，替代 findViewById
    private ActivityRegisterBinding binding;

    // ViewModel 负责注册、登录的业务逻辑
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化视图绑定
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取 ViewModel 实例
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // ======================
        // 返回登录文字点击
        // 关闭当前页面，回到登录页
        // ======================
        binding.tvBackLogin.setOnClickListener(v -> finish());

        // ======================
        // 注册按钮点击事件
        // 1. 获取输入内容
        // 2. 非空校验
        // 3. 两次密码一致性校验
        // 4. 调用 ViewModel 执行注册
        // ======================
        binding.btnRegister.setOnClickListener(v -> {
            // 获取输入框内容并去除空格
            String user = binding.etUsername.getText().toString().trim();
            String pwd = binding.etPassword.getText().toString().trim();
            String pwdConfirm = binding.etPasswordConfirm.getText().toString().trim();

            // 校验：输入框不能为空
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(pwdConfirm)) {
                Toast.makeText(this, "请完整填写信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 校验：两次输入的密码必须一致
            if (!pwd.equals(pwdConfirm)) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 校验通过，调用注册方法
            viewModel.register(user, pwd);
        });

        // ======================
        // 监听注册结果（错误/成功信息）
        // 如果提示包含“注册成功”，则关闭当前页面返回登录
        // ======================
        viewModel.errorMsg.observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            // 注册成功 → 自动返回登录页面
            if (msg.contains("注册成功")) {
                finish();
            }
        });
    }
}