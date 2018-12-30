package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationFragment;



public class PlaylistDeleteDialogFragment extends DialogFragment implements PlaylistDeleteView {


    private PlaylistDeletePresenter presenter;

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
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_playlist_delete, null, false);


        TextView question = bodyLayout.findViewById(R.id.playlist_delete_question);
        question.setText(getString(R.string.delete_playlist_question) + " '" + title + "'?");

        // Setup buttons
        TextView yesButton = bodyLayout.findViewById(R.id.playlist_delete_yes);
        yesButton.setOnClickListener(view -> {

            // Delete playlist
            presenter.deletePlaylist(title);

            // Identify back press
            PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity()
                    .getSupportFragmentManager().findFragmentByTag("playlist_configuration_fragment");

            if (playlistConfigurationFragment != null) {
                getActivity().onBackPressed();
            }

            Intent intent = new Intent();
            intent.setAction("playlist_deleted");
            getContext().sendBroadcast(intent);

            dismiss();

            // Toast!
            Toast.makeText(getContext(), "Playlist '" + title + "' deleted", Toast.LENGTH_SHORT).show();
        });


        TextView noButton = bodyLayout.findViewById(R.id.playlist_delete_no);
        noButton.setOnClickListener(view -> dismiss());

        builder.setView(bodyLayout);


        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
    }

}
