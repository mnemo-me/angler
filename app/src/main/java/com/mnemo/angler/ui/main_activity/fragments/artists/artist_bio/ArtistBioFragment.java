package com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio;


import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;


public class ArtistBioFragment extends Fragment implements ArtistBioView {

    ArtistBioPresenter presenter;

    TextView textView;

    String artist;

    public ArtistBioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get artist
        artist = getArguments().getString("artist");

        // Create scrollable TextView
        ScrollView scrollView = new ScrollView(getContext());

        textView = new TextView(getContext());
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(R.color.gGrey));
        textView.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        textView.setPadding((int)(16 * MainActivity.density), (int)(8 * MainActivity.density),(int)(16 * MainActivity.density),(int)(16 * MainActivity.density));

        scrollView.addView(textView);

        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistBioPresenter();
        presenter.attachView(this);

        // Load artist bio
        presenter.loadBio(artist);
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

    // MVP View methods
    @Override
    public void setBio(String bio) {
        textView.setText(bio);
    }
}
