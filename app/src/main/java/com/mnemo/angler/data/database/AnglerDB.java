package com.mnemo.angler.data.database;


import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import com.mnemo.angler.main_activity.MainActivity;

import javax.inject.Inject;

public class AnglerDB implements LoaderManager.LoaderCallbacks{

    private static final int LOADER_DB_UPDATE_ID = 1;

    Context context;

    @Inject
    public AnglerDB() {
    }


    public void updateDatabase(){

    }

    public void init(){

            //((MainActivity)context).getLoaderManager().initLoader(LOADER_DB_UPDATE_ID, null, AnglerDB.this);

    }


    // database update loader callbacks
    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_DB_UPDATE_ID:
                return new AnglerDBUpdateLoader(context);
            default:
                return null;
        }


    }


    @Override
    public void onLoadFinished(Loader loader, Object o) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
