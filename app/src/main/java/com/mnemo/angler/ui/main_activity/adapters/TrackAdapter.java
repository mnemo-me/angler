package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist.AddTracksDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.manage_tracks.ManageTracksDialogFragment;
import com.mnemo.angler.ui.main_activity.misc.contextual_menu.ContextualMenuDialogFragment;
import com.mnemo.angler.util.MediaAssistant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.claucookie.miniequalizerlibrary.EqualizerView;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder>{

    private Context context;
    private String type;
    private String playlist;
    private List<Track> tracks;
    private String selectedTrackId = "";
    private int playbackState = PlaybackStateCompat.STATE_STOPPED;

    private boolean isHeaderAttach = false;
    private int HEADER_VIEW_TYPE = 0;

    private boolean isAddTracksButtonActive = true;

    static abstract class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderViewHolder extends ViewHolder{

        @BindView(R.id.add_tracks)
        Button addTracks;

        @BindView(R.id.manage_tracks)
        Button manageTracks;

        HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class TrackViewHolder extends ViewHolder{

        @BindView(R.id.playlist_song_title)
        TextView titleView;

        @BindView(R.id.playlist_song_artist)
        TextView artistView;

        @BindView(R.id.playlist_song_duration)
        TextView durationView;

        @BindView(R.id.playilst_song_mini_equalizer)
        EqualizerView equalizerView;

        TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public TrackAdapter(Context context, String type, String playlist, List<Track> tracks) {
        this.context = context;
        this.type = type;
        this.playlist = playlist;
        this.tracks = tracks;

        if (type.equals("playlist(land)")){
            this.isHeaderAttach = true;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == HEADER_VIEW_TYPE){

            View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_add_tracks_header, parent, false);
            HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);

            // Check buttons
            if (!isAddTracksButtonActive){
                headerViewHolder.addTracks.setEnabled(false);
                headerViewHolder.addTracks.setAlpha(0.3f);
            }

            if (getItemCount() == 1){
                headerViewHolder.manageTracks.setEnabled(false);
                headerViewHolder.manageTracks.setAlpha(0.3f);
            }

            // Setup listeners
            Bundle argsToTracks = new Bundle();
            argsToTracks.putString("title", playlist);


            headerViewHolder.addTracks.setOnClickListener(v -> {

                AddTracksDialogFragment addTracksDialogFragment = new AddTracksDialogFragment();
                addTracksDialogFragment.setArguments(argsToTracks);

                addTracksDialogFragment.show(((MainActivity)context).getSupportFragmentManager(), "Add tracks dialog");
            });

            headerViewHolder.manageTracks.setOnClickListener(v -> {

                ManageTracksDialogFragment manageTracksDialogFragment = new ManageTracksDialogFragment();
                manageTracksDialogFragment.setArguments(argsToTracks);

                manageTracksDialogFragment.show(((MainActivity)context).getSupportFragmentManager(), "Manage tracks dialog");

            });

            return headerViewHolder;

        }else {

            View view = LayoutInflater.from(context).inflate(R.layout.mp_track_item, parent, false);
            TrackViewHolder trackViewHolder = new TrackViewHolder(view);

            // Set listeners
            trackViewHolder.itemView.setOnClickListener(v -> {

                int trackPosition = trackViewHolder.getAdapterPosition() - (isHeaderAttach ? 1:0);

                Track track = tracks.get(trackPosition);

                // Get track variables
                String id = track.get_id();

                if (id.equals(selectedTrackId)) {

                    ((MainActivity) context).getAnglerClient().playPause();

                } else {

                    ((MainActivity) context).getAnglerClient().playNow(type, playlist, trackPosition, tracks);
                    setTrack(id);
                }
            });

            trackViewHolder.itemView.setOnLongClickListener(v -> {

                int trackPosition = trackViewHolder.getAdapterPosition() - (isHeaderAttach ? 1:0);

                Track track = tracks.get(trackPosition);

                // Get track variables
                String artist = track.getArtist();
                String album = track.getAlbum();

                String albumCover = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

                ContextualMenuDialogFragment contextualMenuDialogFragment = new ContextualMenuDialogFragment();

                Bundle args = new Bundle();
                args.putString("type", type);
                args.putString("playlist", playlist);
                args.putString("album_cover", albumCover);
                args.putParcelable("track", track);
                args.putParcelableArrayList("tracks", (ArrayList<Track>) tracks);
                contextualMenuDialogFragment.setArguments(args);

                contextualMenuDialogFragment.show(((MainActivity)context).getSupportFragmentManager(), "contextual_menu_dialog_fragment");

                return true;
            });

            return trackViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (holder instanceof TrackViewHolder){

            int trackPosition;

            if (isHeaderAttach){
                trackPosition = position -1;
            }else{
                trackPosition = position;
            }

            Track track = tracks.get(trackPosition);

            // Get track variables
            String id = track.get_id();

            String title = track.getTitle();
            String artist = track.getArtist();
            long duration = track.getDuration();


            // Fill views
            ((TrackViewHolder)holder).titleView.setText(title);
            ((TrackViewHolder)holder).artistView.setText(artist);
            ((TrackViewHolder)holder).durationView.setText(MediaAssistant.convertToTime(duration));

            // Set selection items visibility
            if (id.equals(selectedTrackId) && playbackState != PlaybackStateCompat.STATE_STOPPED) {
                holder.itemView.setSelected(true);
                ((TrackViewHolder)holder).equalizerView.setVisibility(View.VISIBLE);
                ((TrackViewHolder)holder).durationView.setVisibility(View.GONE);

                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    ((TrackViewHolder)holder).equalizerView.animateBars();
                } else {
                    ((TrackViewHolder)holder).equalizerView.stopBars();
                }
            } else {
                if (holder.itemView.isSelected()) {
                    holder.itemView.setSelected(false);
                    ((TrackViewHolder)holder).equalizerView.setVisibility(View.GONE);
                    ((TrackViewHolder)holder).durationView.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    @Override
    public int getItemCount() {

        if (isHeaderAttach){
            return tracks.size() + 1;
        }else{
            return tracks.size();
        }
    }

    public void setTrack(String trackId) {
        if (!selectedTrackId.equals(trackId)) {
            this.selectedTrackId = trackId;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0 && isHeaderAttach){
            return HEADER_VIEW_TYPE;
        }else{
            int TRACK_VIEW_TYPE = 1;
            return TRACK_VIEW_TYPE;
        }
    }

    public void setPlaybackState(int state){

        playbackState = state;
        notifyDataSetChanged();
    }

    public void disableAddTracksButton(){
        isAddTracksButtonActive = false;
        notifyItemChanged(0);
    }

}
