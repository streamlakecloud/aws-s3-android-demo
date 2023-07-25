package com.kwai.upload.demo;

import android.app.Application;

/**
 * author: zhouzhihui
 * created on: 2023/7/13 11:19
 * description:
 */
public class AwsApp extends Application {
    public static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
}
