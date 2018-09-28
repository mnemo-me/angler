package com.mnemo.angler.ui.main_activity.activity;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.base.BasePresenter;


import javax.inject.Inject;

public class MainActivityPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    public MainActivityPresenter() {
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

        ((MainActivityView)getView()).setBackground(backgroundImage, opacity);
    }
}
