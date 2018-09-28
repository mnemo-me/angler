package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.utils.MediaAssistant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.claucookie.miniequalizerlibrary.EqualizerView;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder>{

    private Context context;
    private String playlist;
    private List<Track> tracks;
    private String selectedTrackId = null;
    private int playbackState = PlaybackStateCompat.STATE_PAUSED;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.playlist_song_title)
        TextView titleView;

        @BindView(R.id.playlist_song_artist)
        TextView artistView;

        @BindView(R.id.playlist_song_duration)
        TextView durationView;

        @BindView(R.id.playilst_song_mini_equalizer)
        EqualizerView equalizerView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public TrackAdapter(Context context, String playlist, List<Track> tracks) {
        this.context = context;
        this.playlist = playlist;
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.mp_track_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Track track = tracks.get(position);

        String id = track.get_id();

        String title = track.getTitle();
        String artist = track.getArtist();
        long duration = track.getDuration();

        holder.titleView.setText(title);
        holder.artistView.setText(artist);
        holder.durationView.setText(MediaAssistant.convertToTime(duration));

        holder.itemView.setOnClickListener(view -> {

            if (id.equals(selectedTrackId)){

                ((MainActivity)context).getAnglerClient().playPause();

            }else {

                ((MainActivity)context).getAnglerClient().playNow(playlist, position, tracks);
                setTrack(id);

            }
        });

        if (id.equals(selectedTrackId)){
            holder.itemView.setSelected(true);
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.durationView.setVisibility(View.GONE);

            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                holder.equalizerView.animateBars();
            }else{
                holder.equalizerView.stopBars();
            }
        }else{
            if (holder.itemView.isSelected()){
                holder.itemView.setSelected(false);
                holder.equalizerView.setVisibility(View.GONE);
                holder.durationView.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public void setTrack(String trackId) {
        this.selectedTrackId = trackId;
        notifyDataSetChanged();


    }

    public void setPlaybackState(int state){

        playbackState = state;
        notifyItemChanged(findPositionOfTrack(selectedTrackId));
    }

    private int findPositionOfTrack(String trackId){

        for (int i = 0; i < tracks.size(); i++){
            if (tracks.get(i).get_id().equals(trackId)){
                return i;
            }
        }

        return -1;
    }

}
