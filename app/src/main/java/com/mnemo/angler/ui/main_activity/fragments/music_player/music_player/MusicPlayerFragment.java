package com.mnemo.angler.ui.main_activity.fragments.music_player.music_player;



import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.Spinner;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.ArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artists.ArtistsFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist.MainPlaylistFragment;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MusicPlayerFragment extends Fragment implements MusicPlayerView {

    MusicPlayerPresenter presenter;

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

    @Nullable
    @BindView(R.id.artist_track_separator)
    View separator;

    ArrayAdapter adapter;

    Disposable disposable;
    Unbinder unbinder;

    int orientation;
    private String fragmentOnTop = "playlist";
    private String artistSelected;


    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_fragment_music_player, container, false);

        orientation = getResources().getConfiguration().orientation;

        // inject views
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // restore visibility of fragments
        if (savedInstanceState != null) {
            fragmentOnTop = savedInstanceState.getString("fragment_on_top");
            artistSelected = savedInstanceState.getString("artist_selected");
        }

        // bind Presenter to View
        presenter = new MusicPlayerPresenter();
        presenter.attachView(this);


        // configure playlist spinner with adapter
        adapter = new ArrayAdapter(getContext(), R.layout.playlist_spinner_item, R.id.playlist_spinner_item_title);
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.playlist_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
/*
                String mainPlaylist = (String)adapterView.getItemAtPosition(i);

                ((MainActivity)getActivity()).setMainPlaylistName(mainPlaylist);
                presenter.updateMainPlaylist(mainPlaylist);

                playlist.setAlpha(1f);
                artists.setAlpha(0.5f);

                showLibrary();*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        presenter.loadPlaylists();

        // configure search toolbar
        configureSearchToolbar();
        restoreSearchBarVisibility(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.deattachView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("search_toolbar_visibility", searchView.getVisibility());
        outState.putString("fragment_on_top", fragmentOnTop);
        outState.putString("artist_selected", artistSelected);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void updateSpinner(List<Playlist> playlists) {

        ArrayList<String> playlistTitles = new ArrayList<>();

        for (Playlist playlist : playlists){
            playlistTitles.add(playlist.getTitle());
        }

        adapter.clear();
        adapter.addAll(playlistTitles);
        adapter.notifyDataSetChanged();

        if (fragmentOnTop.equals("playlist")) {
            showLibrary();
        }else{
            showArtistList();
        }
    }



    // show playlist
    public void showLibrary(){

        playlist.setAlpha(1f);
        artists.setAlpha(0.5f);

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){

            // remove artists or artist tracks fragments
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            Fragment artistsFragment = getChildFragmentManager().findFragmentById(R.id.artist_list);

            if (artistsFragment != null) {
                transaction.remove(artistsFragment);
            }


            Fragment artistTrackFragment = getChildFragmentManager().findFragmentById(R.id.artist_song_list);

            if (artistTrackFragment != null){
                transaction.remove(artistTrackFragment);
            }

            transaction.commit();

            // hide separator
            separator.setVisibility(View.INVISIBLE);
        }

        // open playlist fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.song_list,new MainPlaylistFragment())
                .commit();

        fragmentOnTop = "playlist";

    }


    // show playlist artists
    private void showArtistList(){

        artists.setAlpha(1f);
        playlist.setAlpha(0.5f);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            // open artists or artist tracks fragment
            if (artistSelected != null) {

                ArtistTracksFragment artistTracksFragment = new ArtistTracksFragment();

                Bundle args = new Bundle();
                args.putString("artist", artistSelected);

                artistTracksFragment.setArguments(args);

                getChildFragmentManager().beginTransaction()
                    .replace(R.id.song_list, artistTracksFragment, "artist_track_fragment")
                    .commit();
            }else {

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.song_list, new ArtistsFragment(), "artists_fragment")
                        .commit();
            }
        }else{

            // remove playlist fragment
            Fragment playlistFragment = getChildFragmentManager().findFragmentById(R.id.song_list);

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            if (playlistFragment != null){
                transaction.remove(playlistFragment);
            }

            // remove artist track fragment
            Fragment artistTrackFragment = getChildFragmentManager().findFragmentById(R.id.artist_song_list);

            if (artistTrackFragment != null){
                transaction.remove(artistTrackFragment);
            }

            // open artists fragment
            transaction.replace(R.id.artist_list, new ArtistsFragment(), "artists_fragment");
            transaction.commit();

            // show separator
            separator.setVisibility(View.VISIBLE);

            // open artist track fragment
            if (artistSelected != null){

                ArtistTracksFragment artistTracksFragment = new ArtistTracksFragment();

                Bundle args = new Bundle();
                args.putString("artist", artistSelected);

                artistTracksFragment.setArguments(args);

                getChildFragmentManager().beginTransaction()
                    .replace(R.id.artist_song_list, artistTracksFragment, "artist_track_fragment")
                    .commit();
            }
        }

        fragmentOnTop = "artists";
    }


    // set listeners
    @OnClick(R.id.playlist)
    void showPlaylist(){

        artistSelected = null;

        showLibrary();
    }

    @OnClick(R.id.artists)
    void showArtists(){

        artistSelected = null;

        showArtistList();
    }

    @OnClick(R.id.settings)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }


    // configure search toolbar
    private void configureSearchToolbar(){

        // set listener
        search.setOnClickListener(view -> {

            if (searchView.getVisibility() == View.GONE) {
                search.setAlpha(1f);
                searchView.setVisibility(View.VISIBLE);
            }else{
                if (searchView.getQuery() != "") {
                    searchView.setQuery("", false);
                }

                search.setAlpha(0.5f);
                searchView.setVisibility(View.GONE);
            }

        });

        // customize search toolbar
        EditText editText = (searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));

        // change text color in EditText programmatically
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

                String filter = textViewAfterTextChangeEvent.editable().toString();

                ((MainActivity)getActivity()).setFilter(filter);

                Intent intent = new Intent();
                intent.setAction("filter_applied");
                intent.putExtra("filter", filter);

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

    // restore visibility of search toolbar
    private void restoreSearchBarVisibility(Bundle savedInstanceState){

        if (savedInstanceState != null){
            int searchToolbarVisibility = savedInstanceState.getInt("search_toolbar_visibility");

            if (searchToolbarVisibility == View.VISIBLE) {
                search.setAlpha(1f);
                searchView.setVisibility(searchToolbarVisibility);

            }
        }
    }

    public String getArtistSelected() {
        return artistSelected;
    }

    public void setArtistSelected(String artistSelected) {
        this.artistSelected = artistSelected;
    }

}
