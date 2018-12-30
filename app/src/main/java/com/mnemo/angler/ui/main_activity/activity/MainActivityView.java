package com.mnemo.angler.ui.main_activity.activity;


import com.mnemo.angler.ui.base.BaseView;


interface MainActivityView extends BaseView{


    void setBackground(String backgroundImage, int opacity);
    void showDescription(String title, String artist, long durationMs);
    void setPlayPause(String playPauseState);
}
