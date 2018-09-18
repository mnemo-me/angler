package com.mnemo.angler.queue_manager;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.claucookie.miniequalizerlibrary.EqualizerView;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> implements DragAndDropCallback.OnDragAndDropListener{

    private Context context;
    private ArrayList<MediaSessionCompat.QueueItem> queue;
    private OnQueueRemovedListener onQueueRemovedListener;
    private int queuePosition;

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


    QueueAdapter(Context context, ArrayList<MediaSessionCompat.QueueItem> queue) {
        this.context = context;
        this.queue = queue;
        this.queuePosition = ((MainActivity)context).getQueuePosition();
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        MediaSessionCompat.QueueItem queueItem = queue.get(holder.getAdapterPosition());
        MediaDescriptionCompat description = queueItem.getDescription();

        // extract metadata
        String title = description.getTitle().toString();
        String artist = description.getSubtitle().toString();

        Bundle bundle = description.getExtras();
        long duration = bundle.getLong("duration");

        // Assign metadata to views
        holder.titleView.setText(title);
        holder.artistView.setText(artist);
        holder.durationView.setText(MainActivity.convertToTime(duration));

        // Activate selector and miniequalizer views
        if (position == queuePosition){
            holder.playSelector.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.durationView.setVisibility(View.GONE);

            holder.equalizerView.animateBars();

        }else{

            if (holder.playSelector.getVisibility() == View.VISIBLE){
                holder.playSelector.setVisibility(View.GONE);
                holder.equalizerView.setVisibility(View.GONE);
                holder.durationView.setVisibility(View.VISIBLE);
            }
        }



        holder.itemView.setOnClickListener(view -> MediaControllerCompat.getMediaController((MainActivity)context).getTransportControls().skipToQueueItem(holder.getAdapterPosition()));

        // Delete track button
        holder.deleteTrack.setOnClickListener(view -> {

            MediaControllerCompat.getMediaController((MainActivity)context).removeQueueItemAt(holder.getAdapterPosition());
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
        this.queuePosition = queuePosition;
    }

    @Override
    public void onDragAndDrop(int oldPosition, int newPosition) {

        Bundle bundle = new Bundle();
        bundle.putInt("old_position", oldPosition);
        bundle.putInt("new_position", newPosition);

        MediaSessionCompat.QueueItem queueItem = queue.get(oldPosition);
        queue.remove(oldPosition);
        queue.add(newPosition, queueItem);

        MediaControllerCompat.getMediaController((MainActivity)context).getTransportControls().sendCustomAction("replace_queue_items", bundle);
        notifyItemMoved(oldPosition, newPosition);
    }
}
