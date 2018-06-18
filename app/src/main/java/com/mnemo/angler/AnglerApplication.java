package com.mnemo.angler;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;


public class AnglerApplication extends Application {

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        refWatcher = LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context){

        AnglerApplication anglerApplication = (AnglerApplication)context.getApplicationContext();
        return anglerApplication.refWatcher;
    }
}
