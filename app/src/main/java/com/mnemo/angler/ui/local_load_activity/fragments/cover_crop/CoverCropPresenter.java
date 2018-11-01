package com.mnemo.angler.ui.local_load_activity.fragments.cover_crop;

import android.net.Uri;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class CoverCropPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    CoverCropPresenter() {
        AnglerApp.getAnglerComponent().injectCoverCropPresenter(this);
    }

    // Save temp cover
    Uri saveTempCover(){
        return repository.getTempCoverUri();
    }
}
