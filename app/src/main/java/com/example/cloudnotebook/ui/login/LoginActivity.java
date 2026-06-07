package com.example.cloudnotebook.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.cloudnotebook.MainActivity;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityLoginBinding;
import com.example.cloudnotebook.viewmodel.LoginViewModel;

/**
 * 登录页面
 * 功能：
 * 1. 用户账号密码登录
 * 2. 自动记住上次账号密码
 * 3. 已登录状态自动跳过登录，直接进主页
 * 4. 点击跳转到注册页面
 * 5. 登录结果监听与提示
 */
public class LoginActivity extends BaseActivity {

    // ViewBinding 视图绑定，替代 findViewById
    private ActivityLoginBinding binding;

    // 登录业务逻辑 ViewModel
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化视图绑定
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取 ViewModel 实例
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // ======================
        // 自动登录判断
        // 如果用户已登录，直接跳转到主页，不再显示登录页
        // ======================
        if (viewModel.isAlreadyLogin()) {
            jumpActivityFinish(MainActivity.class);
            return;
        }

        // ======================
        // 自动填充上次登录的账号、密码
        // 提升用户体验，不用重复输入
        // ======================
        String lastUser = viewModel.getLastUser();
        String lastPwd = viewModel.getLastPwd();
        if (!TextUtils.isEmpty(lastUser)) {
            binding.etUsername.setText(lastUser);
        }
        if (!TextUtils.isEmpty(lastPwd)) {
            binding.etPassword.setText(lastPwd);
        }

        // ======================
        // 登录按钮点击事件
        // 1. 获取输入框账号密码
        // 2. 非空校验
        // 3. 调用 ViewModel 执行登录
        // ======================
        binding.btnLogin.setOnClickListener(v -> {
            String[] userPwd = getUserAndPwd();
            String user = userPwd[0];
            String pwd = userPwd[1];

            // 校验账号密码不能为空
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 调用登录逻辑
            viewModel.login(user, pwd);
        });

        // ======================
        // 注册文字点击事件
        // 跳转到注册页面
        // ======================
        binding.tvRegister.setOnClickListener(v -> {
            jumpActivity(RegisterActivity.class);
        });

        // ======================
        // 监听登录结果
        // 登录成功 → 跳转到主页
        // ======================
        viewModel.loginSuccess.observe(this, success -> {
            if (success) {
                jumpActivityFinish(MainActivity.class);
            }
        });

        // ======================
        // 监听错误信息
        // 登录失败 → 弹出 Toast 提示
        // ======================
        viewModel.errorMsg.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * 工具方法：获取输入框中的账号和密码
     * @return String[] 数组，[0]=账号，[1]=密码
     */
    private String[] getUserAndPwd() {
        String user = binding.etUsername.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        return new String[]{user, pwd};
    }
}