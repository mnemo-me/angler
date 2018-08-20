package com.mnemo.angler.albums;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.PlaybackManager;
import com.mnemo.angler.PlaylistManager;
import com.mnemo.angler.R;
import com.mnemo.angler.artists.ArtistCoverDialogFragment;
import com.mnemo.angler.background_changer.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AlbumConfigurationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AlbumCursorAdapter.onTrackClickListener {

    Unbinder unbinder;


    // Bind views via butterknife

    @BindView(R.id.album_conf_image)
    ImageView imageView;

    @BindView(R.id.album_conf_title)
    TextView titleText;

    @BindView(R.id.album_conf_artist)
    TextView artistView;

    @BindView(R.id.album_conf_tracks_count)
    TextView tracksCountView;

    @BindView(R.id.album_conf_list)
    ListView listView;


    AlbumCursorAdapter adapter;


    // playlist variables
    String image;
    String title;
    String artist;
    String localPlaylistName;


    // other variables;
    int orientation;
    private static final int LOADER_TRACK_LIST_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    public AlbumConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_album_configuration, container, false);

        unbinder = ButterKnife.bind(this, view);

        orientation = getResources().getConfiguration().orientation;


        // Initialize album variables
        image = getArguments().getString("image");
        title = getArguments().getString("album_name");
        artist = getArguments().getString("artist");

        localPlaylistName = "album/" + artist + "/" + title;

        // Load cover image
        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);

        // Assign title & artist
        titleText.setText(title);
        artistView.setText(artist);


        listView.setDividerHeight(0);

        // Load tracks
        getLoaderManager().initLoader(LOADER_TRACK_LIST_ID, null, this);

        // Set on click listener on cover
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

                Bundle args = new Bundle();
                args.putString("artist", artist);
                args.putString("album", title);
                args.putString("image", image);
                artistCoverDialogFragment.setArguments(args);

                artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Album cover fragment");
            }
        });



        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                            listView.setItemChecked(PlaylistManager.position, true);
                        }

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");

        getContext().registerReceiver(receiver, intentFilter);

        return view;
    }


    @Override
    public void onTrackClicked(int position) {

        PlaybackManager.isCurrentTrackHidden = false;

        if (!PlaylistManager.currentPlaylistName.equals("album/" + artist + "/" + title)) {
            PlaylistManager.currentPlaylistName = "album/" + artist + "/" + title;
        }

        PlaylistManager.position = position;
        ((MainActivity)getActivity()).trackClicked();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_TRACK_LIST_ID:

                return new CursorLoader(getContext(),
                      Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, SourceEntry.SOURCE_LIBRARY), null,
                    TrackEntry.COLUMN_ARTIST + " = ? AND " + TrackEntry.COLUMN_ALBUM + " = ?", new String[]{artist, title},
                    TrackEntry.COLUMN_TITLE + " ASC");

            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

                adapter = new AlbumCursorAdapter(getContext(), data);

                adapter.setOnTrackClickedListener(this);

                listView.setAdapter(adapter);

                tracksCountView.setText("tracks: " + adapter.getCount());

                if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                    listView.setItemChecked(PlaylistManager.position, true);
                }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

        }
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

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }


    // Setup back button
    @OnClick(R.id.album_conf_back)
    void back(){
        getActivity().onBackPressed();
    }
}
