package com.mnemo.angler.ui.main_activity.fragments.equalizer.equalizer;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class EqualizerPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    EqualizerPresenter() {
        AnglerApp.getAnglerComponent().injectEqualizerPresenter(this);
    }

    // Get equalizer state
    boolean getEqualizerState(){
        return repository.getEqualizerState();
    }

    // Save equalizet state
    void saveEqualizerState(boolean equalizerState){
        repository.saveEqualizerState(equalizerState);
    }
}
