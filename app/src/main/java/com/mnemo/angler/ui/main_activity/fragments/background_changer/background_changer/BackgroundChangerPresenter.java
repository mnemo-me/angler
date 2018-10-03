package com.mnemo.angler.ui.main_activity.fragments.background_changer.background_changer;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class BackgroundChangerPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    BackgroundChangerPresenter() {
        AnglerApp.getAnglerComponent().injectBackgroundChangerPresenter(this);
    }

    // Get current background image
    String getCurrentBackgroundImage(){
        return repository.getBackgroundImage();
    }

    // Get current opacity
    int getCurrentOpacity(){
        return repository.getBackgroundOpacity();
    }

    // Gather background images
    void gatherBackgroundImages(){
        repository.gatherBackgroundImages(images -> {

            if (getView() != null){
                ((BackgroundChangerView)getView()).setBackgroundImages(images);
            }
        });
    }

    // Save background image and opacity
    void saveBackground(String backgroundImage, int opacity){
        repository.saveBackground(backgroundImage, opacity);
    }

    // Delete background image from file storage
    void deleteBackgroundImage(String image){
        repository.deleteBackgroundImage(image);
    }
}
