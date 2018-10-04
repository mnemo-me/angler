package com.mnemo.angler.ui.main_activity.misc.contextual_menu;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration.ArtistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist.AddTrackToPlaylistDialogFragment;

import java.util.List;


public class ContextualMenuDialogFragment extends DialogFragment implements ContextualMenuView{

    ContextualMenuPresenter presenter;

    String type;
    String playlist;
    Track track;
    List<Track> tracks;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get variables
        type = getArguments().getString("type");
        playlist = getArguments().getString("playlist");
        track = getArguments().getParcelable("track");
        tracks = getArguments().getParcelableArrayList("tracks");


        // Bind Presenter to View
        presenter = new ContextualMenuPresenter();
        presenter.attachView(this);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Setup header
        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_track_context_menu_title, null, false);

        TextView titleView = titleLayout.findViewById(R.id.context_menu_title);
        TextView artistView = titleLayout.findViewById(R.id.context_menu_title_artist);

        titleView.setText(track.getTitle());
        artistView.setText(track.getArtist());

        builder.setCustomTitle(titleLayout);


        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_track_context_menu, null, false);

        // Contextual menu

        // Main actions
        // Play
        TextView contextMenuPlay = bodyLayout.findViewById(R.id.context_menu_play);
        contextMenuPlay.setOnClickListener(view -> {

            ((MainActivity) getContext()).getAnglerClient().playNow(playlist, tracks.indexOf(track), tracks);

            new Handler().postDelayed(this::dismiss, 300);
        });

        // Play next
        TextView contextMenuPlayNext = bodyLayout.findViewById(R.id.context_menu_play_next);
        contextMenuPlayNext.setOnClickListener(view -> {

            ((MainActivity) getContext()).getAnglerClient().addToQueue(track, playlist, true);

            new Handler().postDelayed(this::dismiss, 300);
        });

        // Add to queue
        TextView contextMenuAddToQueue = bodyLayout.findViewById(R.id.context_menu_add_to_queue);
        contextMenuAddToQueue.setOnClickListener(view -> {

            ((MainActivity) getContext()).getAnglerClient().addToQueue(track, playlist, false);

            new Handler().postDelayed(this::dismiss, 300);
        });

        // Add to playlist
        TextView contextMenuAdd = bodyLayout.findViewById(R.id.context_menu_add);
        contextMenuAdd.setOnClickListener(view -> new Handler().postDelayed(() -> {
            dismiss();

            AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();

            Bundle args = new Bundle();
            args.putParcelable("track", track);
            addTrackToPlaylistDialogFragment.setArguments(args);

            addTrackToPlaylistDialogFragment.show(((MainActivity) getContext()).getSupportFragmentManager(), "add_track_to_playlist_dialog_fragment");

        }, 300));


        // Optional actions
        // Go to artist
        TextView contextMenuGoToArtist = bodyLayout.findViewById(R.id.context_menu_go_to_artist);
        contextMenuGoToArtist.setOnClickListener(view -> {

            String artistImagePath = presenter.getArtistImagePath(track.getArtist());

            ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("image", artistImagePath);
            args.putString("artist", track.getArtist());
            artistConfigurationFragment.setArguments(args);


            new Handler().postDelayed(() -> {
                dismiss();

                if (((MainActivity) getContext()).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                    ((MainActivity) getContext()).findViewById(R.id.main_frame).setVisibility(View.GONE);
                }

                ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, artistConfigurationFragment, "artist_configuration_fragment")
                        .addToBackStack(null)
                        .commit();
            }, 300);

        });

        // Go to album
        TextView contextMenuGoToAlbum = bodyLayout.findViewById(R.id.context_menu_go_to_album);
        contextMenuGoToAlbum.setOnClickListener(view -> {

            String albumImagePath = presenter.getAlbumImagePath(track.getArtist(), track.getAlbum());

            AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("image", albumImagePath);
            args.putString("album_name", track.getAlbum());
            args.putString("artist", track.getArtist());
            albumConfigurationFragment.setArguments(args);


            new Handler().postDelayed(() -> {
                dismiss();

                if (((MainActivity) getContext()).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                    ((MainActivity) getContext()).findViewById(R.id.main_frame).setVisibility(View.GONE);
                }

                ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, albumConfigurationFragment, "album_configuration_fragment")
                        .addToBackStack(null)
                        .commit();
            }, 300);

        });

        // Remove track from playlist
        TextView contextMenuRemoveFromPlaylist = bodyLayout.findViewById(R.id.context_menu_remove_from_playlist);
        contextMenuRemoveFromPlaylist.setOnClickListener(view -> {

            // Delete track
            presenter.deleteTrack(playlist, track.get_id());
        });



        // Set visibility of optional actions
        switch (type){

            case "music_player":

                contextMenuGoToAlbum.setVisibility(View.VISIBLE);
                contextMenuGoToArtist.setVisibility(View.VISIBLE);

                break;

            case "playlist":

                contextMenuGoToAlbum.setVisibility(View.VISIBLE);
                contextMenuGoToArtist.setVisibility(View.VISIBLE);
                contextMenuRemoveFromPlaylist.setVisibility(View.VISIBLE);

                break;

            case "album":

                contextMenuGoToArtist.setVisibility(View.VISIBLE);

                break;

            case "artist":

                contextMenuGoToAlbum.setVisibility(View.VISIBLE);

                break;
        }


        builder.setView(bodyLayout);


        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
    }


    // MVP View methods
    @Override
    public void showDeleteTrackSnackbar(String playlist, String trackId, int position) {

        // Show snackbar
        Snackbar snackbar = Snackbar.make(((MainActivity)getContext()).findViewById(R.id.frame), R.string.track_removed, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, snackbarView -> {

            // Undo deleting
            presenter.restoreTrack(playlist, trackId, position);
        });

        snackbar.show();
        dismiss();
    }
}
