package com.example.cloudnotebook.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类
 * 作用：检查手机当前是否有可用网络（WiFi/流量）
 */
public class NetworkUtil {

    /**
     * 判断手机网络是否可用
     * @param context 上下文
     * @return true = 有网，false = 无网
     */
    public static boolean isNetworkAvailable(Context context) {
        // 获取系统的 网络连接管理器
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取当前正在使用的网络信息
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // 判断：网络不为空 且 正在连接/已连接 → 返回有网
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}