package com.mnemo.angler.data;


import android.content.AsyncTaskLoader;
import android.content.Context;


import com.mnemo.angler.AnglerService;

public class AnglerDBUpdateLoader extends AsyncTaskLoader {

    public AnglerDBUpdateLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Object loadInBackground() {

        AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
        if (!AnglerService.isDBInitialized) {
            dbHelper.updateSources();
        }

        return null;
    }
}
