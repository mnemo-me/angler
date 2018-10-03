package com.mnemo.angler.ui.main_activity.activity;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;


import javax.inject.Inject;

public class MainActivityPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    MainActivityPresenter() {
        AnglerApp.getAnglerComponent().injectMainActivityPresenter(this);
    }


    // Setup background from shared preferences
    void setupBackground(){

        String backgroundImage = repository.getBackgroundImage();
        int opacity = repository.getBackgroundOpacity();

        ((MainActivityView) getView()).setBackground(backgroundImage, opacity);

    }
}
