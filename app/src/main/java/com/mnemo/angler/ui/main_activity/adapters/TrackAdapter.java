package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist.AddTracksDialogFragment;
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
    private int TRACK_VIEW_TYPE = 1;

    static abstract class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderViewHolder extends ViewHolder{

        @BindView(R.id.add_tracks)
        LinearLayout addTracks;

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

        if (type.equals("playlist")){
            this.isHeaderAttach = true;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == HEADER_VIEW_TYPE){

            View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_add_tracks_header, parent, false);
            return new HeaderViewHolder(view);

        }else {

            View view = LayoutInflater.from(context).inflate(R.layout.mp_track_item, parent, false);
            return new TrackViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder){

            ((HeaderViewHolder)holder).addTracks.setOnClickListener(view -> {

                AddTracksDialogFragment addTracksDialogFragment = new AddTracksDialogFragment();

                Bundle argsToAddTracks = new Bundle();
                argsToAddTracks.putString("title", playlist);
                addTracksDialogFragment.setArguments(argsToAddTracks);

                addTracksDialogFragment.show(((MainActivity)context).getSupportFragmentManager(), "Add tracks dialog");
            });

        }else {

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
            String album = track.getAlbum();
            long duration = track.getDuration();

            String albumCover = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

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

            // Set listeners
            ((TrackViewHolder)holder).itemView.setOnClickListener(view -> {

                if (id.equals(selectedTrackId)) {

                    ((MainActivity) context).getAnglerClient().playPause();

                } else {

                    ((MainActivity) context).getAnglerClient().playNow(playlist, trackPosition, tracks);
                    setTrack(id);
                }
            });

            ((TrackViewHolder)holder).itemView.setOnLongClickListener(view -> {

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
            return TRACK_VIEW_TYPE;
        }
    }

    public void setPlaybackState(int state){

        playbackState = state;
        notifyItemChanged(findPositionOfTrack(selectedTrackId));
    }

    private int findPositionOfTrack(String trackId){

        for (int i = 0; i < tracks.size(); i++){
            if (tracks.get(i).get_id().equals(trackId)){
                return isHeaderAttach ? i+1 : i;
            }
        }

        return -1;
    }

}
