package com.joydeep.solar.calculator;

import android.app.Application;

import com.joydeep.solar.calculator.realm.RealmSingleton;

import io.realm.Realm;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Realm
        Realm.init(this);
        RealmSingleton.getInstance();
    }
}
