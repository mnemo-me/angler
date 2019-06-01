package com.mnemo.angler.ui.main_activity.fragments.folders.folders;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;


import javax.inject.Inject;

public class FoldersPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    FoldersPresenter() {

        AnglerApp.getAnglerComponent().injectFoldersPresenter(this);
    }

    // Load music folders
    void loadFolders(){

        setListener(repository.getMusicFolders(musicFolders -> {

            if (getView() != null){
                ((FoldersView)getView()).setFolders(musicFolders);
            }
        }));
    }
}
