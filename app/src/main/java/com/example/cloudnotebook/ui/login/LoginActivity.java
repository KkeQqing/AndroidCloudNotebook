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
 * 登录页面：账号登录/账号注册、自动登录跳转首页
 */
public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //视图绑定
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //初始化ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        //自动登录判定
        if (viewModel.isAutoLogin()) {
            jumpActivityFinish(MainActivity.class);
            return;
        }

        //登录点击
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

        //注册点击
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

        //登录成功监听
        viewModel.loginSuccess.observe(this, success -> {
            if (success) {
                jumpActivityFinish(MainActivity.class);
            }
        });

        //错误提示监听
        viewModel.errorMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * 抽取：获取账号密码，返回数组[账号,密码]
     */
    private String[] getUserAndPwd() {
        String user = binding.etUsername.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        return new String[]{user, pwd};
    }
}