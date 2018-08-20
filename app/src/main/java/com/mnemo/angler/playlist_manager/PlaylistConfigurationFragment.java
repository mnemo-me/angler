package com.mnemo.angler.playlist_manager;


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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.Snackbar;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.PlaybackManager;
import com.mnemo.angler.PlaylistManager;
import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.SourceEntry;
import com.mnemo.angler.data.AnglerContract.TrackEntry;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class PlaylistConfigurationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, PlaylistCursorAdapter.onTrackClickListener, PlaylistCursorAdapter.onTrackRemoveListener {


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

    @Nullable @BindView(R.id.playlist_conf_separator)
    View separator;

    @Nullable @BindView(R.id.playlist_conf_options)
    ImageView optionsButton;


    // db and adapter
    SQLiteDatabase db;
    PlaylistCursorAdapter adapter;


    // playlist variables
    String type;
    String image;
    String title;
    String artist;
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
        final View view = inflater.inflate(R.layout.pm_fragment_playlist_configuration, container, false);

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




        // Initialize db
        final AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
        db = dbHelper.getWritableDatabase();


        // Setup listview with adapter
        listView.setDividerHeight(0);

        // Add add tracks to playlist header to playlist
        LinearLayout headerAddTracksToPlaylist = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_playlist_add_tracks_header, null, false);

        headerAddTracksToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddTracksDialogFragment addTracksDialogFragment = new AddTracksDialogFragment();
                Bundle argsToAddTracks = new Bundle();
                argsToAddTracks.putString("db_name", dbName);
                addTracksDialogFragment.setArguments(argsToAddTracks);

                addTracksDialogFragment.show(getActivity().getSupportFragmentManager(), "Add tracks dialog");
            }
        });

        listView.addHeaderView(headerAddTracksToPlaylist);



        // Load tracks
        getLoaderManager().initLoader(LOADER_TRACK_LIST_ID, null, this);



        // Setup menu
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PlaylistOptionsFragment playlistOptionsFragment = new PlaylistOptionsFragment();

                    Bundle args = new Bundle();
                    args.putString("type", type);
                    args.putString("playlist_name", title);
                    args.putString("image", image);
                    args.putBoolean("playlist_inside", true);
                    args.putInt("tracks_count", adapter.getCount());


                    playlistOptionsFragment.setArguments(args);

                    playlistOptionsFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));

                    playlistOptionsFragment.setEnterTransition(new Slide(Gravity.BOTTOM));

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .addSharedElement(cardView, transitionPrefix + " card")
                            .addSharedElement(imageView, transitionPrefix + " cover")
                            .addSharedElement(titleText, transitionPrefix + " title")
                            .addSharedElement(separator, "separator")
                            .addSharedElement(back, "back")
                            .replace(R.id.frame, playlistOptionsFragment, "playlist_opt_fragment")
                            .addToBackStack(null)
                            .commit();
                }
            });
        }else{

            optionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PlaylistOptionsFragment playlistOptionsFragment = new PlaylistOptionsFragment();

                    Bundle args = new Bundle();
                    args.putString("type", type);
                    args.putString("playlist_name", title);
                    args.putString("image", image);
                    args.putBoolean("playlist_inside", true);
                    args.putInt("tracks_count", adapter.getCount());


                    playlistOptionsFragment.setArguments(args);

                    playlistOptionsFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform())
                            .setDuration(0));

                    playlistOptionsFragment.setEnterTransition(new Slide(Gravity.RIGHT));

                    setReenterTransition(new Fade().setStartDelay(300));

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .addSharedElement(cardView, transitionPrefix + " card")
                            .addSharedElement(imageView, transitionPrefix + " cover")
                            .addSharedElement(titleText, transitionPrefix + " title")
                            .addSharedElement(back, "back")
                            .replace(R.id.frame, playlistOptionsFragment, "playlist_opt_fragment")
                            .addToBackStack(null)
                            .commit();
                }
            });
        }




        // Initialize back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                            listView.setItemChecked(PlaylistManager.position + 1, true);
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

        tracksCountView.setText("tracks: " + adapter.getCount());
    }

    // Updating cover
    public void updateCover(){

        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
    }



    // Changing playlist title also image
    public void changeTitle(String newTitle){
        title = newTitle;
        image = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";
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

        if (db != null){
            db.close();
        }

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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

                ArrayList<String> ids = new ArrayList<>();

                if (data.getCount() != 0){
                    data.moveToFirst();

                    do{
                        ids.add(data.getString(0));
                    }while(data.moveToNext());
                }

                adapter = new PlaylistCursorAdapter(getContext(), data, dbName, ids);

                adapter.setOnTrackClickedListener(this);
                adapter.setOnTrackRemoveListener(this);

                listView.setAdapter(adapter);

                checkTracksCount();

                if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                    listView.setItemChecked(PlaylistManager.position + 1, true);
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
    public void onTrackClicked(int position) {

        PlaybackManager.isCurrentTrackHidden = false;
        setCurrentPlaylist();

        PlaylistManager.position = position;
        ((MainActivity)getActivity()).trackClicked();

    }

    @Override
    public void onTrackRemove(final int position , final Track trackToRemove, final boolean isCurrentTrack) {

        if (isCurrentTrack) {
            PlaybackManager.isCurrentTrackHidden = true;
        }

        setCurrentPlaylist();

        Snackbar snackbar = Snackbar.make(getView(), R.string.track_removed, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentValues contentValues = new ContentValues();
                contentValues.put("_id", trackToRemove.getId());
                contentValues.put(TrackEntry.COLUMN_TITLE, trackToRemove.getTitle());
                contentValues.put(TrackEntry.COLUMN_ARTIST, trackToRemove.getArtist());
                contentValues.put(TrackEntry.COLUMN_ALBUM, trackToRemove.getAlbum());
                contentValues.put(TrackEntry.COLUMN_DURATION, trackToRemove.getDuration());
                contentValues.put(TrackEntry.COLUMN_URI, trackToRemove.getUri());
                contentValues.put(TrackEntry.COLUMN_SOURCE, trackToRemove.getSource());
                contentValues.put(TrackEntry.COLUMN_POSITION, position);


                getActivity().getContentResolver().insert(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName), contentValues);

                adapter.incrementPositions(position - 1);

                setCurrentPlaylist();
/*
                if (AnglerService.mPlaylistManager.getCurrentPosition() >= position - 1) {
                    AnglerService.mPlaylistManager.incrementCurrentPosition();
                }

                if (PlaybackManager.isCurrentTrackHidden && AnglerService.mPlaylistManager.getCurrentPosition() == position - 2){
                    AnglerService.mPlaylistManager.incrementCurrentPosition();
                    PlaybackManager.isCurrentTrackHidden = false;
                }
*/

            }
        });
        snackbar.show();
    }


    public void setCurrentPlaylist() {

        int savedPosition = PlaylistManager.position;

        if (PlaylistManager.currentPlaylistName.equals("playlist/" + title)){
            return;
        }

        PlaylistManager.currentPlaylistName = "playlist/" + title;
        localPlaylistName = "playlist/" + title;


        PlaylistManager.position = savedPosition;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
            listView.setItemChecked(PlaylistManager.position + 1, true);
        }
    }

}
