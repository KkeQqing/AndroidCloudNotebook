package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.cloudnotebook.utils.SharedPrefsHelper;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginViewModel extends AndroidViewModel {
    private SharedPrefsHelper sp;
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public MutableLiveData<String> errorMsg = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application app) {
        super(app);
        sp = new SharedPrefsHelper(app);
    }

    public boolean isAlreadyLogin() {
        return sp.isLogin();
    }

    public String getLastUser() {
        return sp.getUsername();
    }

    public String getLastPwd() {
        return sp.getPassword();
    }

    // ✅ 真实 Bmob 登录
    public void login(String username, String pwd) {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(pwd);

        user.login(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    // 登录成功 → 保存状态
                    sp.saveLoginState(true);
                    sp.saveUserId(bmobUser.getObjectId());
                    sp.saveUsername(username);
                    sp.savePassword(pwd);

                    loginSuccess.postValue(true);
                } else {
                    // 错误：账号密码错误、网络等
                    if (e.getErrorCode() == 101) {
                        errorMsg.postValue("账号或密码错误");
                    } else {
                        errorMsg.postValue("登录失败：" + e.getMessage());
                    }
                }
            }
        });
    }

    // ✅ 注册
    public void register(String username, String pwd) {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(pwd);

        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    errorMsg.postValue("注册成功，请登录");
                } else {
                    errorMsg.postValue("注册失败：" + e.getMessage());
                }
            }
        });
    }
}