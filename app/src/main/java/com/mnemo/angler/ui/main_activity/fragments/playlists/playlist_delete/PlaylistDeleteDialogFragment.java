package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete;



import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationFragment;



public class PlaylistDeleteDialogFragment extends DialogFragment implements PlaylistDeleteView {


    PlaylistDeletePresenter presenter;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Bind Presenter to View
        presenter = new PlaylistDeletePresenter();
        presenter.attachView(this);

        // Get title
        String title = getArguments().getString("title");

        // Setup body
        builder.setMessage(R.string.delete_playlist_answer);

        // Setup buttons
        builder.setPositiveButton(R.string.delete, (dialogInterface, i) -> {

            // Delete playlist
            presenter.deletePlaylist(title);

            // Identify back press
            PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity()
                    .getSupportFragmentManager().findFragmentByTag("playlist_configuration_fragment");

            if (playlistConfigurationFragment != null){
                getActivity().onBackPressed();
            }

            // Toast!
            Toast.makeText(getContext(), "Playlist '" + title + "' deleted", Toast.LENGTH_SHORT).show();
        });


        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

        });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
    }

}
