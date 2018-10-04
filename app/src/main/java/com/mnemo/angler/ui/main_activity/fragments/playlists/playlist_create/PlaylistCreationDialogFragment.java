package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create;


import android.app.Dialog;
import android.content.Intent;
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
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete.PlaylistDeleteDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsFragment;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.ui.local_load_activity.activity.LocalLoadActivity;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;



public class PlaylistCreationDialogFragment extends DialogFragment implements PlaylistCreateView {

    PlaylistCreatePresenter presenter;

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

        // Get cover change variable
        if (savedInstanceState != null){
            isCoverChanged = savedInstanceState.getBoolean("is_cover_changed");
        }

        // Bind Presenter to View
        presenter = new PlaylistCreatePresenter();
        presenter.attachView(this);

        // Initialize temp cover image variable
        tempImage = presenter.getTempImageName();


        // Setup header
        LinearLayout actionTitleLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.misc_dialog_title, null, false);
        TextView actionView = actionTitleLayout.findViewById(R.id.dialog_title);


        // Set variables based on action type
        switch (action){

            case "create":

                title = "New playlist";
                image = tempImage;

                actionView.setText(getString(R.string.create_new_playlist));

                if (savedInstanceState == null) {
                    presenter.createTempImage();
                }

                break;

            case "change":

                title = getArguments().getString("title");
                image = getArguments().getString("cover");

                actionView.setText(getString(R.string.configure_playlist));

                break;
        }

        builder.setCustomTitle(actionTitleLayout);




        // Setup body
        ConstraintLayout constraintLayout = (ConstraintLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_playlist_creation, null, false);

        // Set cover
        ImageView coverView = constraintLayout.findViewById(R.id.playlist_creation_image);

        if (!isCoverChanged) {
            ImageAssistant.loadImage(getContext(), image, coverView, 350);
        }else{
            ImageAssistant.loadImage(getContext(), tempImage, coverView, 350);
        }

        // Load new cover
        ImageView loadImage = constraintLayout.findViewById(R.id.playlist_creation_load_image);
        loadImage.setOnClickListener(view -> {

            Intent intent = new Intent(getActivity(), LocalLoadActivity.class);
            intent.putExtra("image_type", "cover");
            intent.putExtra("cover_image", image);

            startActivity(intent);
        });

        // Title text
        editText = constraintLayout.findViewById(R.id.playlist_creation_title);

        if (action.equals("change")){
            editText.setText(title);
        }

        builder.setView(constraintLayout);

        // CropIwa result receiver updating image cover when new cover cropped
        resultReceiver = new CropIwaResultReceiver();
        resultReceiver.setListener(new CropIwaResultReceiver.Listener() {
            @Override
            public void onCropSuccess(Uri croppedUri) {
                ImageAssistant.loadImage(getContext(), tempImage, coverView, 350);
                isCoverChanged = true;
            }

            @Override
            public void onCropFailed(Throwable e) {

            }
        });
        resultReceiver.register(getContext());



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

        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();

        // Get created dialog
        AlertDialog alertDialog = (AlertDialog)getDialog();

        // Create/Save button (based on action)
        Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);

        switch (action){

            case "create":

                positiveButton.setOnClickListener(view -> {

                    title = editText.getText().toString();

                    if(!checkPlaylistName(title)){
                        return;
                    }

                    // Create new playlist
                    presenter.createPlaylist(title);


                    // Insert track in new playlist table (if exist)
                    Bundle bundle = getArguments();
                    if (bundle != null){
                        presenter.addTrackToPlaylist(title, bundle.getParcelable("track"));
                    }

                    // Toast!
                    Toast.makeText(getContext(), getString(R.string.playlist) + " '" + title + "' " + getString(R.string.created), Toast.LENGTH_SHORT).show();


                    // Open new playlist
                    PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();

                    Bundle args = new Bundle();
                    args.putString("title", title);
                    args.putString("cover", image);

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

                    // Change cover
                    if (isCoverChanged) {
                        presenter.updateCover(image);


                        // Change cover in playlist configuration fragment
                        PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity().getSupportFragmentManager()
                                .findFragmentByTag("playlist_configuration_fragment");

                        if (playlistConfigurationFragment != null){
                            playlistConfigurationFragment.updateCover();
                        }else{

                            PlaylistsFragment playlistsFragment = (PlaylistsFragment) getActivity().getSupportFragmentManager()
                                    .findFragmentByTag("Playlist fragment");
                            playlistsFragment.updateGrid();
                        }
                    }

                    // Change title
                    String newTitle = editText.getText().toString();

                    if (!title.equals(newTitle)){
                        if(!checkPlaylistName(newTitle)){
                            return;
                        }

                        String oldTitle = title;
                        title = newTitle;

                        // Rename playlist
                        presenter.renamePlaylist(oldTitle, newTitle);

                        // Change title in playlist configuration fragment
                        PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity().getSupportFragmentManager()
                                .findFragmentByTag("playlist_configuration_fragment");

                        if (playlistConfigurationFragment != null){
                            playlistConfigurationFragment.changeTitle(title);
                            playlistConfigurationFragment.updateTracks();
                        }

                    }

                    dismiss();

                });

                break;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("is_cover_changed", isCoverChanged);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        resultReceiver.unregister(getContext());
    }


    // Check is new playlist title valid
    private boolean checkPlaylistName(String playlistName){

        // Check if EditText is empty
        if (TextUtils.isEmpty(playlistName)){
            Toast.makeText(getContext(), getString(R.string.playlist_name_is_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check is playlist name already used
        if (presenter.checkPlaylistNameIsAlreadyUsed(playlistName)){
            Toast.makeText(getContext(), getString(R.string.playlist_name_in_used), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check is this name reserved by other sources
        if (playlistName.toLowerCase().equals("library") || playlistName.toLowerCase().equals("new playlist")
                || playlistName.toLowerCase().equals(getResources().getString(R.string.new_playlist).toLowerCase())) {
            Toast.makeText(getContext(), getString(R.string.playlist_name_reserved), Toast.LENGTH_SHORT).show();
            return false;
        }

        // Return true if all checks passed
        return true;
    }
}
