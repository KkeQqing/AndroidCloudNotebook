package com.example.cloudnotebook.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cloudnotebook.R;
import com.example.cloudnotebook.utils.BgImageHelper;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

/**
 * 项目基础 Activity 父类（抽象类）
 * 作用：所有 Activity 都继承此类，统一实现：主题切换、背景设置、页面跳转
 * 特点：子类无需重复编写通用逻辑，实现代码复用
 */
public abstract class BaseActivity extends AppCompatActivity {

    // 主题卡片背景色（供子类/适配器使用）
    public int themeCardColor;
    // 主题主色调（供子类/适配器使用）
    public int themeMainColor;

    /**
     * Activity 创建时执行
     * 顺序：先加载主题 → 再执行父类创建 → 最后应用背景
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 加载用户保存的主题配置
        loadAppTheme();
        // 调用父类 onCreate
        super.onCreate(savedInstanceState);
        // 应用全局背景图片 + 遮罩
        applyAppBackground();
    }

    /**
     * 从 SharedPreferences 读取主题配置，并给主题颜色赋值
     * 共 5 套主题：默认 + 4 套自定义主题
     */
    private void loadAppTheme() {
        // 获取本地存储工具类
        SharedPrefsHelper sp = new SharedPrefsHelper(this);
        // 获取用户保存的主题编号（1-4，默认0）
        int theme = sp.getTheme();

        // 根据主题编号设置颜色
        switch (theme) {
            case 1:
                themeCardColor = getColor(R.color.theme_1_card);
                themeMainColor = getColor(R.color.theme_1_main);
                break;
            case 2:
                themeCardColor = getColor(R.color.theme_2_card);
                themeMainColor = getColor(R.color.theme_2_main);
                break;
            case 3:
                themeCardColor = getColor(R.color.theme_3_card);
                themeMainColor = getColor(R.color.theme_3_main);
                break;
            case 4:
                themeCardColor = getColor(R.color.theme_4_card);
                themeMainColor = getColor(R.color.theme_4_main);
                break;
            default:
                // 默认主题
                themeCardColor = getColor(R.color.theme_0_card);
                themeMainColor = getColor(R.color.theme_0_main);
                break;
        }
    }

    /**
     * 应用全局背景图片
     * 逻辑：设置背景图 + 添加半透明白色遮罩（让文字更清晰）
     */
    private void applyAppBackground() {
        try {
            // 从工具类获取已设置的背景位图
            Bitmap bitmap = BgImageHelper.getBackgroundImage(this);
            if (bitmap != null) {
                // 设置窗口背景为图片
                getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));

                // 创建半透明遮罩层（让文字不被背景干扰）
                View mask = new View(this);
                mask.setBackgroundColor(0xCCFFFFFF); // 白色+80%透明度

                // 遮罩铺满屏幕
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                // 将遮罩添加到界面最上层
                addContentView(mask, params);
            }
        } catch (Exception e) {
            // 捕获图片加载异常，防止崩溃
            e.printStackTrace();
        }
    }

    // ===================== 页面跳转工具方法（子类直接调用） =====================

    /**
     * 跳转到指定 Activity（不关闭当前页）
     */
    protected void jumpActivity(Class<?> clazz) {
        startActivity(new Intent(this, clazz));
    }

    /**
     * 跳转到指定 Activity，并关闭当前 Activity
     */
    protected void jumpActivityFinish(Class<?> clazz) {
        jumpActivity(clazz);
        finish();
    }

    /**
     * 带数据（Bundle）跳转到指定 Activity
     */
    protected void jumpActivityWithData(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}