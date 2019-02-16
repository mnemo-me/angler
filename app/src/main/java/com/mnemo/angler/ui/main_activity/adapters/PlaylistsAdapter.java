package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private Context context;
    private List<Playlist> playlists;

    private OnPlaylistClickListener onPlaylistClickListener;
    private OnPlaylistLongClickListener onPlaylistLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.playlist_image)
        ImageView playlistImage;

        @BindView(R.id.playlist_title)
        TextView playlistTitle;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(String title, String cover);
    }

    public interface OnPlaylistLongClickListener {
        void onPlaylistLongClick(String title, String cover);
    }

    public PlaylistsAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    public PlaylistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_v2, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        // Setup listeners
        viewHolder.itemView.setOnClickListener(v -> {

            // Get playlist data
            String title = playlists.get(viewHolder.getAdapterPosition()).getTitle();
            String cover = playlists.get(viewHolder.getAdapterPosition()).getCover();

            onPlaylistClickListener.onPlaylistClick(title, cover);
        });

        viewHolder.itemView.setOnLongClickListener(v -> {

            // Get playlist data
            String title = playlists.get(viewHolder.getAdapterPosition()).getTitle();
            String cover = playlists.get(viewHolder.getAdapterPosition()).getCover();

            onPlaylistLongClickListener.onPlaylistLongClick(title, cover);

            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistsAdapter.ViewHolder holder, int position) {

        // Get playlist data
        String title = playlists.get(position).getTitle();
        String cover = playlists.get(position).getCover();

        // Assign data to views
        holder.playlistTitle.setText(title);
        ImageAssistant.loadImage(context, cover, holder.playlistImage, 125);

    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }


    // Setter for listeners
    public void setOnPlaylistClickListener(OnPlaylistClickListener onPlaylistClickListener) {
        this.onPlaylistClickListener = onPlaylistClickListener;
    }

    public void setOnPlaylistLongClickListener(OnPlaylistLongClickListener onPlaylistLongClickListener) {
        this.onPlaylistLongClickListener = onPlaylistLongClickListener;
    }
}
