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
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistAlbumAdapter extends RecyclerView.Adapter<ArtistAlbumAdapter.ViewHolder>{

    private Context context;
    private String artist;
    private List<Album> albums;

    private OnAlbumClickListener onAlbumClickListener;
    private OnAlbumLongClickListener onAlbumLongClickListener;


    public interface OnAlbumClickListener{
        void onAlbumClick(String artist, String album, int year);
    }

    public interface OnAlbumLongClickListener{
        void onAlbumLongClick(String artist, String album, int year);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.playlist_image)
        ImageView coverView;

        @BindView(R.id.playlist_title)
        TextView titleView;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public ArtistAlbumAdapter(Context context, String artist, List<Album> albums) {
        this.context = context;
        this.artist = artist;
        this.albums = albums;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.pm_playlist_v2, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        // Setup listeners
        // Open albums configuration fragment
        viewHolder.itemView.setOnClickListener(v -> {

            // Get album variables
            String album = albums.get(viewHolder.getAdapterPosition()).getAlbum();
            int year = albums.get(viewHolder.getAdapterPosition()).getYear();

            onAlbumClickListener.onAlbumClick(artist, album, year);
        });

        // Open cover fragment
        viewHolder.itemView.setOnLongClickListener(v -> {

            // Get album variables
            String album = albums.get(viewHolder.getAdapterPosition()).getAlbum();
            int year = albums.get(viewHolder.getAdapterPosition()).getYear();

            onAlbumLongClickListener.onAlbumLongClick(artist, album, year);

            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Get album variables
        String album = albums.get(position).getAlbum();

        // Create album image path
        String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

        // Load album image
        if (!new File(albumImagePath).exists()){
            albumImagePath = "R.drawable.black_logo";
        }

        ImageAssistant.loadImage(context, albumImagePath, holder.coverView, 125);

        // Set album title
        holder.titleView.setText(album);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    // Setters for listeners
    public void setOnAlbumClickListener(OnAlbumClickListener onAlbumClickListener) {
        this.onAlbumClickListener = onAlbumClickListener;
    }

    public void setOnAlbumLongClickListener(OnAlbumLongClickListener onAlbumLongClickListener) {
        this.onAlbumLongClickListener = onAlbumLongClickListener;
    }
}
