package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    private AlbumAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_albums, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
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
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }

    // Setup drawer menu button
    @OnClick(R.id.albums_drawer_back)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
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

        adapter = new AlbumAdapter(getContext(), albums);

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

            if (presenter.checkAlbumCoverExist(artist, album)){

                CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

                Bundle args = new Bundle();
                args.putString("artist", artist);
                args.putString("album", album);
                args.putString("image", albumImagePath);
                args.putInt("year", year);
                coverDialogFragment.setArguments(args);

                coverDialogFragment.show(getActivity().getSupportFragmentManager(), "album_cover_fragment");

            }else{

                Toast.makeText(getContext(), R.string.no_image, Toast.LENGTH_SHORT).show();
            }

        });

        recyclerView.setAdapter(adapter);
    }
}
