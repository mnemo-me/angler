package com.mnemo.angler.main_activity;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.file_storage.AnglerFolder;


import javax.inject.Inject;

public class MainActivityPresenter {

    private MainActivityView mainActivityView;

    @Inject
    AnglerRepository repository;

    public MainActivityPresenter(MainActivityView mainActivityView) {
        this.mainActivityView = mainActivityView;

        AnglerApp.getAnglerComponent().injectMainActivityPresenter(this);
    }


    public void init(){

        setupBackground();
    }





    // Setup background from shared preferences
    private void setupBackground(){

        //String backgroundImage = repository.getBackgroundImage();
        //int opacity = repository.getBackgroundOpacity();

        String backgroundImage = AnglerFolder.PATH_BACKGROUND_PORTRAIT + "/_bZPFRTm-3M(2).jpeg";
        int opacity = 140;

        mainActivityView.setBackground(backgroundImage, opacity);
    }
}
