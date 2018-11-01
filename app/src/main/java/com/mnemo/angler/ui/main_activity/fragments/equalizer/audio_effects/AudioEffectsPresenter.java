package com.mnemo.angler.ui.main_activity.fragments.equalizer.audio_effects;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class AudioEffectsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    AudioEffectsPresenter() {
        AnglerApp.getAnglerComponent().injectAudioEffectsPresenter(this);
    }

    // Virtualizer
    boolean getVirtualizerState(){
        return repository.getVirtualizerState();
    }

    void saveVirtualizerState(boolean virtualizerState){
        repository.setVirtualizerState(virtualizerState);
    }

    int getVirtualizerStrength(){
        return repository.getVirtualizerStrength();
    }

    void saveVirtualizerStrength(int virtualizerStrength){
        repository.setVirtualizerStrength(virtualizerStrength);
    }


    // Bass boost
    boolean getBassBoostState(){
        return repository.getBassBoostState();
    }

    void saveBassBoostState(boolean bassBoostState){
        repository.setBassBoostState(bassBoostState);
    }

    int getBassBoostStrength(){
        return repository.getBassBoostStrength();
    }

    void saveBassBoostStrength(int bassBoostStrength){
        repository.setBassBoostStrength(bassBoostStrength);
    }

    // Amplifier
    boolean getAmplifierState(){
        return repository.getAmplifierState();
    }

    void saveAmplifierState(boolean amplifierState){
        repository.setAmplifierState(amplifierState);
    }

    int getAmplifierGain(){
        return repository.getAmplifierGain();
    }

    void saveAmplifierGain(int amplifierGain){
        repository.setAmplifierGain(amplifierGain);
    }
}
