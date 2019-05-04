package com.mnemo.angler.data.preferences;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class AnglerPreferences {

    private Context context;
    private SharedPreferences appPreferences;
    private SharedPreferences equalizerPreferences;
    private SharedPreferences queuePreferences;
    private SharedPreferences playbackPreferences;

    @Inject
    public AnglerPreferences(Context context) {
        this.context = context;
    }

    public void initializePreferences(){
        appPreferences = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE);
        equalizerPreferences = context.getSharedPreferences("equalizer_pref", Context.MODE_PRIVATE);
        queuePreferences = context.getSharedPreferences("queue_pref", Context.MODE_PRIVATE);
        playbackPreferences = context.getSharedPreferences("playback_pref", Context.MODE_PRIVATE);
    }


    public boolean getFirstLaunch(){
        return appPreferences.getBoolean("first_launch", true);
    }

    public void setFirstLaunch(boolean firstLaunch){
        appPreferences.edit().putBoolean("first_launch", firstLaunch).apply();
    }

    public long getTrialTimestamp(){
        return appPreferences.getLong("trial_timestamp", 0);
    }

    public void setTrialTimestamp(long timestamp){
        appPreferences.edit().putLong("trial_timestamp", timestamp).apply();
    }

    public void setBackgroundImage(String backgroundImage){
        appPreferences.edit().putString("background", backgroundImage).apply();
    }

    public String getBackgroundImage(){
        return appPreferences.getString("background", "/.default/" + "back1.jpeg");
    }

    public void setBackgroundOpacity(int opacity){
        appPreferences.edit().putInt("overlay", opacity).apply();
    }

    public int getBackgroundOpacity(){
        return appPreferences.getInt("overlay", 140);
    }

    public void setMainPlaylist(String playlist){
        appPreferences.edit().putString("main_playlist", playlist).apply();
    }


    // Equalizer methods
    public boolean getEqualizerState(){
        return equalizerPreferences.getBoolean("on_off_state", false);
    }

    public void setEqualizerState(boolean equalizerState){
        equalizerPreferences.edit().putBoolean("on_off_state", equalizerState).apply();
    }

    public int getEqualizerPreset(){
        return equalizerPreferences.getInt("active_preset", 0);
    }

    public void setEqualizerPreset(int preset){
        equalizerPreferences.edit().putInt("active_preset", preset).apply();
    }

    public List<Short> getBandsLevel(int bandsCount){

        List<Short> bandsLevel = new ArrayList<>();

        for (int i = 0; i < bandsCount; i++){
            bandsLevel.add((short)equalizerPreferences.getInt("band_" + i + "_level", 0));
        }

        return bandsLevel;
    }

    public void setBandsLevel(List<Short> bandsLevel){

        for (int i = 0; i < bandsLevel.size(); i++){
            equalizerPreferences.edit().putInt("band_" + i + "_level", bandsLevel.get(i)).apply();
        }
    }

    // Virtualizer
    public boolean getVirtualizerState(){
        return equalizerPreferences.getBoolean("virtualizer_on_off_state", false);
    }

    public void setVirtualizerState(boolean virtualizerState){
        equalizerPreferences.edit().putBoolean("virtualizer_on_off_state", virtualizerState).apply();
    }

    public int getVirtualizerStrength(){
        return equalizerPreferences.getInt("virtualizer_strength", 0);
    }

    public void setVirtualizerStrength(int virtualizerStrength){
        equalizerPreferences.edit().putInt("virtualizer_strength", virtualizerStrength).apply();
    }

    // Bass boost
    public boolean getBassBoostState(){
        return equalizerPreferences.getBoolean("bass_boost_on_off_state", false);
    }

    public void setBassBoostState(boolean bassBoostState){
        equalizerPreferences.edit().putBoolean("bass_boost_on_off_state", bassBoostState).apply();
    }

    public int getBassBoostStrength(){
        return equalizerPreferences.getInt("bass_boost_strength", 0);
    }

    public void setBassBoostStrength(int bassBoostStrength){
        equalizerPreferences.edit().putInt("bass_boost_strength", bassBoostStrength).apply();
    }

    // Amplifier
    public boolean getAmplifierState(){
        return equalizerPreferences.getBoolean("amplifier_on_off_state", false);
    }

    public void setAmplifierState(boolean amplifierState){
        equalizerPreferences.edit().putBoolean("amplifier_on_off_state", amplifierState).apply();
    }

    public int getAmplifierGain(){
        return equalizerPreferences.getInt("amplifier_gain", 0);
    }

    public void setAmplifierGain(int amplifierGain){
        equalizerPreferences.edit().putInt("amplifier_gain", amplifierGain).apply();
    }




    // Queue methods
    public String getQueueTitle(){
        return queuePreferences.getString("queue_title", null);
    }

    public void setQueueTitle(String queueTitle){
        queuePreferences.edit().putString("queue_title", queueTitle).apply();
    }

    public Set<String> getQueue(){
        return queuePreferences.getStringSet("queue", null);
    }

    public void setQueue(HashSet<String> queue){
        queuePreferences.edit().putStringSet("queue", queue).apply();
    }

    public int getQueueIndex(){
        return queuePreferences.getInt("queue_index", -1);
    }

    public void setQueueIndex(int queueIndex){
        queuePreferences.edit().putInt("queue_index", queueIndex).apply();
    }

    public String getQueueFilter(){
        return queuePreferences.getString("queue_filter", "");
    }

    public void setQueueFilter(String queueFilter){
        queuePreferences.edit().putString("queue_filter", queueFilter).apply();
    }



    // Playback methods
    // Seekbar
    public String getCurrentTrack(){

        try {
            return queuePreferences.getString("current_track", null);
        }catch (NullPointerException e){
            return null;
        }
    }

    public void setCurrentTrack(String track){
        queuePreferences.edit().putString("current_track", track).apply();
    }

    public int getSeekbarPosition(){
        return playbackPreferences.getInt("seekbar_position", 0);
    }

    public void setSeekbarPosition(int seekbarPosition){
        playbackPreferences.edit().putInt("seekbar_position", seekbarPosition).apply();
    }

    // Repeat
    public int getRepeatMode(){
        return playbackPreferences.getInt("repeat_mode", 0);
    }

    public void setRepeatMode(int repeatMode){
        playbackPreferences.edit().putInt("repeat_mode", repeatMode).apply();
    }

    // Shuffle
    public int getShuffleMode(){
        return playbackPreferences.getInt("shuffle_mode", 0);
    }

    public void setShuffleMode(int shuffleMode){
        playbackPreferences.edit().putInt("shuffle_mode", shuffleMode).apply();
    }
}
