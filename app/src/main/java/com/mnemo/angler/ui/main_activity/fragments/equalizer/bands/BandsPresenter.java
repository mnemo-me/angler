package com.mnemo.angler.ui.main_activity.fragments.equalizer.bands;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;


public class BandsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    BandsPresenter() {
        AnglerApp.getAnglerComponent().injectBandsPresenter(this);
    }

    // Get active preset
    int getEqualizerPreset(){
        return repository.getEqualizerPreset();
    }

    // Save active preset
    void saveEqualizerPreset(int preset){
        repository.setEqualizerPreset(preset);
    }

    // Get bands level from shared preferences
    List<Short> getBandsLevel(int bandsCount){
        return repository.getBandsLevel(bandsCount);
    }

    // Save current bands level to shared preferences
    void saveBandsLevel(List<Short> bandsLevel){
        repository.setBandsLevel(bandsLevel);
    }
}
