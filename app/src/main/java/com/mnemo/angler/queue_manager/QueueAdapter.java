package com.mnemo.angler.queue_manager;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> implements DragAndDropCallback.OnDragAndDropListener{

    private Context context;
    private ArrayList<MediaSessionCompat.QueueItem> queue;
    private OnQueueRemovedListener onQueueRemovedListener;
    private int queuePosition;

    static class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout constraintLayout;

        ViewHolder(ConstraintLayout constraintLayout) {
            super(constraintLayout);
            this.constraintLayout = constraintLayout;
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

        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.qu_queue_track, parent, false);

        return new ViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        MediaSessionCompat.QueueItem queueItem = queue.get(position);
        final MediaDescriptionCompat description = queueItem.getDescription();

        // extract metadata
        String title = description.getTitle().toString();
        String artist = description.getSubtitle().toString();

        Bundle bundle = description.getExtras();
        String album = bundle.getString("album");
        long duration = bundle.getLong("duration");

        String uri = description.getMediaUri().toString();

        // Assign metadata to views
        TextView titleView = holder.constraintLayout.findViewById(R.id.queue_track_title);
        titleView.setText(title);

        TextView artistView = holder.constraintLayout.findViewById(R.id.queue_track_artist);
        artistView.setText(artist);

        TextView durationView = holder.constraintLayout.findViewById(R.id.queue_track_duration);
        durationView.setText(MainActivity.convertToTime(duration));

        // Activate selector and miniequalizer views
        ImageView playSelector = holder.constraintLayout.findViewById(R.id.queue_track_play_selector);
        EqualizerView equalizerView = holder.constraintLayout.findViewById(R.id.queue_track_mini_equalizer);

        if (position == queuePosition){
            playSelector.setVisibility(View.VISIBLE);
            equalizerView.setVisibility(View.VISIBLE);
            durationView.setVisibility(View.GONE);

            equalizerView.animateBars();

        }else{
            if (playSelector.getVisibility() == View.VISIBLE){
                playSelector.setVisibility(View.GONE);
                equalizerView.setVisibility(View.GONE);
                durationView.setVisibility(View.VISIBLE);
            }
        }



        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaControllerCompat.getMediaController((MainActivity)context).getTransportControls().skipToQueueItem(holder.getAdapterPosition());
            }
        });

        // Delete track button
        FrameLayout deleteTrack = holder.constraintLayout.findViewById(R.id.queue_track_delete);
        deleteTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MediaControllerCompat.getMediaController((MainActivity)context).removeQueueItemAt(holder.getAdapterPosition());
                queue.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                onQueueRemovedListener.onQueueRemove();
            }
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
