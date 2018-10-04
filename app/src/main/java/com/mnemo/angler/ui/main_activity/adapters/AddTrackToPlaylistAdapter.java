package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.util.ImageAssistant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTrackToPlaylistAdapter extends RecyclerView.Adapter<AddTrackToPlaylistAdapter.ViewHolder>{

    public interface OnAddTrackToPlaylistListener{
        void trackAdded(String playlist);
    }

    private Context context;
    private List<Playlist> playlists;
    private List<String> playlistsWithTrack;

    private OnAddTrackToPlaylistListener onAddTrackToPlaylistListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.playlist_image)
        ImageView coverView;

        @BindView(R.id.playlist_title)
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public AddTrackToPlaylistAdapter(Context context, List<Playlist> playlists, List<String> playlistsWithTrack) {
        this.context = context;
        this.playlists = playlists;
        this.playlistsWithTrack = playlistsWithTrack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_v2_mod, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Get playlist variables
        String title = playlists.get(position).getTitle();
        String cover = playlists.get(position).getCover();

        // Fill views
        holder.titleView.setText(title);
        ImageAssistant.loadImage(context, cover, holder.coverView, 125);

        // Disable playlists with track
        if (playlistsWithTrack.contains(title)){
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
        }else{
            if (!holder.itemView.isEnabled()){
                holder.itemView.setEnabled(true);
                holder.itemView.setAlpha(1f);
            }
        }

        // Listener
        holder.itemView.setOnClickListener(view -> onAddTrackToPlaylistListener.trackAdded(title));

    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void setOnAddTrackToPlaylistListener(OnAddTrackToPlaylistListener onAddTrackToPlaylistListener) {
        this.onAddTrackToPlaylistListener = onAddTrackToPlaylistListener;
    }
}
