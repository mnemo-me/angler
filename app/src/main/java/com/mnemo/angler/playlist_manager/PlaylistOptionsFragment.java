package com.mnemo.angler.playlist_manager;


import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.Nullable;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PlaylistOptionsFragment extends Fragment {

    boolean isPlaylistNew = false;
    boolean isPlaylistInside;


    @BindView(R.id.playlist_options_image)
    ImageView coverView;

    @BindView(R.id.playlist_options_cardview)
    CardView cardView;

    @BindView(R.id.playlist_options_title)
    TextView titleView;

    @BindView(R.id.playlist_options_back)
    ImageView back;

    @Nullable @BindView(R.id.playlist_options_options)
    ImageView optionsButton;

    @Nullable @BindView(R.id.playlist_options_tracks_count)
    TextView tracksCountView;



    // Options
    @BindView(R.id.playlist_options_play)
    TextView play;

    @BindView(R.id.playlist_options_change_title)
    TextView rename;

    @BindView(R.id.playlist_options_change_cover)
    TextView changeCover;

    @BindView(R.id.playlist_options_show_tracks)
    TextView showTracks;

    @BindView(R.id.playlist_options_delete_playlist)
    TextView deletePlaylist;

    @BindView(R.id.playlist_options_create)
    TextView create;


    // fragment variables
    String type;

    String title;
    String image;
    String dbName;
    int tracksCount;

    String transitionPrefix;

    // CropIwa receiver
    CropIwaResultReceiver resultReceiver;

    Unbinder unbinder;


    public PlaylistOptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_options, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get argument (title, image, isPlaylistInside)
        type = getArguments().getString("type");
        title = getArguments().getString("playlist_name");
        image = getArguments().getString("image");
        isPlaylistInside = getArguments().getBoolean("playlist_inside");
        tracksCount = getArguments().getInt("tracks_count");

        // Check is playlist new
        if (title.equals(getResources().getString(R.string.new_playlist))){
            isPlaylistNew = true;
        }

        // Get database name
        dbName = AnglerSQLiteDBHelper.createTrackTableName(title);



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
        transitionPrefix = title;
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





        // Configure options

        // PLAY
        if (!isPlaylistNew) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PlaybackManager.isCurrentTrackHidden = false;

                    if (tracksCount != 0) {
                        if (PlaylistManager.currentPlaylistName.equals("playlist/" + title)) {
                            return;
                        }

                        PlaylistManager.currentPlaylistName = "playlist/" + title;
                    }else{
                        Toast.makeText(getContext(), R.string.empty_playlist_play_warning, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PlaylistManager.position = 0;
                    ((MainActivity) getActivity()).trackClicked();


                }
            });
        }else{
            play.setVisibility(View.GONE);
        }

        // RENAME
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


        // CHANGE COVER
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



        // SHOW TRACKS
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


                        playlistConfigurationFragment.setArguments(args);

                        playlistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                                .addTransition(new ChangeBounds())
                                .addTransition(new ChangeTransform()));

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .addSharedElement(cardView, transitionPrefix + " card")
                                .addSharedElement(coverView, transitionPrefix + " cover")
                                .addSharedElement(titleView, transitionPrefix + " title")
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


        // CREATE PLAYLIST
        if (isPlaylistNew) {
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
                            .addSharedElement(back, "back")
                            .replace(R.id.frame, playlistConfigurationFragment, "playlist_conf_fragment")
                            .addToBackStack(null)
                            .commit();

                    //getActivity().getSupportFragmentManager().popBackStack();
                }


            });
        }

        // Configure back behavior based on orientation

        final int orientation = getResources().getConfiguration().orientation;
        back = view.findViewById(R.id.playlist_options_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (orientation == Configuration.ORIENTATION_LANDSCAPE && !isPlaylistNew){
                    getActivity().getSupportFragmentManager().popBackStack();
                }

                getActivity().onBackPressed();
            }
        });

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){

            optionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().onBackPressed();
                }
            });

            if (isPlaylistNew) {
                optionsButton.setVisibility(View.GONE);
            }


            tracksCountView.setText("tracks: " + tracksCount);
        }



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

        unbinder.unbind();
    }


}

