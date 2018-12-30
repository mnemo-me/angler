package com.mnemo.angler.ui.local_load_activity.fragments.landscape_crop;

import android.net.Uri;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class LandscapeCropPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    LandscapeCropPresenter() {
        AnglerApp.getAnglerComponent().injectLandscapeCropPresenter(this);
    }

    // Create new landscape background image file
    Uri createNewBackgroundImageFile(String backgroundImageFileName){

        return repository.getImageUri(backgroundImageFileName, android.content.res.Configuration.ORIENTATION_LANDSCAPE);
    }

    // Delete portrait background image
    void deleteBackgroundImage(String image){
        repository.deleteBackgroundImage(image);
    }
}
