package com.mnemo.angler.ui.main_activity.fragments.folders.folder_configuration;

import android.util.Log;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

public class FolderConfigurationPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;


    FolderConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectFolderConfigurationPresenter(this);
    }

    // Load folder tracks
    void loadFolderTracks(String folder){

        setListener(repository.loadFolderTracks(folder, tracks -> {

            if (getView() != null){

                this.tracks = tracks;

                ((FolderConfigurationView)getView()).setFolderTracks(tracks);
            }
        }));
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Link methods
    void addFolderLink(String folderName, String folderPath){

        repository.addFolderLink(folderName, folderPath, () -> {

            if (getView() != null){
                ((FolderConfigurationView)getView()).linkAdded();
            }
        });
    }

    void removeFolderLink(String folderPath){

        repository.deleteFolderLink(folderPath, () -> {

            if (getView() != null){
                ((FolderConfigurationView)getView()).linkRemoved();
            }
        });
    }

    void checkLink(String folder) {

        repository.checkFolderLink(folder, (linkStatus) -> {

            if (getView() != null){
                ((FolderConfigurationView)getView()).linkActive(linkStatus);
            }
        });
    }
}
