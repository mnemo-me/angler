package com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist;


import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.data.database.Entities.Playlist;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.adapters.AddTrackToPlaylistAdapter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;

import java.util.List;


public class AddTrackToPlaylistDialogFragment extends DialogFragment implements AddTrackToPlaylistView  {

    AddTrackToPlaylistPresenter presenter;

    RecyclerView recyclerView;
    AddTrackToPlaylistAdapter adapter;

    Track track;

    int orientation;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Get track
        track = getArguments().getParcelable("track");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Setup header
        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_dialog_title, null, false);
        TextView title = titleLayout.findViewById(R.id.dialog_title);
        title.setText(R.string.add_track_to_playlist);

        builder.setCustomTitle(titleLayout);

        // Setup body
        recyclerView = new RecyclerView(getContext());

        recyclerView.setPadding((int)(12 * MainActivity.density), (int)(8 * MainActivity.density), (int)(8 * MainActivity.density), (int)(8 * MainActivity.density));
        recyclerView.setClipToPadding(false);

        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        }else{
            gridLayoutManager = new GridLayoutManager(getContext(), 3);
        }

        recyclerView.setLayoutManager(gridLayoutManager);

        builder.setView(recyclerView);


        // Setup buttons
        builder.setNegativeButton(R.string.close, (dialogInterface, i) -> {

        });

        builder.setNeutralButton(R.string.create_new_playlist, (dialogInterface, i) -> {

            PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

            Bundle args = getArguments();
            args.putString("action", "create");
            args.putParcelable("track", track);
            playlistCreationDialogFragment.setArguments(args);

            playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");

        });

        // Bind Presenter to View
        presenter = new AddTrackToPlaylistPresenter();
        presenter.attachView(this);

        // Load playlists
        presenter.loadPlaylistsAndTitlesWithTrack(track.get_id());

        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();

        // Set dialog window size
        AlertDialog alertDialog = (AlertDialog)getDialog();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            alertDialog.getWindow().setLayout((int) (320 * MainActivity.density), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
    }

    // MVP View methods
    @Override
    public void setPlaylists(List<Playlist> playlists, List<String> playlistsWithTrack) {

        adapter = new AddTrackToPlaylistAdapter(getContext(), playlists, playlistsWithTrack);
        adapter.setOnAddTrackToPlaylistListener(playlist -> {

            presenter.addTrackToPlaylist(playlist, track);
            Toast.makeText(getContext(), "'" + track.getArtist() + " - " + track.getTitle() + "' " + getString(R.string.added_to) + " '" + playlist + "'",
                    Toast.LENGTH_SHORT).show();

            dismiss();
        });

        recyclerView.setAdapter(adapter);
    }

}
