package com.example.cloudnotebook.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity通用基类
 * 封装页面通用方法：页面跳转、关闭页面、基础初始化
 * 所有业务Activity继承此类，减少重复代码
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 跳转新页面
     * @param clazz 目标Activity.class
     */
    protected void jumpActivity(Class<?> clazz){
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    /**
     * 跳转页面并关闭当前页
     */
    protected void jumpActivityFinish(Class<?> clazz){
        jumpActivity(clazz);
        finish();
    }

    /**
     * 携带数据跳转页面
     */
    protected void jumpActivityWithData(Class<?> clazz, Bundle bundle){
        Intent intent = new Intent(this,clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
