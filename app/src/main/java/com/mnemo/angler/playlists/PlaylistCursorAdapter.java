package com.mnemo.angler.playlists;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
import com.mnemo.angler.playlist_manager.PlaylistManager;
import com.mnemo.angler.R;
import com.mnemo.angler.albums.AlbumConfigurationFragment;
import com.mnemo.angler.artists.ArtistConfigurationFragment;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.playlist_manager.Track;

import java.io.File;
import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class PlaylistCursorAdapter extends CursorAdapter {

    private String localDBName;

    private onTrackClickListener onTrackClickListener;
    private onTrackRemoveListener onTrackRemoveListener;

    // ids
    private ArrayList<String> ids;


    public interface onTrackClickListener{
        void onTrackClicked(int position);
    }

    public interface onTrackRemoveListener{
        void onTrackRemove(int position, Track trackToRemove, boolean isCurrentTrack);
    }

    public PlaylistCursorAdapter(Context context, Cursor c, String localDBName, ArrayList<String> ids) {
        super(context, c, true);
        this.localDBName = localDBName;
        this.ids = ids;
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

        // Merge album image path
        final String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

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
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistCursorAdapter.this.mContext);

                // Set title
                LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(PlaylistCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu_title, null, false);

                TextView contextMenuTitle = titleLayout.findViewById(R.id.context_menu_title);
                contextMenuTitle.setText(title);

                TextView contextMenuTitleArtist = titleLayout.findViewById(R.id.context_menu_title_artist);
                contextMenuTitleArtist.setText(artist);

                builder.setCustomTitle(titleLayout);


                // Set body
                LinearLayout bodyLinearLayout = (LinearLayout) LayoutInflater.from(PlaylistCursorAdapter.this.mContext).inflate(R.layout.pm_track_context_menu, null, false);

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
                                lyricsDialogFragment.show(((AppCompatActivity)PlaylistCursorAdapter.this.mContext).getSupportFragmentManager(), "Lyrics dialog");

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
                                        .replace(R.id.frame, albumConfigurationFragment, "album_conf_fragment")
                                        .addToBackStack(null)
                                        .commit();
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

                                ((AppCompatActivity) PlaylistCursorAdapter.this.mContext).getSupportFragmentManager().beginTransaction()
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
                                addTrackToPlaylistDialogFragment.show(((AppCompatActivity)PlaylistCursorAdapter.this.mContext).getSupportFragmentManager(), "Add track to playlist dialog");

                            }
                        }, 300);
                    }
                });


                // Move
                TextView contextMenuMove = bodyLinearLayout.findViewById(R.id.context_menu_move);
                contextMenuMove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 300);
                    }
                });


                // Remove
                TextView contextMenuRemove = bodyLinearLayout.findViewById(R.id.context_menu_remove_from_playlist);
                contextMenuRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int currentPositionBeforeRemoving = PlaylistManager.position;

                        boolean isCurrentTrack = false;

                        if (currentPositionBeforeRemoving == position){
                            isCurrentTrack = true;
                        }
/*
                        if (currentPositionBeforeRemoving >= holder.getAdapterPosition()){
                            AnglerService.mPlaylistManager.decrementCurrentPosition();
                        }
*/

                        Track trackToRemove = new Track(id, title, artist, album, duration, uri, source);

                        PlaylistCursorAdapter.this.mContext.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, localDBName),
                                "_id = ?", new String[]{ids.get(position)});

                        decrementPositions(position);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 300);

                        onTrackRemoveListener.onTrackRemove(position + 1, trackToRemove, isCurrentTrack);

                    }
                });




                // Set visibility of contextual menu items
                contextMenuGoToAlbum.setVisibility(View.VISIBLE);
                contextMenuGoToArtist.setVisibility(View.VISIBLE);
                contextMenuMove.setVisibility(View.VISIBLE);
                contextMenuRemove.setVisibility(View.VISIBLE);



                return true;
            }
        });



    }


    // Support methods

    void decrementPositions(int from){

        for (int i = from; i < ids.size(); i++) {

            ContentValues contentValuesFrom = new ContentValues();
            contentValuesFrom.put(AnglerContract.TrackEntry.COLUMN_POSITION, i);

            mContext.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, localDBName),
                    contentValuesFrom, "_id = ?", new String[]{ids.get(i)});

        }

    }

    void incrementPositions(int from){

        for (int i = from; i < ids.size(); i++) {

            ContentValues contentValuesFrom = new ContentValues();
            contentValuesFrom.put(AnglerContract.TrackEntry.COLUMN_POSITION, i + 2);

            mContext.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, localDBName),
                    contentValuesFrom, "_id = ?", new String[]{ids.get(i)});

        }
    }


    // Setters
    public void setOnTrackClickedListener(onTrackClickListener onTrackClickListener){
        this.onTrackClickListener = onTrackClickListener;
    }

    public void setOnTrackRemoveListener(onTrackRemoveListener onTrackRemoveListener) {
        this.onTrackRemoveListener = onTrackRemoveListener;
    }

    /*
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        ContentValues contentValuesFrom = new ContentValues();
        contentValuesFrom.put(AnglerContract.TrackEntry.COLUMN_POSITION, toPosition + 1);

        context.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, currentDBName),
                contentValuesFrom, "_id = ?", new String[]{ids.get(fromPosition)});



        ContentValues contentValuesTo = new ContentValues();
        contentValuesTo.put(AnglerContract.TrackEntry.COLUMN_POSITION, fromPosition + 1);

        context.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, currentDBName),
                contentValuesTo, "_id = ?", new String[]{ids.get(toPosition)});





        //notifyDataSetChanged();




    @Override
    public void onItemDismiss(int position) {

        context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, currentDBName),
                AnglerContract.TrackEntry.COLUMN_POSITION + " = " + (position + 1), null);


        if (cursor.getCount() != 0){
            cursor.moveToFirst();

            do {

                int positionInDb = cursor.getInt(cursor.getColumnIndex(AnglerContract.TrackEntry.COLUMN_POSITION));

                if (positionInDb > position + 1) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AnglerContract.TrackEntry.COLUMN_POSITION, positionInDb - 1);

                    context.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, currentDBName),
                            contentValues, AnglerContract.TrackEntry.COLUMN_POSITION + " = " + positionInDb, null);
                }
            } while (cursor.moveToNext());
        }
        //notifyDataSetChanged();

    }*/
}
