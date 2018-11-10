package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;


import android.content.res.Configuration;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;
import com.mnemo.angler.ui.main_activity.adapters.ArtistTabsAdapter;
import com.mnemo.angler.ui.main_activity.misc.play_all.PlayAllDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;



public class ArtistConfigurationFragment extends Fragment implements ArtistConfigurationView {


    ArtistConfigurationPresenter presenter;

    // Bind views via ButterKnife
    Unbinder unbinder;

    @BindView(R.id.artist_conf_cardview)
    CardView cardView;

    @BindView(R.id.artist_conf_image)
    ImageView imageView;

    @BindView(R.id.artist_conf_artist)
    TextView artistText;

    @Nullable
    @BindView(R.id.artist_conf_play_all)
    Button playAllLayout;

    @Nullable
    @BindView(R.id.artist_conf_separator)
    View separator;

    @BindView(R.id.artist_conf_tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.artist_conf_view_pager)
    ViewPager viewPager;

    @Nullable
    @BindView(R.id.artist_conf_app_bar)
    AppBarLayout appBarLayout;

    @Nullable
    @BindView(R.id.artist_conf_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.artist_conf_back)
    ImageButton back;

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

        // Setup appbar behavior
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

                    float alpha = 0;

                    artistText.setAlpha(alpha);
                    playAllLayout.setAlpha(alpha);
                    cardView.setAlpha(alpha);
                    separator.setAlpha(alpha);
                    back.setAlpha(alpha);

                } else {

                    float alpha = 1f - (float) Math.abs(verticalOffset) / (float) (appBarLayout.getTotalScrollRange() / 2);

                    artistText.setAlpha(alpha);
                    playAllLayout.setAlpha(alpha);
                    cardView.setAlpha(alpha);
                    separator.setAlpha(alpha);
                    back.setAlpha(alpha);
                }
            });
        }

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

    @Optional
    @OnClick(R.id.artist_conf_play_all)
    void playAll(){

        PlayAllDialogFragment playAllDialogFragment = new PlayAllDialogFragment();

        Bundle args = new Bundle();
        args.putString("playlist", localPlaylistName);
        args.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) presenter.getTracks());
        playAllDialogFragment.setArguments(args);

        playAllDialogFragment.show(getActivity().getSupportFragmentManager(), "play_all_dialog_fragment");
    }


    @Optional
    @OnClick(R.id.artist_conf_play_all_button)
    void playAllButton(){
        playAll();
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
            imageHeight = 148;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
    }
}
