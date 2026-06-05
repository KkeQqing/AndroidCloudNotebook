package com.example.cloudnotebook.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.cloudnotebook.utils.SharedPrefsHelper;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.LogInListener;

/**
 * 登录注册页面专属ViewModel
 * 职责：处理账号登录、账号注册、自动登录校验；通过LiveData异步通知页面登录/注册结果，
 * 登录成功后使用SP持久化保存用户ID与登录状态
 */
public class LoginViewModel extends AndroidViewModel {
    /**
     * 登录/注册成功信号：true=操作成功，页面收到后跳转首页
     * MutableLiveData：支持代码主动修改值，数据变化自动回调UI观察者
     */
    public MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    /**
     * 错误提示信息载体：登录、注册异常时存放失败文案，由页面弹窗展示
     */
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * SP本地存储工具类：持久化存储用户ID、登录状态标识，用于自动登录判断
     */
    private SharedPrefsHelper prefsHelper;

    /**
     * ViewModel构造方法
     * @param application 全局Application上下文，生命周期长于Activity，避免内存泄漏
     */
    public LoginViewModel(Application application){
        super(application);
        // 初始化本地存储工具
        prefsHelper = new SharedPrefsHelper(application);
    }

    /**
     * 账号密码登录接口
     * @param username 登录用户名
     * @param password 登录密码
     */
    public void login(String username, String password) {
        // Bmob登录使用LogInListener监听回调，区别于注册的SaveListener
        BmobUser.loginByAccount(username, password, new LogInListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    // 登录成功：保存云端用户唯一id、本地登录标记
                    prefsHelper.saveUserId(user.getObjectId());
                    prefsHelper.saveLoginState(true);
                    // postValue：子线程中更新LiveData，通知页面跳转
                    loginSuccess.postValue(true);
                } else {
                    // 登录失败：拼接错误信息+错误码，推送至页面提示
                    errorMessage.postValue("登录失败：" + e.getMessage() + ", 错误码：" + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 用户账号注册
     * @param username 注册用户名
     * @param password 注册密码
     */
    public void register(String username, String password){
        // 实例化Bmob用户对象，赋值账号密码
        BmobUser newUser = new BmobUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        // signUp注册接口使用SaveListener接收注册结果
        newUser.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e){
                if(e == null){
                    // 注册成功，保存用户信息与登录状态
                    prefsHelper.saveUserId(user.getObjectId());
                    prefsHelper.saveLoginState(true);
                    loginSuccess.postValue(true);
                }else {
                    // 注册异常，返回错误信息
                    errorMessage.postValue("注册失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 校验是否可以自动登录
     * @return true：SP标记已登录 && Bmob缓存登录用户有效，直接跳转首页；false：需要手动登录
     */
    public boolean isAutoLogin() {
        // 双重校验：本地存储登录标识 + Bmob当前用户登录状态
        return prefsHelper.isLogin() && BmobUser.isLogin();
    }

}