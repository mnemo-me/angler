package com.mnemo.angler.playlist_manager;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Slide;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.PlaybackManager;
import com.mnemo.angler.PlaylistManager;
import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistant;
import com.mnemo.angler.background_changer.LocalLoadFragment;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PlaylistOptionsFragment extends Fragment {

    boolean isPlaylistNew = false;
    boolean isPlaylistInside;

    String type;

    String title;
    TextView titleView;

    String image;
    ImageView coverView;
    CardView cardView;

    String artist;

    String dbName;

    CropIwaResultReceiver resultReceiver;

    ImageView back;
    View separator;

    String transitionPrefix;

    public PlaylistOptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_options, container, false);

        // Get argument (title, image, isPlaylistInside)
        type = getArguments().getString("type");
        title = getArguments().getString("playlist_name");
        image = getArguments().getString("image");
        isPlaylistInside = getArguments().getBoolean("playlist_inside");

        if (type.equals("album")){
            artist = getArguments().getString("artist");
            TextView artistView = view.findViewById(R.id.playlist_options_artist);
            artistView.setVisibility(View.VISIBLE);
            artistView.setText(artist);
        }

        // Get database name
        dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

        // Check is playlist new
        if (title.equals(getResources().getString(R.string.new_playlist))){
            isPlaylistNew = true;
        }

        // Initialize views
        titleView = view.findViewById(R.id.playlist_options_title);
        coverView = view.findViewById(R.id.playlist_options_image);
        cardView = view.findViewById(R.id.playlist_options_cardview);

        separator = view.findViewById(R.id.playlist_options_separator);


        // Create temp cover image (for new playlist)
        if (isPlaylistNew) {
            image = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + "temp.jpg";
            File outputFile = new File(image);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("back3", "drawable", getContext().getPackageName()));
            try {
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set unique transition name
        switch (type){
            case "album":
                transitionPrefix = artist + " " + title;
                break;

            case "artist":

                break;

            case "playlist":
                transitionPrefix = title;
                break;
        }


        coverView.setTransitionName(transitionPrefix + " cover");
        cardView.setTransitionName(transitionPrefix + " card");
        titleView.setTransitionName(transitionPrefix + " title");

        // Fill views
        titleView.setText(title);
        ImageAssistant.loadImage(getContext(), image, coverView, 250);



        // CropIwa result receiver updating image cover when new cover cropped
        resultReceiver = new CropIwaResultReceiver();
        resultReceiver.setListener(new CropIwaResultReceiver.Listener() {
            @Override
            public void onCropSuccess(Uri croppedUri) {

                ImageAssistant.loadImage(getContext(), image, coverView, 250);

                if (isPlaylistInside){
                    ((PlaylistConfigurationFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_conf_fragment")).updateCover();
                }
            }

            @Override
            public void onCropFailed(Throwable e) {

            }
        });
        resultReceiver.register(getContext());


        // Fill artist for 'album' type
        if (type.equals("album")){
            String artist = getArguments().getString("artist");
            TextView artistView = view.findViewById(R.id.playlist_options_artist);
            artistView.setVisibility(View.VISIBLE);
            artistView.setText(artist);
        }


        // Configure options

        // PLAY
        TextView play = view.findViewById(R.id.playlist_options_play);
        if (!isPlaylistNew) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaybackManager.isCurrentTrackHidden = false;

                    switch (type) {

                        case "album":

                            if (PlaylistManager.currentPlaylistName.equals("album/" + artist + "/" + title)) {
                                return;
                            }

                            PlaylistManager.currentPlaylistName = "album/" + artist + "/" + title;
                            break;

                        case "artist":

                            if (PlaylistManager.currentPlaylistName.equals("artist/" + artist)){
                                return;
                            }

                            PlaylistManager.currentPlaylistName = "artist/" + artist;
                            break;

                        case "playlist":

                            if (PlaylistManager.currentPlaylistName.equals("playlist/" + title)){
                                return;
                            }

                            PlaylistManager.currentPlaylistName = "playlist/" + title;
                            break;
                    }

                    PlaylistManager.position = 0;
                    ((MainActivity) getActivity()).trackClicked();
                }
            });
        }else{
            play.setVisibility(View.GONE);
        }

        // RENAME
        TextView rename = view.findViewById(R.id.playlist_options_change_title);

        if (type.equals("playlist")) {
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PlaylistRenameDialogFragment playlistRenameDialogFragment = new PlaylistRenameDialogFragment();
                    Bundle renameArgs = new Bundle();
                    renameArgs.putString("title", title);
                    playlistRenameDialogFragment.setArguments(renameArgs);

                    playlistRenameDialogFragment.show(getActivity().getSupportFragmentManager(), "Rename dialog");
                }
            });

        }else{
            rename.setVisibility(View.GONE);
        }

        // CHANGE COVER
        TextView changeCover = view.findViewById(R.id.playlist_options_change_cover);

        if (type.equals("playlist")) {
             changeCover.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     LocalLoadFragment localLoadFragment = new LocalLoadFragment();
                     Bundle args = new Bundle();
                     args.putString("image_type", "cover");
                     localLoadFragment.setArguments(args);

                     localLoadFragment.setEnterTransition(new Slide(Gravity.BOTTOM));

                     getActivity().getSupportFragmentManager().beginTransaction()
                             .replace(R.id.full_frame, localLoadFragment)
                             .addToBackStack(null)
                             .commit();
                 }
             });
         }else{

            changeCover.setVisibility(View.GONE);
        }


        // SHOW TRACKS
        TextView showTracks = view.findViewById(R.id.playlist_options_show_tracks);
        if (!isPlaylistNew) {
            showTracks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (isPlaylistInside) {

                        getActivity().onBackPressed();

                    } else {

                        PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();
                        Bundle args = new Bundle();
                        args.putString("type", "playlist");
                        args.putString("image", image);
                        args.putString("playlist_name", title);

                        if (type.equals("album")){
                            args.putString("artist", artist);
                        }

                        playlistConfigurationFragment.setArguments(args);

                        playlistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                                .addTransition(new ChangeBounds())
                                .addTransition(new ChangeTransform()));

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .addSharedElement(cardView, transitionPrefix + " card")
                                .addSharedElement(coverView, transitionPrefix + " cover")
                                .addSharedElement(titleView, transitionPrefix + " title")
                                .addSharedElement(separator, "separator")
                                .addSharedElement(back, "back")
                                .replace(R.id.frame, playlistConfigurationFragment, "playlist_conf_fragment")
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });
        }else{
            showTracks.setVisibility(View.GONE);
        }

        // DELETE PLAYLIST
        TextView deletePlaylist = view.findViewById(R.id.playlist_options_delete_playlist);
        if (type.equals("playlist")) {
            if (!isPlaylistNew) {
                deletePlaylist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        PlaylistDeleteDialogFragment playlistDeleteDialogFragment = new PlaylistDeleteDialogFragment();
                        Bundle argsToDelete = new Bundle();
                        argsToDelete.putString("title", title);
                        argsToDelete.putString("db_name", dbName);
                        argsToDelete.putBoolean("playlist_inside", isPlaylistInside);
                        playlistDeleteDialogFragment.setArguments(argsToDelete);

                        playlistDeleteDialogFragment.show(getActivity().getSupportFragmentManager(), "Delete playlist dialog");
                    }
                });
            } else {
                deletePlaylist.setVisibility(View.INVISIBLE);
            }
        }else{
            deletePlaylist.setVisibility(View.INVISIBLE);
        }

        // CREATE PLAYLIST
        if (isPlaylistNew) {
            TextView create = view.findViewById(R.id.playlist_options_create);
            create.setVisibility(View.VISIBLE);
            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (title.equals(getResources().getString(R.string.new_playlist))){
                        Toast.makeText(getContext(), "Change playlist name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

                    dbHelper.createTrackTable(db, AnglerSQLiteDBHelper.createTrackTableName(title));

                    String newImageName = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";
                    copyImage(image, newImageName);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_NAME, title);
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE,newImageName);
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE, AnglerSQLiteDBHelper.createTrackTableName(title));
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST, 0);
                    getActivity().getContentResolver().insert(AnglerContract.PlaylistEntry.CONTENT_URI, contentValues);


                    Toast.makeText(getContext(), "Playlist '" + title + "' created", Toast.LENGTH_SHORT).show();

                    db.close();


                    PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();
                    Bundle args = new Bundle();
                    args.putString("type", "playlist");
                    args.putString("image", image);
                    args.putString("playlist_name", title);
                    playlistConfigurationFragment.setArguments(args);

                    playlistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .addSharedElement(cardView, "card")
                            .addSharedElement(coverView, "cover")
                            .addSharedElement(titleView, "title")
                            .addSharedElement(separator, "separator")
                            .addSharedElement(back, "back")
                            .replace(R.id.frame, playlistConfigurationFragment, "playlist_conf_fragment")
                            .addToBackStack(null)
                            .commit();

                    //getActivity().getSupportFragmentManager().popBackStack();
                }


            });
        }

        if (type.equals("album")){
            TextView create = view.findViewById(R.id.playlist_options_create);
            create.setVisibility(View.INVISIBLE);
        }




        // Initialize back button
        back = view.findViewById(R.id.playlist_options_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }


    // Changing playlist title also changes image and track table names
    public void changeTitle(String newTitle){
        String oldTitle = title;
        title = newTitle;
        titleView.setText(title);

        if (!isPlaylistNew) {

            AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String oldDbName = dbName;
            dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

            db.execSQL("ALTER TABLE " + oldDbName + " RENAME TO " + dbName + ";");

            String oldImage = image;
            image = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";

            File oldImageFile = new File(oldImage);
            oldImageFile.renameTo(new File(image));

            ContentValues contentValues = new ContentValues();
            contentValues.put(AnglerContract.PlaylistEntry.COLUMN_NAME, title);
            contentValues.put(AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE, image);
            contentValues.put(AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE, dbName);

            getActivity().getContentResolver().update(AnglerContract.PlaylistEntry.CONTENT_URI, contentValues, AnglerContract.PlaylistEntry.COLUMN_NAME + " = ?", new String[]{oldTitle});


            if (isPlaylistInside){
                ((PlaylistConfigurationFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_conf_fragment")).changeTitle(title);
            }

            db.close();
        }

    }

    // Getter for image (for using from another fragments)
    public String getImage() {
        return image;
    }

    // Method for copying images in folder (for new playlist)
    private void copyImage(String inputFileName, String outputFileName){

        File inputFile = new File(inputFileName);
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            FileOutputStream outputStream = new FileOutputStream(outputFileName);

            byte[] buff = new byte[1024];
            int length;

            while ((length = inputStream.read(buff)) > 0){
                outputStream.write(buff,0, length);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (resultReceiver != null){
            resultReceiver.unregister(getContext());
        }
    }


}
