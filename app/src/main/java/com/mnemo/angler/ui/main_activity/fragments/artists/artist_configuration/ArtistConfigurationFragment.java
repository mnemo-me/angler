package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;


import android.content.res.Configuration;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;
import com.mnemo.angler.ui.main_activity.adapters.ArtistTabsAdapter;
import com.mnemo.angler.ui.main_activity.misc.play_all.PlayAllDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;



public class ArtistConfigurationFragment extends Fragment implements ArtistConfigurationView {


    ArtistConfigurationPresenter presenter;

    // Bind views via ButterKnife
    Unbinder unbinder;

    @BindView(R.id.artist_conf_image)
    ImageView imageView;

    @BindView(R.id.artist_conf_artist)
    TextView artistText;

    @BindView(R.id.artist_conf_tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.artist_conf_view_pager)
    ViewPager viewPager;

    // Artist variables
    String image;
    String artist;
    String localPlaylistName;

    // Other variables;
    int orientation;


    public ArtistConfigurationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_configuration, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Initialize artist variables
        artist = getArguments().getString("artist");
        image = getArguments().getString("image");

        localPlaylistName = "artist/" + artist;

        // Assign artist text
        artistText.setText(artist);

        // Load artist image
        loadArtistImage();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistConfigurationPresenter();
        presenter.attachView(this);

        // Load track & albums count
        presenter.loadTracksAndAlbumsCount(artist);
    }

    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        presenter.deattachView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }


    // Setup listeners
    @OnClick(R.id.artist_conf_cardview)
    void openCover() {

     CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

     Bundle args = new Bundle();
     args.putString("artist", artist);
     args.putString("image", image);
     coverDialogFragment.setArguments(args);

     coverDialogFragment.show(getActivity().getSupportFragmentManager(), "cover_dialog_fragment");
    }

    @OnClick(R.id.artist_conf_play_all)
    void playAll(){

        PlayAllDialogFragment playAllDialogFragment = new PlayAllDialogFragment();

        Bundle args = new Bundle();
        args.putString("playlist", localPlaylistName);
        args.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) presenter.getTracks());
        playAllDialogFragment.setArguments(args);

        playAllDialogFragment.show(getActivity().getSupportFragmentManager(), "play_all_dialog_fragment");
    }

    @OnClick(R.id.artist_conf_back)
    void back(){
        getActivity().onBackPressed();
    }



    // MVP View methods
    @Override
    public void initializeTabs(int tracksCount, int albumsCount) {

        viewPager.setAdapter(new ArtistTabsAdapter(getActivity().getSupportFragmentManager(), getContext(), artist, tracksCount, albumsCount, orientation));
        tabLayout.setupWithViewPager(viewPager);
    }



    // Support methods
    void loadArtistImage(){

        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
    }
}
