package com.mnemo.angler.ui.main_activity.fragments.albums;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.artists.ArtistConfigurationFragment;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.utils.MediaAssistant;
import com.mnemo.angler.ui.main_activity.fragments.playlists.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.LyricsDialogFragment;

import java.io.File;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class AlbumCursorAdapter extends CursorAdapter {


    private String localPlaylistName;


    AlbumCursorAdapter(Context context, Cursor c, String localPlaylistName) {
        super(context, c, true);
        this.localPlaylistName = localPlaylistName;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.mp_track_item, null, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final int position = cursor.getPosition();


        // Extract track's metadata from database
        final String id = cursor.getString(0);
        final String title = cursor.getString(1);
        final String artist = cursor.getString(2);
        final String album = cursor.getString(3);
        final long duration = cursor.getInt(4);
        final String uri = cursor.getString(5);

        // Convert duration to mm:ss format
        String durationInText = MediaAssistant.convertToTime(duration);

        // Set tag to view
        view.setTag(id);

        // Assign metadata to views
        TextView titleView = view.findViewById(R.id.playlist_song_title);
        titleView.setText(title);

        TextView artistView = view.findViewById(R.id.playlist_song_artist);
        artistView.setText(artist);

        TextView durationView = view.findViewById(R.id.playlist_song_duration);
        durationView.setText(durationInText);

        // Set mini equalizer view
        EqualizerView miniEqualizer = view.findViewById(R.id.playilst_song_mini_equalizer);


        // Listeners for track clicks

        // OnClickListener
        view.setOnClickListener(view18 -> ((MainActivity)mContext).getAnglerClient().playNow(localPlaylistName, position, cursor));

        // OnLongClickListener
        view.setOnLongClickListener(view17 -> {

            // Build contextual menu dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(AlbumCursorAdapter.this.mContext);

            // Set title
            LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(AlbumCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu_title, null, false);

            TextView contextMenuTitle = titleLayout.findViewById(R.id.context_menu_title);
            contextMenuTitle.setText(title);

            TextView contextMenuTitleArtist = titleLayout.findViewById(R.id.context_menu_title_artist);
            contextMenuTitleArtist.setText(artist);

            builder.setCustomTitle(titleLayout);


            // Set body
            LinearLayout bodyLinearLayout = (LinearLayout) LayoutInflater.from(AlbumCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu, null, false);

            builder.setView(bodyLinearLayout);
            final AlertDialog dialog = builder.create();
            dialog.show();



            // Contextual menu

            // Play
            TextView contextMenuPlay = bodyLinearLayout.findViewById(R.id.context_menu_play);
            contextMenuPlay.setOnClickListener(view16 -> {

                ((MainActivity)mContext).getAnglerClient().playNow(localPlaylistName, position, cursor);

                new Handler().postDelayed(() -> dialog.dismiss(), 300);

            });

            // Play next
            TextView contextMenuPlayNext = bodyLinearLayout.findViewById(R.id.context_menu_play_next);
            contextMenuPlayNext.setOnClickListener(view15 -> {

                ((MainActivity)mContext).getAnglerClient().addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), true);

                new Handler().postDelayed(() -> dialog.dismiss(), 300);

            });

            // Add to queue
            TextView contextMenuAddToQueue = bodyLinearLayout.findViewById(R.id.context_menu_add_to_queue);
            contextMenuAddToQueue.setOnClickListener(view14 -> {

                ((MainActivity)mContext).getAnglerClient().addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), false);

                new Handler().postDelayed(() -> dialog.dismiss(), 300);

            });


            // Lyrics
            TextView contextMenuLyrics = bodyLinearLayout.findViewById(R.id.context_menu_lyrics);
            contextMenuLyrics.setOnClickListener(view13 -> new Handler().postDelayed(() -> {
                dialog.dismiss();

                LyricsDialogFragment lyricsDialogFragment = new LyricsDialogFragment();
                lyricsDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Lyrics dialog");

            }, 300));


            // Go to artist
            TextView contextMenuGoToArtist = bodyLinearLayout.findViewById(R.id.context_menu_go_to_artist);
            contextMenuGoToArtist.setOnClickListener(view12 -> {

                String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

                final ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

                Bundle args = new Bundle();
                args.putString("image", imagePath);
                args.putString("artist", artist);
                artistConfigurationFragment.setArguments(args);


                new Handler().postDelayed(() -> {
                    dialog.dismiss();

                    if (((MainActivity)mContext).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                        ((MainActivity) mContext).findViewById(R.id.main_frame).setVisibility(View.GONE);
                    }

                    ((AppCompatActivity) AlbumCursorAdapter.this.mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame,artistConfigurationFragment, "artist_conf_fragment")
                            .addToBackStack(null)
                            .commit();
                }, 300);

            });


            // Add to playlist
            TextView contextMenuAdd = bodyLinearLayout.findViewById(R.id.context_menu_add);
            contextMenuAdd.setOnClickListener(view1 -> new Handler().postDelayed(() -> {
                dialog.dismiss();

                AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();

                Bundle args = MediaAssistant.putMetadataInBundle(id, title, artist, album, duration, uri);
                addTrackToPlaylistDialogFragment.setArguments(args);

                addTrackToPlaylistDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Add track to playlist dialog");

            }, 300));


            // Set visibility of contextual menu items
            contextMenuGoToArtist.setVisibility(View.VISIBLE);

            return true;
            });


    }



}
