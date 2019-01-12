package com.mnemo.angler.ui.main_activity.fragments.music_player.music_player;



import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.FrameLayout;
import android.widget.Spinner;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artists.PlaylistArtistsFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist.MainPlaylistFragment;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MusicPlayerFragment extends Fragment implements MusicPlayerView {

    private MusicPlayerPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.main_fragment_playlist_spinner)
    Spinner spinner;

    @Nullable
    @BindView(R.id.search)
    View search;

    @BindView(R.id.search_toolbar)
    SearchView searchView;

    @BindView(R.id.playlist)
    View playlist;

    @BindView(R.id.artists)
    View artists;

    @BindView(R.id.song_list)
    FrameLayout songList;

    @Nullable
    @BindView(R.id.artist_track_separator)
    View separator;

    private ArrayAdapter adapter;

    private Disposable disposable;

    private int orientation;
    private boolean isSpinnerInitialized = false;
    private boolean isSearchBarOpen = false;
    private String fragmentOnTop = "";
    private String artistSelected;


    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_fragment_music_player, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Restore visibility of fragments
        if (savedInstanceState != null) {
            fragmentOnTop = savedInstanceState.getString("fragment_on_top");
            artistSelected = savedInstanceState.getString("artist_selected");
        }

        // Configure playlist spinner with adapter
        adapter = new ArrayAdapter(getContext(), R.layout.mp_playlist_spinner_item, R.id.playlist_spinner_item_title);
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.mp_playlist_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (isSpinnerInitialized) {

                    String mainPlaylist = (String) adapterView.getItemAtPosition(i);

                    ((MainActivity) getActivity()).setMainPlaylistName(mainPlaylist);
                    presenter.updateMainPlaylist(mainPlaylist);

                    playlist.setAlpha(1f);
                    artists.setAlpha(0.5f);

                    showLibrary();

                }else{

                    isSpinnerInitialized = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Configure search toolbar
        configureSearchToolbar();
        restoreSearchBarVisibility(savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new MusicPlayerPresenter();
        presenter.attachView(this);

        // Load playlists
        presenter.loadPlaylists();
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
        outState.putBoolean("search_toolbar_visibility", isSearchBarOpen);
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
    public void updateSpinner(List<String> playlists) {

        adapter.clear();
        adapter.addAll(playlists);
        spinner.setSelection(playlists.indexOf(((MainActivity) getActivity()).getMainPlaylistName()));
        adapter.notifyDataSetChanged();

        if (fragmentOnTop.equals("artists")) {
            showArtistList();
        }else{
            showLibrary();
        }
    }



    // Show playlist
    private void showLibrary(){

        playlist.setAlpha(1f);
        artists.setAlpha(0.5f);

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){

            // remove artists or artist tracks fragments
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            Fragment artistsFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.artist_list);

            if (artistsFragment != null) {
                transaction.remove(artistsFragment);
            }


            Fragment artistTrackFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.artist_song_list);

            if (artistTrackFragment != null){
                transaction.remove(artistTrackFragment);
            }

            transaction.commit();

            // hide separator
            separator.setVisibility(View.INVISIBLE);
        }

        // open playlist fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list,new MainPlaylistFragment())
                .commit();

        fragmentOnTop = "playlist";

    }


    // show playlist artists
    private void showArtistList(){

        artists.setAlpha(1f);
        playlist.setAlpha(0.5f);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            // Open artists fragment
            PlaylistArtistsFragment playlistArtistsFragment = new PlaylistArtistsFragment();

            Bundle args = new Bundle();
            args.putString("artist", artistSelected);

            playlistArtistsFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.song_list, playlistArtistsFragment, "artists_fragment")
                    .commit();

        }else{

            // Remove playlist fragment
            Fragment playlistFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.song_list);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            if (playlistFragment != null){
                transaction.remove(playlistFragment);
            }

            // Remove artist track fragment
            Fragment artistTrackFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.artist_song_list);

            if (artistTrackFragment != null){
                transaction.remove(artistTrackFragment);
            }



            // Open artists fragment
            transaction.replace(R.id.artist_list, new PlaylistArtistsFragment(), "artists_fragment");
            transaction.commit();

            // Show separator
            separator.setVisibility(View.VISIBLE);

            // Open artist track fragment
            if (artistSelected != null){

                PlaylistArtistTracksFragment playlistArtistTracksFragment = new PlaylistArtistTracksFragment();

                Bundle args = new Bundle();
                args.putString("artist", artistSelected);

                playlistArtistTracksFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_song_list, playlistArtistTracksFragment, "artist_track_fragment")
                    .commit();
            }
        }

        fragmentOnTop = "artists";
    }


    // Set listeners
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

    @Optional
    @OnClick(R.id.search)
    void openSearch(){

        if (searchView.getTranslationY() == 0) {

            showSearchToolbar();

            searchView.requestFocus();

        }else{
            if (searchView.getQuery().length() > 0) {
                searchView.setQuery("", false);
            }

            hideSearchToolbar();

            searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn).setVisibility(View.GONE);

            searchView.clearFocus();
        }
    }


    // Configure search toolbar
    private void configureSearchToolbar(){

        // Customize search toolbar
        EditText editText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        // Change text color in EditText programmatically
        editText.setHintTextColor(getResources().getColor(R.color.gGrey));
        editText.setTextColor(getResources().getColor(android.R.color.black));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {

            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (hasFocus) {
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }else{
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        searchView.setIconified(false);
        searchView.clearFocus();
        searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn).setVisibility(View.GONE);

        searchView.setOnCloseListener(() -> {

            searchView.setIconified(false);
            searchView.clearFocus();

            return false;
        });

        // Create observer on after text changes events
        Observer<TextViewAfterTextChangeEvent> observer = new Observer<TextViewAfterTextChangeEvent>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {

                String filter = textViewAfterTextChangeEvent.editable().toString();

                if (filter.equals("")) {
                    searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn).setVisibility(View.GONE);
                }

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


        // Create observable with text changes events
        RxTextView.afterTextChangeEvents(editText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    // Restore visibility of search toolbar
    private void restoreSearchBarVisibility(Bundle savedInstanceState){

        if (savedInstanceState != null){
            isSearchBarOpen = savedInstanceState.getBoolean("search_toolbar_visibility");

            if (isSearchBarOpen) {
                showSearchToolbar();
            }
        }
    }

    // Show/hide search toolbar
    private void showSearchToolbar(){

        if (orientation == Configuration.ORIENTATION_PORTRAIT){

            isSearchBarOpen = true;

            search.setAlpha(1f);

            searchView.animate().translationY(searchView.getHeight());
            songList.animate().translationY(searchView.getHeight());
        }
    }

    private void hideSearchToolbar(){

        isSearchBarOpen = false;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){

            search.setAlpha(0.5f);

            searchView.animate().translationY(0);
            songList.animate().translationY(0);
        }
    }


    public String getArtistSelected() {
        return artistSelected;
    }

    public void setArtistSelected(String artistSelected) {
        this.artistSelected = artistSelected;
    }

}
