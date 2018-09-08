package com.mnemo.angler.artists;


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
import com.mnemo.angler.albums.AlbumConfigurationFragment;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.data.MediaAssistant;
import com.mnemo.angler.playlists.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.playlists.LyricsDialogFragment;

import java.io.File;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class ArtistTrackCursorAdapter extends CursorAdapter {

    private String localPlaylistName;


    ArtistTrackCursorAdapter(Context context, Cursor c, String localPlaylistName) {
        super(context, c, true);
        this.localPlaylistName = localPlaylistName;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pm_playlist_song_v2, null, false);
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

        // Merge album image path
        final String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

        // Convert duration to mm:ss format
        String durationInText = MainActivity.convertToTime(duration);

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
        EqualizerView miniEqualizer = view.findViewById(R.id.playilst_song_mimi_equalizer);


        // OnClickListener
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((MainActivity)mContext).playNow(localPlaylistName, position, cursor);
            }
        });

        // OnLongClickListener
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Build contextual menu dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ArtistTrackCursorAdapter.this.mContext);

                // Set title
                LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(ArtistTrackCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu_title, null, false);

                TextView contextMenuTitle = titleLayout.findViewById(R.id.context_menu_title);
                contextMenuTitle.setText(title);

                TextView contextMenuTitleArtist = titleLayout.findViewById(R.id.context_menu_title_artist);
                contextMenuTitleArtist.setText(artist);

                builder.setCustomTitle(titleLayout);


                // Set body
                LinearLayout bodyLinearLayout = (LinearLayout) LayoutInflater.from(ArtistTrackCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu, null, false);

                builder.setView(bodyLinearLayout);
                final AlertDialog dialog = builder.create();
                dialog.show();



                // Contextual menu

                // Play
                TextView contextMenuPlay = bodyLinearLayout.findViewById(R.id.context_menu_play);
                contextMenuPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ((MainActivity)mContext).playNow(localPlaylistName, position, cursor);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 300);

                    }
                });

                // Play next
                TextView contextMenuPlayNext = bodyLinearLayout.findViewById(R.id.context_menu_play_next);
                contextMenuPlayNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ((MainActivity)mContext).addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), true);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 300);

                    }
                });

                // Add to queue
                TextView contextMenuAddToQueue = bodyLinearLayout.findViewById(R.id.context_menu_add_to_queue);
                contextMenuAddToQueue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ((MainActivity)mContext).addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), false);

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
                                lyricsDialogFragment.show(((AppCompatActivity)ArtistTrackCursorAdapter.this.mContext).getSupportFragmentManager(), "Lyrics dialog");

                            }
                        }, 300);
                    }
                });


                // Go to album
                TextView contextMenuGoToAlbum = bodyLinearLayout.findViewById(R.id.context_menu_go_to_album);
                contextMenuGoToAlbum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

                        Bundle args = new Bundle();
                        args.putString("image", albumImagePath);
                        args.putString("album_name", album);
                        args.putString("artist", artist);

                        albumConfigurationFragment.setArguments(args);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                                if (((MainActivity)mContext).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                                    ((MainActivity) mContext).findViewById(R.id.main_frame).setVisibility(View.GONE);
                                }

                                ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.frame, albumConfigurationFragment)
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

                                Bundle args = MediaAssistant.putMetadataInBundle(id, title, artist, album, duration, uri);
                                addTrackToPlaylistDialogFragment.setArguments(args);

                                addTrackToPlaylistDialogFragment.show(((AppCompatActivity)ArtistTrackCursorAdapter.this.mContext).getSupportFragmentManager(), "Add track to playlist dialog");

                            }
                        }, 300);
                    }
                });


                // Set visibility of context menu items
                contextMenuGoToAlbum.setVisibility(View.VISIBLE);

                return true;
                }
            });





    }


}
