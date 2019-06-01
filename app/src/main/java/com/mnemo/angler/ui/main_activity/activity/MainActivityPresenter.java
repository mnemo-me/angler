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

        int opacity = repository.getBackgroundOpacity();

        if (getView() != null){
           ((MainActivityView) getView()).setOpacity(opacity);
        }

        repository.getBackgroundImage(backgroundImage -> {

            if (getView() != null){
                ((MainActivityView) getView()).setBackground(backgroundImage);
            }
        });
    }


}
