package com.mnemo.angler;



import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int LOADER_PLAYLIST_ID = 0;

    Spinner spinner;
    SimpleCursorAdapter adapter;

    ImageButton playlist;
    ImageButton artists;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        // Configure playlist spinner with cursor adapter
        spinner = view.findViewById(R.id.main_fragment_playlist_spinner);
        adapter = new SimpleCursorAdapter(getContext(), R.layout.playlist_spinner_item, null,
                    new String[]{AnglerContract.PlaylistEntry.COLUMN_NAME},
                    new int[]{R.id.playlist_spinner_item_title});
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.playlist_spinner_dropdown_item);

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        configureNavigationButtons();
        configureSettingsButton();
    }


    public void showLibrary(){

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list,new MainPlaylistFragment())
                .commit();

    }

    private void showArtistList(){

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list, new ArtistFragment())
                .commit();
    }



    private void configureNavigationButtons(){

        playlist = getActivity().findViewById(R.id.playlist);
        artists = getActivity().findViewById(R.id.artists);

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playlist.setAlpha(1f);
                artists.setAlpha(0.5f);

                showLibrary();
            }
        });


        artists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                artists.setAlpha(1f);
                playlist.setAlpha(0.5f);

                showArtistList();
            }
        });
    }



    private void configureSettingsButton(){

        ImageView settingsView = getActivity().findViewById(R.id.settings);
        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
            }
        });
    }


    public void updateMainPlaylist(){

        getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.song_list, new MainPlaylistFragment())
            .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_PLAYLIST_ID:
                return new CursorLoader(getContext(),
                        AnglerContract.PlaylistEntry.CONTENT_URI, null,
                        AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " != 1", null,
                        AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:

                adapter.swapCursor(data);

                data.moveToFirst();

                do{
                    if (data.getString(1).equals(PlaylistManager.mainPlaylistName)){
                        spinner.setSelection(data.getPosition());
                    }
                }while (data.moveToNext());


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        data.moveToPosition(i);

                        PlaylistManager.mainPlaylistName = data.getString(1);
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString("active_playlist", PlaylistManager.mainPlaylistName).apply();

                        playlist.setAlpha(1f);
                        artists.setAlpha(0.5f);

                        updateMainPlaylist();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

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
        }
    }


}
