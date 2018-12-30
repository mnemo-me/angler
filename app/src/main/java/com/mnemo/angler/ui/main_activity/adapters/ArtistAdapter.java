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
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder>{

    private Context context;
    private List<String> artists;

    private OnArtistClickListener onArtistClickListener;
    private OnArtistLongClickListener onArtistLongClickListener;

    public interface OnArtistClickListener{
        void onArtistClick(String artist, String image);
    }

    public interface OnArtistLongClickListener{
        void onArtistLongClick(String artist, String image);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.artist_image)
        ImageView imageView;

        @BindView(R.id.artist_artist)
        TextView titleView;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public ArtistAdapter(Context context, List<String> artists) {
        this.context = context;
        this.artists = artists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.art_artist, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        // Set listeners
        viewHolder.itemView.setOnClickListener(v -> {

            // Get artist variables
            String artist = artists.get(viewHolder.getAdapterPosition());
            String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

            onArtistClickListener.onArtistClick(artist, image);
        });

        viewHolder.itemView.setOnLongClickListener(v -> {

            // Get artist variables
            String artist = artists.get(viewHolder.getAdapterPosition());
            String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

            onArtistLongClickListener.onArtistLongClick(artist, image);

            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Get artist variables
        String artist = artists.get(position);
        String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

        // Fill views
        holder.titleView.setText(artist);
        ImageAssistant.loadImage(context, image, holder.imageView, 200);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    // Setters for listeners

    public void setOnArtistClickListener(OnArtistClickListener onArtistClickListener) {
        this.onArtistClickListener = onArtistClickListener;
    }

    public void setOnArtistLongClickListener(OnArtistLongClickListener onArtistLongClickListener) {
        this.onArtistLongClickListener = onArtistLongClickListener;
    }
}
