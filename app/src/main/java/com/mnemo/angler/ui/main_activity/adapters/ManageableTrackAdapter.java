package com.mnemo.angler.ui.main_activity.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.queue.DragAndDropCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageableTrackAdapter extends RecyclerView.Adapter<ManageableTrackAdapter.ViewHolder> implements DragAndDropCallback.OnDragAndDropListener{

    private List<Track> tracks;
    private OnTracksChangeListener onTracksChangeListener;


    public interface OnTracksChangeListener{
        void onTracksChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.manageable_track_title)
        TextView titleView;

        @BindView(R.id.manageable_track_artist)
        TextView artistView;

        @BindView(R.id.manageable_track_delete)
        FrameLayout deleteTrack;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public ManageableTrackAdapter(List<Track> tracks) {
        this.tracks = tracks;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pm_track_manageable_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        // Delete track button listener
        viewHolder.deleteTrack.setOnClickListener(v -> {

            tracks.remove(viewHolder.getAdapterPosition());
            notifyItemRemoved(viewHolder.getAdapterPosition());
            onTracksChangeListener.onTracksChanged();
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Track track = tracks.get(position);

        // Extract metadata
        String title = track.getTitle();
        String artist = track.getArtist();

        // Assign metadata to views
        holder.titleView.setText(title);
        holder.artistView.setText(artist);

    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }


    @Override
    public void onDragAndDrop(int oldPosition, int newPosition) {

        Track track = tracks.get(oldPosition);
        tracks.remove(oldPosition);
        tracks.add(newPosition, track);

        notifyItemMoved(oldPosition, newPosition);
        onTracksChangeListener.onTracksChanged();
    }


    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    public void setOnTracksChangeListener(OnTracksChangeListener onTracksChangeListener) {
        this.onTracksChangeListener = onTracksChangeListener;
    }
}
