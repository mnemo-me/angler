package com.mnemo.angler.data.preferences;


import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class AnglerPreferences {

    Context context;
    private SharedPreferences appPreferences;
    private SharedPreferences equalizerPreferences;

    @Inject
    public AnglerPreferences(Context context) {
        this.context = context;
    }

    public void initializePreferences(){
        appPreferences = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE);
        equalizerPreferences = context.getSharedPreferences("equalizer_pref", Context.MODE_PRIVATE);
    }

    public void setBackgroundImage(String backgroundImage){
        appPreferences.edit().putString("background", backgroundImage).apply();
    }

    public String getBackgroundImage(){
        return appPreferences.getString("background", "R.drawable.back");
    }

    public void setBackgroundOpacity(int opacity){
        appPreferences.edit().putInt("overlay", opacity).apply();
    }

    public int getBackgroundOpacity(){
        return appPreferences.getInt("overlay", 203);
    }

    public void setMainPlaylist(String playlist){
        appPreferences.edit().putString("main_playlist", playlist).apply();
    }
}
