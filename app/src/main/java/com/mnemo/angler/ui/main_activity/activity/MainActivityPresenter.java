package com.mnemo.angler.ui.main_activity.activity;


import android.util.Log;

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


    // Check trial
    void checkTrial(String androidId, long currentTimestamp){

        long trialPeriod = 1296000000;

        long trialTimestamp = repository.getTrialTimestamp();

        if (trialTimestamp != 0){

            if (getView() != null){
                Log.e("%%%%", currentTimestamp + "        " +  trialTimestamp + "         " + String.valueOf(currentTimestamp - trialTimestamp) + "    " + trialPeriod);
                Log.e("%%%%", String.valueOf(currentTimestamp - trialTimestamp < trialPeriod));
                ((MainActivityView)getView()).setTrial(currentTimestamp - trialTimestamp < trialPeriod);
            }

        }else {


            repository.syncTimestamps(androidId, currentTimestamp, (currentTrialTimestamp ,isTrialInitialized) -> {

                repository.setTrialTimestamp(currentTrialTimestamp);

                if (getView() != null){

                    ((MainActivityView)getView()).setTrial(currentTimestamp - currentTrialTimestamp < trialPeriod);

                    if (isTrialInitialized) {
                        ((MainActivityView) getView()).showWelcomeDialog();
                    }
                }

            });
        }
    }


}
