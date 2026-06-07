package com.example.cloudnotebook.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 本地轻量级存储工具类
 * 作用：持久化保存用户信息、登录状态、主题设置
 * 特点：数据存在本地，APP关闭后不会丢失
 */
public class SharedPrefsHelper {

    // 本地存储文件名（所有配置存在这个文件里）
    private static final String PREF_NAME = "notepad_prefs";

    // SharedPreferences 实例
    private SharedPreferences prefs;

    /**
     * 构造方法：初始化存储对象
     * MODE_PRIVATE：只有本APP可以访问，安全
     */
    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ====================== 用户ID ======================
    /**
     * 保存登录用户的唯一ID（用于云同步）
     */
    public void saveUserId(String userId) {
        prefs.edit().putString("userId", userId).apply();
    }

    /**
     * 获取当前登录用户ID
     */
    public String getUserId() {
        return prefs.getString("userId", null);
    }

    // ====================== 登录状态 ======================
    /**
     * 保存是否已登录
     */
    public void saveLoginState(boolean state) {
        prefs.edit().putBoolean("isLogin", state).apply();
    }

    /**
     * 获取当前登录状态
     */
    public boolean isLogin() {
        return prefs.getBoolean("isLogin", false);
    }

    // ====================== 记住账号 ======================
    /**
     * 保存用户名（用于自动填充）
     */
    public void saveUsername(String username) {
        prefs.edit().putString("username", username).apply();
    }

    /**
     * 获取保存的用户名
     */
    public String getUsername() {
        return prefs.getString("username", "");
    }

    // ====================== 记住密码 ======================
    /**
     * 保存密码（用于自动填充）
     */
    public void savePassword(String password) {
        prefs.edit().putString("password", password).apply();
    }

    /**
     * 获取保存的密码
     */
    public String getPassword() {
        return prefs.getString("password", "");
    }

    // ====================== 主题设置 ======================
    /**
     * 保存主题编号（0~4）
     */
    public void setTheme(int id) {
        prefs.edit().putInt("app_theme", id).apply();
    }

    /**
     * 获取当前主题，默认0（默认白色）
     */
    public int getTheme() {
        return prefs.getInt("app_theme", 0);
    }

    // ====================== 清空所有数据 ======================
    /**
     * 退出登录时清空所有本地存储
     */
    public void clear() {
        prefs.edit().clear().apply();
    }
}