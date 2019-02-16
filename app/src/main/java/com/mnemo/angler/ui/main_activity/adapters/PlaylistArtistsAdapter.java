package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;

import java.util.List;


public class PlaylistArtistsAdapter extends RecyclerView.Adapter<PlaylistArtistsAdapter.ViewHolder>{

    private Context context;
    private List<String> artists;
    private String selectedArtist = null;

    private OnArtistSelectedListener listener;

    public interface OnArtistSelectedListener{
        void onArtistSelected(String artist);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public PlaylistArtistsAdapter(Context context, List<String> artists) {
        this.context = context;
        this.artists = artists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.mp_artist_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        // Setup listener
        viewHolder.itemView.setOnClickListener(v -> {

            String artist = artists.get(viewHolder.getAdapterPosition());

            listener.onArtistSelected(artist);
            setArtist(artist);
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String artist = artists.get(position);

        ((TextView)holder.itemView).setText(artist);

        if (artist.equals(selectedArtist)){
            holder.itemView.setSelected(true);
        }else{
            if (holder.itemView.isSelected()){
                holder.itemView.setSelected(false);
            }
        }

    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public void setArtist(String artist) {
        selectedArtist = artist;
        notifyDataSetChanged();
    }

    public void setOnArtistSelectedListener(OnArtistSelectedListener listener) {
        this.listener = listener;
    }


}
