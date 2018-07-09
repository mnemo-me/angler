package com.mnemo.angler.playlist_manager;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistant;
import com.mnemo.angler.data.AnglerContract.*;

public class AddTrackToPlaylistDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final int LOADER_PLAYLIST_ID = 0;

    private GridView playlistGrid;
    SimpleCursorAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_dialog_title, null, false);
        TextView title = titleLayout.findViewById(R.id.dialog_title);
        title.setText(R.string.add_track_to_playlist);

        builder.setCustomTitle(titleLayout);

        // Setup playlist gridview with adapter
        playlistGrid = new GridView(getActivity());

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            playlistGrid.setNumColumns(2);
        }else{
            playlistGrid.setNumColumns(3);
        }

        playlistGrid.setVerticalSpacing((int) (8 * MainActivity.density));
        playlistGrid.setPadding((int)(4 * MainActivity.density), (int)(4 * MainActivity.density), (int)(4 * MainActivity.density), (int)(4 * MainActivity.density));
        playlistGrid.setClipToPadding(false);
        playlistGrid.setGravity(Gravity.CENTER);

        adapter = new SimpleCursorAdapter(getContext(),R.layout.pm_playlist_v2, null,
                new String[]{PlaylistEntry.COLUMN_IMAGE_RESOURCE, PlaylistEntry.COLUMN_NAME, PlaylistEntry.COLUMN_TRACKS_TABLE},
                new int[]{R.id.playlist_options_image, R.id.playlist_options_name},0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {

                if (view.getId() == R.id.playlist_options_image){
                    ImageAssistant.loadImage(getContext(),cursor.getString(2), (ImageView) view, 104);
                    return true;
                }
                return false;
            }
        });

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        playlistGrid.setAdapter(adapter);

        builder.setView(playlistGrid);


        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setNeutralButton(R.string.create_new_playlist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_PLAYLIST_ID:
                return new CursorLoader(getContext(),
                        PlaylistEntry.CONTENT_URI, null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " = 0", null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(data);

                playlistGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        data.moveToPosition(position);
/*
                        PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();
                        Bundle args = new Bundle();
                        args.putString("type", "playlist");
                        args.putString("image",data.getString(2));
                        args.putString("playlist_name", data.getString(1));
                        playlistConfigurationFragment.setArguments(args);

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame,playlistConfigurationFragment, "playlist_conf_fragment")
                                .addToBackStack(null)
                                .commit();

*/
                    }
                });
                break;

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(null);
                break;
        }
    }
}
