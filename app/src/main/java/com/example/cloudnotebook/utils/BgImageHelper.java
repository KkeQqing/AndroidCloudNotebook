package com.example.cloudnotebook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BgImageHelper {
    private static final String BG_FILE_NAME = "app_bg.png";

    public static void saveBackgroundImage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            File file = new File(context.getFilesDir(), BG_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBackgroundImage(Context context) {
        try {
            File file = new File(context.getFilesDir(), BG_FILE_NAME);
            if (!file.exists()) return null;
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    // 清除背景
    public static void clear(Context context) {
        File file = new File(context.getFilesDir(), BG_FILE_NAME);
        if (file.exists()) file.delete();
    }
}