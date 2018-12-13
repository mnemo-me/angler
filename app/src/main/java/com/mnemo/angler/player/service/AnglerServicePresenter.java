package com.mnemo.angler.player.service;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class AnglerServicePresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    AnglerServicePresenter() {
        AnglerApp.getAnglerComponent().injectAnglerServicePresenter(this);
    }



    // Equalizer methods
    // Get equalizer state
    boolean getEqualizerState(){
        return repository.getEqualizerState();
    }

    // Get active preset
    int getEqualizerPreset(){
        return repository.getEqualizerPreset();
    }

    // Get bands level
    List<Short> getBandsLevel(int bandsCount){
        return repository.getBandsLevel(bandsCount);
    }


    // Audio efects
    // Virtualizer
    boolean getVirtualizerState(){
        return repository.getVirtualizerState();
    }

    int getVirtualizerStrength(){
        return repository.getVirtualizerStrength();
    }


    // Bass boost
    boolean getBassBoostState(){
        return repository.getBassBoostState();
    }

    int getBassBoostStrength(){
        return repository.getBassBoostStrength();
    }


    // Amplifier
    boolean getAmplifierState(){
        return repository.getAmplifierState();
    }

    int getAmplifierGain(){
        return repository.getAmplifierGain();
    }



    // Queue methods
    // Queue title
    String getQueueTitle(){
        return repository.getQueueTitle();
    }

    void saveQueueTitle(String queueTitle){
        repository.setQueuTitle(queueTitle);
    }

    // Queue
    Set<String> getQueue(){
        return repository.getQueue();
    }

    void saveQueue(HashSet<String> queue){
        repository.setQueue(queue);
    }

    // Queue index
    int getQueueIndex(){
        return repository.getQueueIndex();
    }

    void saveQueueIndex(int queueIndex){
        repository.setQueueIndex(queueIndex);
    }

    // Seekbar position
    int getSeekbarPosition(){
        return repository.getSeekbarPosition();
    }

    void saveSeekbarPosition(int seekbarPosition){
        repository.setSeekbarPosition(seekbarPosition);
    }

    // Repeat mode
    int getRepeatMode(){
        return repository.getRepeatMode();
    }

    void saveRepeatMode(int repeatMode){
        repository.setRepeatMode(repeatMode);
    }

    // Shuffle mode
    int getShuffleMode(){
        return repository.getShuffleMode();
    }

    void saveShuffleMode(int shuffleMode){
        repository.setShuffleMode(shuffleMode);
    }


    // Get library tracks
    void loadLibraryTracks(){
        repository.loadPlaylistTracks("library", tracks -> {

            if (getView() != null){
                ((AnglerServiceView)getView()).setQueue(tracks);
            }
        });
    }

}
