package com.example.cloudnotebook.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cloudnotebook.utils.BgImageHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyAppBackground();
    }

    private void applyAppBackground() {
        try {
            Bitmap bitmap = BgImageHelper.getBackgroundImage(this);
            if (bitmap != null) {
                getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));

                View mask = new View(this);
                mask.setBackgroundColor(0xCCFFFFFF);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                addContentView(mask, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void jumpActivity(Class<?> clazz) {
        startActivity(new Intent(this, clazz));
    }

    protected void jumpActivityFinish(Class<?> clazz) {
        jumpActivity(clazz);
        finish();
    }

    protected void jumpActivityWithData(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}