package com.mnemo.angler.data;

import android.content.Context;

import com.mnemo.angler.data.database.AnglerDB;
import com.mnemo.angler.data.file_storage.AnglerFileStorage;
import com.mnemo.angler.data.firebase_database.AnglerFirebaseDatabase;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.data.preferences.AnglerPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    private Context context;

    public RepositoryModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return context;
    }

    @Provides
    @Singleton
    AnglerRepository provideRepositoty(){
        return new AnglerRepository();
    }

    @Provides
    @Singleton
    AnglerDB provideDB(){
        return new AnglerDB(provideContext());
    }

    @Provides
    @Singleton
    AnglerNetworking provideNetworking(){
        return new AnglerNetworking(provideContext());
    }

    @Provides
    @Singleton
    AnglerFileStorage provideFileStorage(){
        return new AnglerFileStorage(provideContext());
    }

    @Provides
    @Singleton
    AnglerPreferences providePreferences(){
        return new AnglerPreferences(provideContext());
    }

    @Provides
    @Singleton
    AnglerFirebaseDatabase provideFirebaseDatabase(){
        return new AnglerFirebaseDatabase();
    }
}
