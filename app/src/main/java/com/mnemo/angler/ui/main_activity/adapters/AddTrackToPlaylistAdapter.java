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

    private static final int CREATE_NEW_PLAYLIST_VIEW_TYPE = 0;
    private static final int PLAYLIST_VIEW_TYPE = 1;


    static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class CreateNewPlaylistViewHolder extends ViewHolder{

        CreateNewPlaylistViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class PlaylistViewHolder extends ViewHolder{

        @BindView(R.id.playlist_image)
        ImageView coverView;

        @BindView(R.id.playlist_title)
        TextView titleView;

        PlaylistViewHolder(View itemView) {
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

        if (viewType == CREATE_NEW_PLAYLIST_VIEW_TYPE){

            View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_create_new, parent, false);
            return new CreateNewPlaylistViewHolder(view);

        }else{

            View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_v2, parent, false);
            return new PlaylistViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String title;

        if (holder.getItemViewType() == CREATE_NEW_PLAYLIST_VIEW_TYPE){

            title = context.getResources().getString(R.string.create_new_playlist);

        }else{

            // Get playlist variables
            String cover;

            title = playlists.get(position - 1).getTitle();
            cover = playlists.get(position - 1).getCover();


            // Fill views
            ((PlaylistViewHolder)holder).titleView.setText(title);
            ImageAssistant.loadImage(context, cover, ((PlaylistViewHolder)holder).coverView, 125);

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

        }

        // Listener
        holder.itemView.setOnClickListener(view -> onAddTrackToPlaylistListener.trackAdded(title));

    }

    @Override
    public int getItemCount() {
        return playlists.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0){
            return CREATE_NEW_PLAYLIST_VIEW_TYPE;
        }else{
            return PLAYLIST_VIEW_TYPE;
        }
    }

    public void setOnAddTrackToPlaylistListener(OnAddTrackToPlaylistListener onAddTrackToPlaylistListener) {
        this.onAddTrackToPlaylistListener = onAddTrackToPlaylistListener;
    }
}
