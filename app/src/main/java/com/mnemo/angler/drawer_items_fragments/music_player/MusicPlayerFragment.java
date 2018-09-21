package com.mnemo.angler.drawer_items_fragments.music_player;



import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;

import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;

import android.widget.Spinner;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import com.mnemo.angler.main_activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.AnglerContract;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MusicPlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int LOADER_PLAYLIST_ID = 0;

    @BindView(R.id.main_fragment_playlist_spinner)
    Spinner spinner;

    @BindView(R.id.search)
    View search;

    @BindView(R.id.search_toolbar)
    SearchView searchView;

    @BindView(R.id.playlist)
    View playlist;

    @BindView(R.id.artists)
    View artists;

    SimpleCursorAdapter adapter;

    Disposable disposable;
    Unbinder unbinder;

    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_fragment_music_player, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Configure playlist spinner with cursor adapter
        adapter = new SimpleCursorAdapter(getContext(), R.layout.playlist_spinner_item, null,
                    new String[]{AnglerContract.PlaylistEntry.COLUMN_NAME},
                    new int[]{R.id.playlist_spinner_item_title});
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.playlist_spinner_dropdown_item);

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        configureNavigationButtons();
        configureSearchToolbar();

        // restore visibility of search toolbar
        if (savedInstanceState != null){
            int searchToolbarVisibility = savedInstanceState.getInt("search_toolbar_visibility");

            if (searchToolbarVisibility == View.VISIBLE) {
                search.setAlpha(1f);
                searchView.setVisibility(searchToolbarVisibility);

            }
        }

        return view;
    }


    public void showLibrary(){

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list,new MainPlaylistFragment())
                .commit();

    }

    private void showArtistList(){

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list, new ArtistsFragment())
                .commit();
    }



    private void configureNavigationButtons(){


        playlist.setOnClickListener(v -> {

            playlist.setAlpha(1f);
            artists.setAlpha(0.5f);

            showLibrary();
        });


        artists.setOnClickListener(v -> {

            artists.setAlpha(1f);
            playlist.setAlpha(0.5f);

            showArtistList();
        });

        // configure search button
        search.setOnClickListener(view -> {

            if (searchView.getVisibility() == View.GONE) {
                search.setAlpha(1f);
                searchView.setVisibility(View.VISIBLE);
            }else{
                searchView.setQuery("", false);

                search.setAlpha(0.5f);
                searchView.setVisibility(View.GONE);

            }
        });
    }

    private void configureSearchToolbar(){

        EditText editText = (searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));

        // change text color in edittext programmatically
        editText.setHintTextColor(getResources().getColor(R.color.gGrey));
        editText.setTextColor(getResources().getColor(android.R.color.white));

        searchView.setIconified(false);
        searchView.clearFocus();

        searchView.setOnCloseListener(() -> {

            searchView.setIconified(false);
            searchView.clearFocus();
            return false;
        });


        // create observer on after text changes events
        Observer<TextViewAfterTextChangeEvent> observer = new Observer<TextViewAfterTextChangeEvent>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {

                ((MainActivity)getActivity()).getAnglerClient().setFilter(textViewAfterTextChangeEvent.editable().toString());

                Intent intent = new Intent();
                intent.setAction("filter_applied");

                getActivity().sendBroadcast(intent);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };


        // create observable with text changes events
        RxTextView.afterTextChangeEvents(editText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
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
                    if (data.getString(1).equals(((MainActivity)getActivity()).getAnglerClient().getMainPlaylistName())){
                        spinner.setSelection(data.getPosition());
                    }
                }while (data.moveToNext());


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        data.moveToPosition(i);

                        ((MainActivity)getActivity()).getAnglerClient().setMainPlaylistName(data.getString(1));
                        getActivity().getPreferences(Context.MODE_PRIVATE).edit().putString("active_playlist", ((MainActivity)getActivity()).getAnglerClient().getMainPlaylistName()).apply();

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("search_toolbar_visibility", searchView.getVisibility());
    }
}
