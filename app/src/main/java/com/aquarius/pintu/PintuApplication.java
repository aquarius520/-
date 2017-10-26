package com.aquarius.pintu;

import android.app.Application;
/**
 * Created by aquarius on 2017/10/25.
 */
public class PintuApplication  extends Application{

    private static PintuApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static PintuApplication getInstance() {
        return instance;
    }
}
