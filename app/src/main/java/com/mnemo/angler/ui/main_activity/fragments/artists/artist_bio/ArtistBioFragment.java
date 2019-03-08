package com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio;


import android.content.res.Configuration;
import android.os.Bundle;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ArtistBioFragment extends Fragment implements ArtistBioView {

    private ArtistBioPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.artist_bio_text)
    TextView textView;

    private String artist;

    public ArtistBioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_bio, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get artist
        artist = getArguments().getString("artist");

        // Set padding
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            textView.setPadding(0, (int)getResources().getDimension(R.dimen.playlist_track_list_padding), 0, 0);
        }

        // Set link movement method
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
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
    public void onDestroy() {
        super.onDestroy();

        presenter.deattachView();
        unbinder.unbind();
    }

    // MVP View methods
    @Override
    public void setBio(String bio) {

        textView.setText(Html.fromHtml(bio));
    }
}
