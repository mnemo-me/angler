package com.mnemo.angler;

import android.app.Application;

import com.mnemo.angler.data.RepositoryModule;


public class AnglerApp extends Application {

    private static AnglerComponent anglerComponent;

    public static AnglerComponent getAnglerComponent() {
        return anglerComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        anglerComponent = DaggerAnglerComponent.builder().repositoryModule(new RepositoryModule(this)).build();
    }
}
