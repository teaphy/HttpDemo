package com.teaphy.okhttptest;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * @autor Teaphy
 * Created at 2016/6/22.
 */
public class TeaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger
            .init("Teaphy")      // default tag : PRETTYLOGGER or use just init()
            .hideThreadInfo();   // default it is shown
    }
}
