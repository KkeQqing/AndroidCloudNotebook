package com.example.cloudnotebook.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.cloudnotebook.utils.SharedPrefsHelper;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * 登录注册 ViewModel
 * 职责：处理用户登录、注册逻辑，连接 Bmob 云端
 * 页面只需要观察数据变化，无需关心网络/存储细节
 */
public class LoginViewModel extends AndroidViewModel {

    // 本地存储工具类：保存登录状态、用户ID、主题等
    private SharedPrefsHelper sp;

    // 登录成功状态：页面观察，成功则跳主页
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    // 错误信息：登录/注册失败时提示用户
    public MutableLiveData<String> errorMsg = new MutableLiveData<>();

    /**
     * 构造方法
     * @param app 应用上下文，用于获取工具类
     */
    public LoginViewModel(@NonNull Application app) {
        super(app);
        sp = new SharedPrefsHelper(app);
    }

    /**
     * 判断用户是否已经登录
     * @return 已登录返回 true
     */
    public boolean isAlreadyLogin() {
        return sp.isLogin();
    }

    /**
     * 获取上次登录的账号（用于自动填充）
     */
    public String getLastUser() {
        return sp.getUsername();
    }

    /**
     * 获取上次登录的密码（用于自动填充）
     */
    public String getLastPwd() {
        return sp.getPassword();
    }

    // ====================== Bmob 登录 ======================
    /**
     * 执行登录请求（调用 Bmob SDK）
     * @param username 账号
     * @param pwd 密码
     */
    public void login(String username, String pwd) {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(pwd);

        // 发起登录
        user.login(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    // 登录成功
                    // 保存登录状态、用户ID、账号密码
                    sp.saveLoginState(true);
                    sp.saveUserId(bmobUser.getObjectId());
                    sp.saveUsername(username);
                    sp.savePassword(pwd);

                    // 通知页面登录成功
                    loginSuccess.postValue(true);
                } else {
                    // 登录失败
                    if (e.getErrorCode() == 101) {
                        // 101 = 账号或密码错误
                        errorMsg.postValue("账号或密码错误");
                    } else {
                        errorMsg.postValue("登录失败：" + e.getMessage());
                    }
                }
            }
        });
    }

    // ====================== Bmob 注册 ======================
    /**
     * 执行注册请求
     * @param username 账号
     * @param pwd 密码
     */
    public void register(String username, String pwd) {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(pwd);

        // 发起注册
        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    // 注册成功
                    errorMsg.postValue("注册成功，请登录");
                } else {
                    // 注册失败（如账号已存在）
                    errorMsg.postValue("注册失败：" + e.getMessage());
                }
            }
        });
    }
}