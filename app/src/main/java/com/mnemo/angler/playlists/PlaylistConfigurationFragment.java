package com.mnemo.angler.playlists;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.TrackEntry;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class PlaylistConfigurationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, PlaylistCursorAdapter.onTrackRemoveListener {


    public PlaylistConfigurationFragment() {
        // Required empty public constructor
    }


    Unbinder unbinder;


    // Bind views via butterknife

    @BindView(R.id.playlist_conf_cardview)
    CardView cardView;

    @BindView(R.id.playlist_conf_image)
    ImageView imageView;

    @BindView(R.id.playlist_conf_title)
    TextView titleText;

    @BindView(R.id.playlist_conf_tracks_count)
    TextView tracksCountView;

    @BindView(R.id.playlist_conf_list)
    ListView listView;

    @BindView(R.id.playlist_conf_back)
    ImageButton back;

    @BindView(R.id.playlist_conf_play_all)
    LinearLayout playAllButton;

    // db and adapter
    PlaylistCursorAdapter adapter;


    // playlist variables
    String image;
    String title;
    String dbName;
    String localPlaylistName;


    // other variables;
    int orientation;
    String transitionPrefix;
    private static final int LOADER_TRACK_LIST_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_configuration, container, false);

        unbinder = ButterKnife.bind(this, view);

        orientation = getResources().getConfiguration().orientation;


        // Initialize playlist variables
        if (title == null) {
            image = getArguments().getString("image");
            title = getArguments().getString("playlist_name");
        }

        dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

        localPlaylistName = "playlist/" + title;


        // Load cover image
        // Set unique transition name
        transitionPrefix = title;
        cardView.setTransitionName(transitionPrefix+ " card");
        imageView.setTransitionName(transitionPrefix + " cover");

        updateCover();

        // Assign title
        titleText.setText(title);



        // Setup ListView with adapter
        listView.setDividerHeight(0);

        // Add add tracks to playlist header to playlist
        LinearLayout headerAddTracksToPlaylist = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_playlist_add_tracks_header, null, false);

        headerAddTracksToPlaylist.setOnClickListener(view13 -> {

            AddTracksDialogFragment addTracksDialogFragment = new AddTracksDialogFragment();
            Bundle argsToAddTracks = new Bundle();
            argsToAddTracks.putString("db_name", dbName);
            addTracksDialogFragment.setArguments(argsToAddTracks);

            addTracksDialogFragment.show(getActivity().getSupportFragmentManager(), "Add tracks dialog");
        });

        listView.addHeaderView(headerAddTracksToPlaylist);



        // Load tracks
        getLoaderManager().initLoader(LOADER_TRACK_LIST_ID, null, this);



        // Setup menu
        cardView.setOnClickListener(view12 -> {

            PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

            Bundle args = new Bundle();
            args.putString("action", "change");
            args.putString("title", title);
            args.putString("image", image);
            playlistCreationDialogFragment.setArguments(args);

            playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
        });




        // Initialize back button
        back.setOnClickListener(view1 -> getActivity().onBackPressed());


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (trackPlaylist.equals(localPlaylistName)) {
                            try {
                                listView.setItemChecked(listView.getPositionForView(listView.findViewWithTag(mediaId)), true);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
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


    // Track counter
    public void checkTracksCount(){

        tracksCountView.setText(getString(R.string.tracks) + " " + adapter.getCount());
    }

    // Updating cover
    public void updateCover(){

        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 200;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
    }



    // Changing playlist title also image
    public void changeTitle(String newTitle){
        title = newTitle;
        image = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";

        titleText.setText(title);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("image", image);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();

    }






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_TRACK_LIST_ID:

            return new CursorLoader(getContext(),
                    Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName), null,
                    null, null,
                    TrackEntry.COLUMN_POSITION + " ASC, " + TrackEntry.COLUMN_TITLE + " ASC, " + TrackEntry.COLUMN_ARTIST + " ASC");

            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

                ArrayList<String> ids = new ArrayList<>();

                if (data.getCount() != 0){
                    data.moveToFirst();

                    do{
                        ids.add(data.getString(0));
                    }while(data.moveToNext());
                }

                adapter = new PlaylistCursorAdapter(getContext(), data, "playlist", localPlaylistName, dbName, ids);

                adapter.setOnTrackRemoveListener(this);

                listView.setAdapter(adapter);

                checkTracksCount();


                playAllButton.setOnClickListener(view -> playAll(data));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

        }
    }



    @Override
    public void onTrackRemove(final int position , final Track trackToRemove, final boolean isCurrentTrack) {



        Snackbar snackbar = Snackbar.make(getView(), R.string.track_removed, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, view -> {

            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", trackToRemove.getId());
            contentValues.put(TrackEntry.COLUMN_TITLE, trackToRemove.getTitle());
            contentValues.put(TrackEntry.COLUMN_ARTIST, trackToRemove.getArtist());
            contentValues.put(TrackEntry.COLUMN_ALBUM, trackToRemove.getAlbum());
            contentValues.put(TrackEntry.COLUMN_DURATION, trackToRemove.getDuration());
            contentValues.put(TrackEntry.COLUMN_URI, trackToRemove.getUri());
            contentValues.put(TrackEntry.COLUMN_POSITION, position);


            getActivity().getContentResolver().insert(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName), contentValues);

            adapter.incrementPositions(position - 1);


/*
            if (AnglerService.mPlaylistManager.getCurrentPosition() >= position - 1) {
                AnglerService.mPlaylistManager.incrementCurrentPosition();
            }

            if (PlaybackManager.isCurrentTrackHidden && AnglerService.mPlaylistManager.getCurrentPosition() == position - 2){
                AnglerService.mPlaylistManager.incrementCurrentPosition();
                PlaybackManager.isCurrentTrackHidden = false;
            }
*/

        });
        snackbar.show();
    }



    void playAll(final Cursor data){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LinearLayout bodyLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_play_all_context_menu, null, false);
        builder.setView(bodyLayout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Contextual menu

        // Play now
        TextView playNow = bodyLayout.findViewById(R.id.play_all_play_now);
        playNow.setOnClickListener(view -> {

            ((MainActivity)getActivity()).playNow(localPlaylistName, 0, data);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });

        // Play next
        TextView playNext = bodyLayout.findViewById(R.id.play_all_play_next);
        playNext.setOnClickListener(view -> {

            ((MainActivity)getActivity()).addToQueue(localPlaylistName, data, true);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });

        // Add to queue
        TextView addToQueue = bodyLayout.findViewById(R.id.play_all_add_to_queue);
        addToQueue.setOnClickListener(view -> {

            ((MainActivity)getActivity()).addToQueue(localPlaylistName, data, false);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });
    }



    @Override
    public void onResume() {
        super.onResume();

    }

}
