package com.mnemo.angler.artists;


import android.content.res.Configuration;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ArtistConfigurationFragment extends Fragment {


    public ArtistConfigurationFragment() {
        // Required empty public constructor
    }

    Unbinder unbinder;


    // Bind views via butterknife

    @BindView(R.id.artist_conf_cardview)
    CardView cardView;

    @BindView(R.id.artist_conf_image)
    ImageView imageView;

    @BindView(R.id.artist_conf_artist)
    TextView artistText;


    @BindView(R.id.artist_conf_tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.artist_conf_view_pager)
    ViewPager viewPager;

    // artist variables
    String image;
    String artist;


    // other variables;
    int orientation;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_configuration, container, false);

        unbinder = ButterKnife.bind(this, view);

        orientation = getResources().getConfiguration().orientation;


        // Initialize artist variables
        image = getArguments().getString("image");
        artist = getArguments().getString("artist");

        // Load cover image
        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);


        // Assign artist text
        artistText.setText(artist);


        // Initialize tab layout with view pager (tracks, albums, bio)
        viewPager.setAdapter(new ArtistTabsAdapter(getActivity().getSupportFragmentManager(), artist));
        tabLayout.setupWithViewPager(viewPager);


        // Set on click listener on cover
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

                Bundle args = new Bundle();
                args.putString("artist", artist);
                args.putString("image", image);
                artistCoverDialogFragment.setArguments(args);

                artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Artist cover fragment");
            }
        });


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();

    }


    // Setup back button
    @OnClick(R.id.artist_conf_back)
    void back(){
        getActivity().onBackPressed();
    }

}
