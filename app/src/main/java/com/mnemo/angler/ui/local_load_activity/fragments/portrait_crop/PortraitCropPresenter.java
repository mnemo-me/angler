package com.mnemo.angler.ui.local_load_activity.fragments.portrait_crop;

import android.net.Uri;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class PortraitCropPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;


    PortraitCropPresenter() {
        AnglerApp.getAnglerComponent().injectPortraitCropPresenter(this);
    }

    // Generate background image file name
    String generateBackgroundImageFileName(String image) {

        return repository.generateNewBackgroundImageName(image);
    }

    // Create new portrait background image file
    Uri createNewBackgroundImageFile(String backgroundImageFileName){

        return repository.getImageUri(backgroundImageFileName, android.content.res.Configuration.ORIENTATION_PORTRAIT);
    }

}
