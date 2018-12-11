package com.mnemo.angler.player.queue;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.util.MediaAssistant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.claucookie.miniequalizerlibrary.EqualizerView;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> implements DragAndDropCallback.OnDragAndDropListener{

    private Context context;
    private List<MediaSessionCompat.QueueItem> queue;
    private OnQueueRemovedListener onQueueRemovedListener;
    private int queuePosition = -1;
    private int playbackState = PlaybackStateCompat.STATE_PAUSED;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.queue_track_title)
        TextView titleView;

        @BindView(R.id.queue_track_artist)
        TextView artistView;

        @BindView(R.id.queue_track_duration)
        TextView durationView;

        @BindView(R.id.queue_track_play_selector)
        ImageView playSelector;

        @BindView(R.id.queue_track_delete)
        FrameLayout deleteTrack;

        @BindView(R.id.queue_track_mini_equalizer)
        EqualizerView equalizerView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    QueueAdapter(Context context, List<MediaSessionCompat.QueueItem> queue) {
        this.context = context;
        this.queue = queue;
    }

    public interface OnQueueRemovedListener{
        void onQueueRemove();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qu_queue_track, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MediaSessionCompat.QueueItem queueItem = queue.get(holder.getAdapterPosition());
        MediaDescriptionCompat description = queueItem.getDescription();

        // Extract metadata
        String title = description.getTitle().toString();
        String artist = description.getSubtitle().toString();

        Bundle bundle = description.getExtras();
        long duration = bundle.getLong("duration");

        // Assign metadata to views
        holder.titleView.setText(title);
        holder.artistView.setText(artist);
        holder.durationView.setText(MediaAssistant.convertToTime(duration));

        // Activate selector and miniequalizer views
        if (position == queuePosition){
            holder.playSelector.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.durationView.setVisibility(View.GONE);

            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                holder.equalizerView.animateBars();
            }else{
                holder.equalizerView.stopBars();
            }

        }else{

            if (holder.playSelector.getVisibility() == View.VISIBLE){
                holder.playSelector.setVisibility(View.GONE);
                holder.equalizerView.setVisibility(View.GONE);
                holder.durationView.setVisibility(View.VISIBLE);
            }
        }


Log.e("vvvvvv", holder.getAdapterPosition() + "   " + queuePosition);
        holder.itemView.setOnClickListener(view -> {

            if (holder.getAdapterPosition() == queuePosition){
                ((MainActivity)context).getAnglerClient().playPause();
            }else {
                ((MainActivity)context).getAnglerClient().skipToQueuePosition(holder.getAdapterPosition());
            }
        });

        // Delete track button
        holder.deleteTrack.setOnClickListener(view -> {

            if (holder.getAdapterPosition() <= queuePosition){
                queuePosition--;
            }

            ((MainActivity)context).getAnglerClient().removeQueueItemAt(holder.getAdapterPosition());
            queue.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            onQueueRemovedListener.onQueueRemove();
        });

    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    void setOnQueueRemovedListener(QueueAdapter.OnQueueRemovedListener onQueueRemovedListener) {
        this.onQueueRemovedListener = onQueueRemovedListener;
    }

    void setQueuePosition(int queuePosition) {

        if (this.queuePosition != queuePosition) {
            this.queuePosition = queuePosition;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDragAndDrop(int oldPosition, int newPosition) {

        MediaSessionCompat.QueueItem queueItem = queue.get(oldPosition);
        queue.remove(oldPosition);
        queue.add(newPosition, queueItem);

        ((MainActivity)context).getAnglerClient().replaceQueueItems(oldPosition, newPosition);
        notifyItemMoved(oldPosition, newPosition);
    }

    void setPlaybackState(int playbackState) {

        if (this.playbackState != playbackState) {
            this.playbackState = playbackState;
            notifyItemChanged(queuePosition);
        }
    }

}
