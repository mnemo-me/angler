package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.classes.Album;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistAlbumAdapter extends RecyclerView.Adapter<ArtistAlbumAdapter.ViewHolder>{

    private Context context;
    private String artist;
    private List<Album> albums;

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

            // Create album image path
            String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

            AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("type", "album");
            args.putString("image", albumImagePath);
            args.putString("album_name", album);
            args.putString("artist", artist);
            albumConfigurationFragment.setArguments(args);

            ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, albumConfigurationFragment, "album_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });

        // Open cover fragment
        viewHolder.itemView.setOnLongClickListener(v -> {

            // Get album variables
            String album = albums.get(viewHolder.getAdapterPosition()).getAlbum();

            // Create album image path
            String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

            CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("album", album);
            args.putString("image", albumImagePath);
            coverDialogFragment.setArguments(args);

            coverDialogFragment.show(((MainActivity) context).getSupportFragmentManager(), "album_cover_fragment");

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
        ImageAssistant.loadImage(context, albumImagePath, holder.coverView, 125);

        // Set album title
        holder.titleView.setText(album);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
