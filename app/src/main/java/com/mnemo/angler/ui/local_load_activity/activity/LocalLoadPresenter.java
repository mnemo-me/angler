package com.mnemo.angler.ui.local_load_activity.activity;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class LocalLoadPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    LocalLoadPresenter() {
        AnglerApp.getAnglerComponent().injectLocalLoadPresenter(this);
    }

    // Gather image folders
    void gatherImageFolders(){

        if (getView() != null){
            ((LocalLoadView)getView()).setImageFolders(repository.gatherImageFolders());
        }
    }
}
