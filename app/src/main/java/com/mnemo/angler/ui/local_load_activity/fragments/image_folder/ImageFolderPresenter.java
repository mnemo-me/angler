package com.mnemo.angler.ui.local_load_activity.fragments.image_folder;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class ImageFolderPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ImageFolderPresenter() {
        AnglerApp.getAnglerComponent().injectImageFolderPresenter(this);
    }

    // Get folder images
    void getImages(String imageFolder){

        if (getView() != null){
            ((ImageFolderView)getView()).setImages(repository.getImages(imageFolder));
        }
    }
}
