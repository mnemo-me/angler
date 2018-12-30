package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.adapters.AlbumAdapter;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AlbumsFragment extends Fragment implements DrawerItem, AlbumsView {


    public AlbumsFragment() {
        // Required empty public constructor
    }

    private AlbumsPresenter presenter;

    // Bind view via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.albums_list)
    RecyclerView recyclerView;

    @BindView(R.id.albums_empty_text)
    TextView emptyTextView;

    private ShimmerFrameLayout loadingView;

    AlbumAdapter adapter;

    private int orientation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_albums, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Loading view appear handler
        loadingView = view.findViewById(R.id.albums_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Setup recycler view
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new AlbumsPresenter();
        presenter.attachView(this);

        // Load albums
        presenter.loadAlbums();
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
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Setup drawer menu button
    @OnClick(R.id.albums_drawer_back)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }


    // MVP View methods
    @Override
    public void setAlbums(List<Album> albums) {

        // Empty text visibility
        if (albums.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        int albumsInLine;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            albumsInLine = 3;
        }else{
            albumsInLine = 5;
        }

        adapter = new AlbumAdapter(getContext(), albums, albumsInLine);

        adapter.setOnAlbumClickListener((artist, album, year) -> {

            // Create album image path
            String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

            AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("image", albumImagePath);
            args.putString("album_name", album);
            args.putString("artist", artist);
            args.putInt("year", year);
            albumConfigurationFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, albumConfigurationFragment, "album_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });

        adapter.setOnAlbumLongClickListener((artist, album, year) -> {

            // Create album image path
            String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

            CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("album", album);
            args.putString("image", albumImagePath);
            args.putInt("year", year);
            coverDialogFragment.setArguments(args);

            coverDialogFragment.show(getActivity().getSupportFragmentManager(), "album_cover_fragment");
        });

        recyclerView.setAdapter(adapter);
    }
}
