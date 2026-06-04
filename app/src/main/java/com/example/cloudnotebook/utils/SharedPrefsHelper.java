package com.example.cloudnotebook.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类
 * 作用：轻量级数据存储，用来保存用户登录状态、用户ID等简单数据
 * 特点：数据持久化，App 关闭再打开依然存在
 */
public class SharedPrefsHelper {
    // 存储文件的名称（所有数据存在这个文件里）
    private static final String PREF_NAME = "notepad_prefs";

    // Android 系统提供的轻量级存储对象
    private SharedPreferences prefs;

    /**
     * 构造方法：初始化 SharedPreferences
     * @param context 上下文
     */
    public SharedPrefsHelper(Context context) {
        // 创建/获取名为 notepad_prefs 的私有存储文件
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存用户ID（登录后存起来）
     * @param userId 用户唯一标识
     */
    public void saveUserId(String userId) {
        prefs.edit().putString("userId", userId).apply();
    }

    /**
     * 获取保存的用户ID
     * @return 返回 userId，没有则返回 null
     */
    public String getUserId() {
        return prefs.getString("userId", null);
    }

    /**
     * 保存登录状态
     * @param state true=已登录，false=未登录
     */
    public void saveLoginState(boolean state) {
        prefs.edit().putBoolean("isLogin", state).apply();
    }

    /**
     * 获取当前登录状态
     * @return true=已登录，false=未登录
     */
    public boolean isLogin() {
        return prefs.getBoolean("isLogin", false);
    }

    /**
     * 清空所有存储数据（一般用于退出登录）
     */
    public void clear() {
        prefs.edit().clear().apply();
    }
}