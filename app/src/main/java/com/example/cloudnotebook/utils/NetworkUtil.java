package com.example.cloudnotebook.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络状态工具类
 * 功能：判断当前设备是否有可用网络（WiFi/移动数据）
 * 用于：云同步、上传笔记、拉取数据前的网络判断
 */
public class NetworkUtil {

    /**
     * 判断网络是否可用
     * @param context 上下文
     * @return true：网络连接正常   false：无网络
     */
    public static boolean isNetworkAvailable(Context context) {
        // 获取系统的网络连接管理器
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取当前激活的网络信息（正在使用的网络）
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // 判断：网络存在 并且 已经连接或正在连接中
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}