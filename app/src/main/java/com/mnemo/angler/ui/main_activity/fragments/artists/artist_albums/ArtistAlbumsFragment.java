package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;


import android.content.res.Configuration;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.adapters.ArtistAlbumAdapter;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ArtistAlbumsFragment extends Fragment implements ArtistAlbumsView {


    private ArtistAlbumsPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.artist_albums_list)
    RecyclerView recyclerView;

    private ArtistAlbumAdapter adapter;

    private String artist;

    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_albums, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            recyclerView.setPadding(0, (int)getResources().getDimension(R.dimen.playlist_track_list_padding), 0, 0);
        }

        // Get artist
        artist = getArguments().getString("artist");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistAlbumsPresenter();
        presenter.attachView(this);

        // Load albums
        presenter.loadArtistAlbums(artist);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        presenter.deattachView();
        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void setArtistAlbums(List<Album> albums) {

        adapter = new ArtistAlbumAdapter(getContext(), artist, albums);

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

            if (presenter.checkAlbumCoverExist(artist, album)) {

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
