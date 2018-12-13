package com.mnemo.angler.player.client;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class AnglerClientPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    public AnglerClientPresenter() {
        AnglerApp.getAnglerComponent().injectAnglerClientPresenter(this);
    }

    // Queue filter
    String getQueueFilter(){
        return repository.getQueueFilter();
    }

    void saveQueueFilter(String queueFilter){
        repository.setQueueFilter(queueFilter);
    }

}
