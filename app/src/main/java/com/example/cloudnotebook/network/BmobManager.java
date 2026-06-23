package com.example.cloudnotebook.network;

import android.app.Application;
import cn.bmob.v3.Bmob;

/**
 * Bmob后端云全局初始化类
 * 继承Android原生Application，应用全局入口，程序启动最先执行此类
 * 作用：项目全局初始化Bmob SDK，后续所有增删改查云数据库前必须完成初始化，否则报错未初始化
 * 配置须知：需要在AndroidManifest.xml的<application>标签通过android:name注册本类，系统才会自动实例化
 */
public class BmobManager extends Application {

    /**
     * 应用进程创建时自动回调，整个APP生命周期仅执行1次，优先于所有Activity、Service创建
     * 适合第三方SDK全局初始化、全局配置、全局工具类初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * Bmob SDK初始化方法
         * 参数1：this = Application全局上下文Context，生命周期跟随整个App，不会出现内存泄漏
         * 参数2：Bmob后台创建应用获取的Application ID（唯一密钥），用来绑定云端数据库，区分项目
         * 规则：必须在所有Bmob数据操作代码执行前初始化，固定放在Application的onCreate中是最佳规范
         */
        Bmob.initialize(this, "6bc4c89d53be89f915ad3e6eac597ed3");

    }
}