package com.mnemo.angler.ui.main_activity.fragments.artists.artists;


import android.os.Bundle;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.ui.main_activity.adapters.ArtistAdapter;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration.ArtistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ArtistsFragment extends Fragment implements DrawerItem, ArtistsView {

    private ArtistsPresenter presenter;


    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.artists_grid)
    RecyclerView recyclerView;

    @BindView(R.id.artists_empty_text)
    TextView emptyTextView;

    private ShimmerFrameLayout loadingView;

    private ArtistAdapter adapter;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artists, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Loading view appear handler
        loadingView = view.findViewById(R.id.artists_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistsPresenter();
        presenter.attachView(this);

        // Load artists
        presenter.loadArtists();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }

    // Setup drawer menu button
    @OnClick(R.id.artists_drawer_back)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
    }


    // MVP View methods
    @Override
    public void setArtists(List<String> artists) {

        // Empty text visibility
        if (artists.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        adapter = new ArtistAdapter(getContext(), artists);

        adapter.setOnArtistClickListener((artist, image) -> {

            ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("image", image);
            artistConfigurationFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, artistConfigurationFragment, "artist_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });

        adapter.setOnArtistLongClickListener((artist, image) -> {

            CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("image", image);
            coverDialogFragment.setArguments(args);

            coverDialogFragment.show(getActivity().getSupportFragmentManager(), "cover_dialog_fragment");
        });

        recyclerView.setAdapter(adapter);
    }
}
