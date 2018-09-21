package com.mnemo.angler.main_activity;


public interface MainActivityView {


    void setBackground(String backgroundImage, int opacity);
    void showDescription(String title, String artist, long durationMs);
    void setPlayPause(String playPauseState);
}
