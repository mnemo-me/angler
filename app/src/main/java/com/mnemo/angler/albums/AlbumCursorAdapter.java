package com.mnemo.angler.albums;


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

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.artists.ArtistConfigurationFragment;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.playlist_manager.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.playlist_manager.LyricsDialogFragment;
import com.mnemo.angler.playlist_manager.PlaylistConfigurationFragment;

import java.io.File;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class AlbumCursorAdapter extends CursorAdapter {


    private onTrackClickListener onTrackClickListener;



    public interface onTrackClickListener{
        void onTrackClicked(int position);
    }


    public AlbumCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pm_playlist_song_v2, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final int position = cursor.getPosition();

        final View constraintLayout =  view;

        // Extract track's metadata from database
        final String id = cursor.getString(0);
        final String title = cursor.getString(1);
        final String artist = cursor.getString(2);
        final String album = cursor.getString(3);
        final long duration = cursor.getInt(4);
        final String uri = cursor.getString(5);
        final String source = cursor.getString(6);

        // Convert duration to mm:ss format
        String durationInText = MainActivity.convertToTime(duration);


        // Assign metadata to views
        TextView titleView = constraintLayout.findViewById(R.id.playlist_song_title);
        titleView.setText(title);

        TextView artistView = constraintLayout.findViewById(R.id.playlist_song_artist);
        artistView.setText(artist);

        TextView durationView = constraintLayout.findViewById(R.id.playlist_song_duration);
        durationView.setText(durationInText);

        // Set mini equalizer view
        EqualizerView miniEqualizer = constraintLayout.findViewById(R.id.playilst_song_mimi_equalizer);


        // Listeners for track clicks

        // OnClickListener
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onTrackClickListener.onTrackClicked(position);
            }
        });

        // OnLongClickListener
        constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

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
                contextMenuPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onTrackClickListener.onTrackClicked(position);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 300);

                    }
                });


                // Lyrics
                TextView contextMenuLyrics = bodyLinearLayout.findViewById(R.id.context_menu_lyrics);
                contextMenuLyrics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                                LyricsDialogFragment lyricsDialogFragment = new LyricsDialogFragment();
                                lyricsDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Lyrics dialog");

                            }
                        }, 300);
                    }
                });


                // Go to artist
                TextView contextMenuGoToArtist = bodyLinearLayout.findViewById(R.id.context_menu_go_to_artist);
                contextMenuGoToArtist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

                        final ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

                        Bundle args = new Bundle();
                        args.putString("image", imagePath);
                        args.putString("artist", artist);
                        artistConfigurationFragment.setArguments(args);


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                                if (((MainActivity)mContext).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                                    ((MainActivity) mContext).findViewById(R.id.main_frame).setVisibility(View.GONE);
                                }

                                ((AppCompatActivity) AlbumCursorAdapter.this.mContext).getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.frame,artistConfigurationFragment, "artist_conf_fragment")
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }, 300);

                    }
                });


                // Add to playlist
                TextView contextMenuAdd = bodyLinearLayout.findViewById(R.id.context_menu_add);
                contextMenuAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                                AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();
                                addTrackToPlaylistDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Add track to playlist dialog");

                            }
                        }, 300);
                    }
                });


                // Set visibility of contextual menu items
                contextMenuGoToArtist.setVisibility(View.VISIBLE);

                return true;
                }
            });


    }


    // Setters
    public void setOnTrackClickedListener(onTrackClickListener onTrackClickListener){
        this.onTrackClickListener = onTrackClickListener;
    }

}
