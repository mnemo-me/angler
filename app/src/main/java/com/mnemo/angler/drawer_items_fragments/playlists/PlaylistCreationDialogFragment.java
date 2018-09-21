package com.mnemo.angler.drawer_items_fragments.playlists;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.drawer_items_fragments.local_load.LocalLoadActivity;
import com.mnemo.angler.data.database.AnglerContract;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.data.database.AnglerSQLiteDBHelper;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlaylistCreationDialogFragment extends DialogFragment {


    String action;

    String title;
    String image;
    String dbName;

    String tempImage;
    boolean isCoverChanged = false;

    EditText editText;

    // CropIwa receiver
    CropIwaResultReceiver resultReceiver;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get action
        action = getArguments().getString("action");

        // Set title
        LinearLayout actionTitleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_dialog_title, null, false);
        TextView actionView = actionTitleLayout.findViewById(R.id.dialog_title);

        // Create temp cover image
        tempImage = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + "temp.jpg";


        // Set variables based on action type
        switch (action){

            case "create":

                title = "New playlist";
                image = tempImage;

                // create temp image
                File outputFile = new File(tempImage);
                Bitmap bm = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("back3", "drawable", getContext().getPackageName()));
                try {
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                actionView.setText(getString(R.string.create_new_playlist));

                break;

            case "change":

                title = getArguments().getString("title");
                image = getArguments().getString("image");

                actionView.setText(getString(R.string.configure_playlist));

                // Get database name
                dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

                break;
        }

        builder.setCustomTitle(actionTitleLayout);




        // Set body
        ConstraintLayout constraintLayout = (ConstraintLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_playlist_creation, null, false);

        final ImageView imageView = constraintLayout.findViewById(R.id.playlist_creation_image);
        ImageAssistant.loadImage(getContext(), image, imageView, 350);

        ImageView loadImage = constraintLayout.findViewById(R.id.playlist_creation_load_image);

        editText = constraintLayout.findViewById(R.id.playlist_creation_title);

        if (action.equals("change")){
            editText.setText(title);
        }


        builder.setView(constraintLayout);


        // Assign buttons

        switch (action){

            case "create":

                builder.setPositiveButton(R.string.create, null);
                break;

            case "change":

                builder.setPositiveButton(R.string.save, null);
                break;
        }




        // Cancel button
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

        });


        // Delete playlist button (change only)
        if (action.equals("change")) {
            builder.setNeutralButton(R.string.delete, (dialogInterface, i) -> {

                PlaylistDeleteDialogFragment playlistDeleteDialogFragment = new PlaylistDeleteDialogFragment();
                Bundle argsToDelete = new Bundle();
                argsToDelete.putString("title", title);
                argsToDelete.putString("db_name", dbName);
                playlistDeleteDialogFragment.setArguments(argsToDelete);

                playlistDeleteDialogFragment.show(getActivity().getSupportFragmentManager(), "Delete playlist dialog");
            });
        }

        // CropIwa result receiver updating image cover when new cover cropped
        resultReceiver = new CropIwaResultReceiver();
        resultReceiver.setListener(new CropIwaResultReceiver.Listener() {
            @Override
            public void onCropSuccess(Uri croppedUri) {
                ImageAssistant.loadImage(getContext(), tempImage, imageView, 350);
                isCoverChanged = true;
            }

            @Override
            public void onCropFailed(Throwable e) {

            }
        });
        resultReceiver.register(getContext());


        // CHANGE COVER
        loadImage.setOnClickListener(view -> {

            Intent intent = new Intent(getActivity(), LocalLoadActivity.class);
            intent.putExtra("image_type", "cover");
            intent.putExtra("cover_image", image);

            startActivity(intent);
        });


        return builder.create();
    }



    private boolean checkPlaylistName(String playlistName){


        // Check if EditText is empty
        if (TextUtils.isEmpty(playlistName)){
            Toast.makeText(getContext(), getString(R.string.playlist_name_is_empty), Toast.LENGTH_SHORT).show();
            return false;
        }


        // Check is playlist name already exist
        Cursor playlistNameCheckerCursor = getActivity().getContentResolver().query(AnglerContract.PlaylistEntry.CONTENT_URI, null,
                AnglerContract.PlaylistEntry.COLUMN_NAME + " = ? OR " + AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE + " = ?", new String[]{playlistName, AnglerSQLiteDBHelper.createTrackTableName(playlistName)},
                null);

        boolean isPlaylistNameAlreadyExist = playlistNameCheckerCursor.getCount() == 1;
        playlistNameCheckerCursor.close();

        if (isPlaylistNameAlreadyExist){
            Toast.makeText(getContext(), getString(R.string.playlist_name_in_used), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check is this name reserved by other sources
        if (playlistName.equals("Library") || playlistName.equals(getResources().getString(R.string.new_playlist))) {

            Toast.makeText(getContext(), getString(R.string.playlist_name_reserved), Toast.LENGTH_SHORT).show();
            return false;

        }


        // Check if playlist name match patter (with regex)
        String regex = "^[\\p{L}\\d _!.,:'-]+$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(playlistName);

        if (!matcher.find()){
            Toast.makeText(getContext(), getString(R.string.incorrect_playlist_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Return true if all checks passed
        return true;

    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog)getDialog();
        Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);

        // Create/Save button (based on action)

        switch (action){

            case "create":

                positiveButton.setOnClickListener(view -> {

                    title = editText.getText().toString();

                    if(!checkPlaylistName(title)){
                        return;
                    }


                    // Get access to db
                    AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    dbName = AnglerSQLiteDBHelper.createTrackTableName(title);


                    // Create new playlist table
                    dbHelper.createTrackTable(db, AnglerSQLiteDBHelper.createTrackTableName(title));

                    // Insert track in new playlist table (if exist)
                    Bundle bundle = getArguments();
                    if (bundle != null){

                        // Assign track variables
                        String title = getArguments().getString("title");
                        String artist = getArguments().getString("artist");
                        String album = getArguments().getString("album");
                        long duration = getArguments().getLong("duration");
                        String uri = getArguments().getString("uri");

                        dbHelper.insertTrack(db, dbName, title, artist, album, duration, uri, 0);

                    }


                    // Copy new playlist image from temp
                    String newImageName = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";
                    ImageAssistant.copyImage(image, newImageName);


                    // Register new playlist in playlists table
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_NAME, title);
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE,newImageName);
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE, AnglerSQLiteDBHelper.createTrackTableName(title));
                    contentValues.put(AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST, 0);
                    getActivity().getContentResolver().insert(AnglerContract.PlaylistEntry.CONTENT_URI, contentValues);


                    Toast.makeText(getContext(), "Playlist '" + title + "' created", Toast.LENGTH_SHORT).show();

                    db.close();


                    // Open new playlist
                    PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();

                    Bundle args = new Bundle();
                    args.putString("image", image);
                    args.putString("playlist_name", title);

                    playlistConfigurationFragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, playlistConfigurationFragment, "playlist_conf_fragment")
                            .addToBackStack(null)
                            .commit();

                    dismiss();

                });

                break;

            case "change":

                positiveButton.setOnClickListener(view -> {

                    // Change title
                    String newTitle = editText.getText().toString();

                    if (!title.equals(newTitle)){
                        if(!checkPlaylistName(newTitle)){
                            return;
                        }

                        String oldTitle = title;
                        title = newTitle;

                        // Get access to db
                        AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                        // Rename db table
                        String oldDbName = dbName;
                        dbName = AnglerSQLiteDBHelper.createTrackTableName(title);

                        db.execSQL("ALTER TABLE " + oldDbName + " RENAME TO " + dbName + ";");

                        // Rename cover image
                        String oldImage = image;
                        image = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";

                        File oldImageFile = new File(oldImage);
                        oldImageFile.renameTo(new File(image));

                        // Save changes in playlists table
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_NAME, title);
                        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE, image);
                        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE, dbName);

                        getActivity().getContentResolver().update(AnglerContract.PlaylistEntry.CONTENT_URI, contentValues, AnglerContract.PlaylistEntry.COLUMN_NAME + " = ?", new String[]{oldTitle});

                        db.close();

                        // Change title in playlist configuration fragment
                        PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_conf_fragment");

                        if (playlistConfigurationFragment != null){
                            playlistConfigurationFragment.changeTitle(title);
                        }

                    }


                    // Change playlist cover
                    if (isCoverChanged) {
                        ImageAssistant.copyImage(tempImage, image);

                        // Change cover in playlist configuration fragment
                        PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_conf_fragment");

                        if (playlistConfigurationFragment != null){
                            playlistConfigurationFragment.updateCover();
                        }else{

                            PlaylistManagerFragment playlistManagerFragment = (PlaylistManagerFragment) getActivity().getSupportFragmentManager().findFragmentByTag("Playlists fragment");
                            playlistManagerFragment.updateGrid();
                        }
                    }

                    dismiss();

                });

                break;


        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resultReceiver.unregister(getContext());
    }
}
