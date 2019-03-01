package com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio;


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

    private ShimmerFrameLayout loadingView;

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

        // Loading view appear handler
        loadingView = view.findViewById(R.id.artist_bio_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (textView != null) {
                if (TextUtils.isEmpty(textView.getText())) {
                    loadingView.setVisibility(View.VISIBLE);
                }
            }

        }, 1000);

        // Get artist
        artist = getArguments().getString("artist");

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

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        textView.setText(Html.fromHtml(bio));
    }
}
