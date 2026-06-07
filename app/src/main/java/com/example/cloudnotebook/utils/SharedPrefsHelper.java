package com.example.cloudnotebook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private static final String PREF_NAME = "notepad_prefs";
    private SharedPreferences prefs;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 保存用户ID
    public void saveUserId(String userId) {
        prefs.edit().putString("userId", userId).apply();
    }

    public String getUserId() {
        return prefs.getString("userId", null);
    }

    // 保存登录状态
    public void saveLoginState(boolean state) {
        prefs.edit().putBoolean("isLogin", state).apply();
    }

    public boolean isLogin() {
        return prefs.getBoolean("isLogin", false);
    }

    // ==============================================
    // 保存 账号
    // ==============================================
    public void saveUsername(String username) {
        prefs.edit().putString("username", username).apply();
    }

    public String getUsername() {
        return prefs.getString("username", "");
    }

    // ==============================================
    // 保存 密码
    // ==============================================
    public void savePassword(String password) {
        prefs.edit().putString("password", password).apply();
    }

    public String getPassword() {
        return prefs.getString("password", "");
    }

    // 清空（退出登录时调用）
    public void clear() {
        prefs.edit().clear().apply();
    }
}