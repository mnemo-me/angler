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
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration.ArtistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder>{

    private Context context;
    private List<String> artists;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.artist_image)
        ImageView imageView;

        @BindView(R.id.artist_artist)
        TextView titleView;

        public ViewHolder(View itemView) {
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
            String title = artists.get(viewHolder.getAdapterPosition());
            String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + title + ".jpg";

            ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("artist", title);
            args.putString("image", image);
            artistConfigurationFragment.setArguments(args);

            ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, artistConfigurationFragment, "artist_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });

        viewHolder.itemView.setOnLongClickListener(v -> {

            // Get artist variables
            String title = artists.get(viewHolder.getAdapterPosition());
            String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + title + ".jpg";

            CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", title);
            args.putString("image", image);
            coverDialogFragment.setArguments(args);

            coverDialogFragment.show(((MainActivity)context).getSupportFragmentManager(), "cover_dialog_fragment");

            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Get artist variables
        String title = artists.get(position);
        String image = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + title + ".jpg";

        // Fill views
        holder.titleView.setText(title);
        ImageAssistant.loadImage(context, image, holder.imageView, 200);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }
}
