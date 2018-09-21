package com.mnemo.angler;

import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.RepositoryModule;
import com.mnemo.angler.main_activity.MainActivityPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface AnglerComponent {

    void injectAnglerRepository(AnglerRepository anglerRepository);
    void injectMainActivityPresenter(MainActivityPresenter mainActivityPresenter);

}
