package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import android.content.res.Configuration;
import android.os.Bundle;
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

import com.mnemo.angler.ui.main_activity.adapters.AlbumAdapter;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AlbumsFragment extends Fragment implements DrawerItem, AlbumsView {


    public AlbumsFragment() {
        // Required empty public constructor
    }

    AlbumsPresenter presenter;

    // Bind view via ButterKnife
    Unbinder unbinder;

    @BindView(R.id.albums_list)
    RecyclerView recyclerView;

    AlbumAdapter adapter;

    int orientation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_albums, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
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

        int albumsInLine;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            albumsInLine = 3;
        }else{
            albumsInLine = 5;
        }

        adapter = new AlbumAdapter(getContext(), albums, albumsInLine);
        recyclerView.setAdapter(adapter);
    }
}
