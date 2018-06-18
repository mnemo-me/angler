package com.mnemo.angler;



import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.mnemo.angler.data.AnglerContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int LOADER_PLAYLIST_ID = 0;

    @BindView(R.id.main_fragment_playlist_spinner)
    Spinner spinner;

    @BindView(R.id.playlist)
    View playlist;

    @BindView(R.id.artists)
    View artists;

    SimpleCursorAdapter adapter;

    Unbinder unbinder;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Configure playlist spinner with cursor adapter
        adapter = new SimpleCursorAdapter(getContext(), R.layout.playlist_spinner_item, null,
                    new String[]{AnglerContract.PlaylistEntry.COLUMN_NAME},
                    new int[]{R.id.playlist_spinner_item_title});
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.playlist_spinner_dropdown_item);

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        configureNavigationButtons();

        return view;
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




    public void updateMainPlaylist(){

        getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.song_list, new MainPlaylistFragment())
            .commit();
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Setup drawer button
    @OnClick(R.id.settings)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }
}
