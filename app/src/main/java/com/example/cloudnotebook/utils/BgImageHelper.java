package com.example.cloudnotebook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 应用背景图片工具类
 * 功能：
 * 1. 保存用户选择的图片作为全局背景
 * 2. 获取已保存的背景图片
 * 3. 清除背景图片
 * 存储位置：应用私有目录（files 文件夹），安全且无需权限
 */
public class BgImageHelper {
    // 背景图片固定文件名
    private static final String BG_FILE_NAME = "app_bg.png";

    /**
     * 保存背景图片到应用私有目录
     * @param context 上下文
     * @param uri 从相册选择的图片 Uri 地址
     */
    public static void saveBackgroundImage(Context context, Uri uri) {
        try {
            // 1. 通过 Uri 打开输入流，读取图片
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            // 2. 将流解析为 Bitmap 对象
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 3. 在应用私有 files 目录创建文件
            File file = new File(context.getFilesDir(), BG_FILE_NAME);
            // 4. 创建文件输出流
            FileOutputStream fos = new FileOutputStream(file);
            // 5. 压缩图片并写入文件（PNG格式，质量80%）
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);

            // 6. 关闭流，释放资源
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            // 捕获异常，防止崩溃
            e.printStackTrace();
        }
    }

    /**
     * 获取保存的背景图片
     * @param context 上下文
     * @return 背景 Bitmap，若无则返回 null
     */
    public static Bitmap getBackgroundImage(Context context) {
        try {
            // 1. 获取背景图片文件
            File file = new File(context.getFilesDir(), BG_FILE_NAME);
            // 2. 文件不存在则返回空
            if (!file.exists()) return null;
            // 3. 文件存在，解析为 Bitmap 返回
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            // 出错返回空
            return null;
        }
    }

    /**
     * 清除背景图片（删除文件）
     * @param context 上下文
     */
    public static void clear(Context context) {
        File file = new File(context.getFilesDir(), BG_FILE_NAME);
        // 文件存在则删除
        if (file.exists()) file.delete();
    }
}