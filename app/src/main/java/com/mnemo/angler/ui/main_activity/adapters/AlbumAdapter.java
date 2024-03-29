package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{

    private Context context;
    private int albumsInLine;
    private int imageHeight;

    private ArrayList<ListItem> items;
    private static final int ARTIST_HEADER_VIEW_TYPE = 0;
    private static final int ALBUMS_LINE_VIEW_TYPE = 1;

    private OnAlbumClickListener onAlbumClickListener;
    private OnAlbumLongClickListener onAlbumLongClickListener;


    public interface OnAlbumClickListener{
        void onAlbumClick(String artist, String album, int year);
    }

    public interface OnAlbumLongClickListener{
        void onAlbumLongClick(String artist, String album, int year);
    }

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albumsInLine = context.getResources().getInteger(R.integer.playlist_column_count);

        imageHeight = context.getResources().getConfiguration().screenWidthDp / albumsInLine;

        // Get artists
        TreeSet<String> artists = new TreeSet<>();
        for (Album album : albums){
            artists.add(album.getArtist());
        }

        // Fill items list with artist headers and album lines
        items = new ArrayList<>();
        for (String artist : artists){

            items.add(new HeaderItem(artist));

            ArrayList<Album> artistAlbums = new ArrayList<>();

            for (Album album : albums){
                if (album.getArtist().equals(artist)){
                    artistAlbums.add(album);
                }
            }

            int maxAlbumsLines = (int)Math.ceil((double)artistAlbums.size() / albumsInLine);


            for (int i = 0; i < maxAlbumsLines; i++){

                ArrayList<Album> albumLine = new ArrayList<>();

                int max = albumsInLine;

                if (i == maxAlbumsLines - 1){
                    if (artistAlbums.size() % albumsInLine != 0) {
                        max = artistAlbums.size() % albumsInLine;
                    }
                }

                for (int y = 0; y < max; y ++){
                    albumLine.add(artistAlbums.get(i * albumsInLine + y));
                }
                items.add(new AlbumLine(albumLine));
            }
        }
    }

    abstract static class ViewHolder extends RecyclerView.ViewHolder{
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ArtistHolder extends ViewHolder{

        @BindView(R.id.artist_title)
        TextView artistView;

        ArtistHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    static class AlbumsLineHolder extends ViewHolder{

        @Nullable
        @BindViews({R.id.album_line_album_one, R.id.album_line_album_two, R.id.album_line_album_three,
                R.id.album_line_album_four, R.id.album_line_album_five})
        List<View> albumsView;

        @Nullable
        @BindViews({R.id.album_line_album_one_image, R.id.album_line_album_two_image, R.id.album_line_album_three_image,
                R.id.album_line_album_four_image, R.id.album_line_album_five_image})
        List<ImageView> albumsImageView;

        @Nullable
        @BindViews({R.id.album_line_album_one_name, R.id.album_line_album_two_name, R.id.album_line_album_three_name,
                R.id.album_line_album_four_name, R.id.album_line_album_five_name})
        List<TextView> albumsTitleView;


        AlbumsLineHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ARTIST_HEADER_VIEW_TYPE){

            View view = LayoutInflater.from(context).inflate(R.layout.alb_artist, parent, false);
            return new ArtistHolder(view);

        }else{

            View view = LayoutInflater.from(context).inflate(R.layout.alb_album_line, parent, false);
            AlbumsLineHolder albumsLineHolder = new AlbumsLineHolder(view);

            // Setup listeners

            for (int i = 0; i < albumsInLine; i ++) {

                // Open albums configuration fragment

                int finalI = i;
                
                albumsLineHolder.albumsView.get(i).setOnClickListener(v -> {

                    ArrayList<Album> artistAlbums = ((AlbumLine)items.get(albumsLineHolder.getAdapterPosition())).getAlbums();

                    // Get album variables
                    String artist = artistAlbums.get(finalI).getArtist();
                    String album = artistAlbums.get(finalI).getAlbum();
                    int year = artistAlbums.get(finalI).getYear();

                    onAlbumClickListener.onAlbumClick(artist, album, year);
                });

                // Open cover fragment
                albumsLineHolder.albumsView.get(i).setOnLongClickListener(v -> {

                    ArrayList<Album> artistAlbums = ((AlbumLine)items.get(albumsLineHolder.getAdapterPosition())).getAlbums();

                    // Get album variables
                    String artist = artistAlbums.get(finalI).getArtist();
                    String album = artistAlbums.get(finalI).getAlbum();
                    int year = artistAlbums.get(finalI).getYear();

                    onAlbumLongClickListener.onAlbumLongClick(artist, album, year);

                    return true;
                });
            }

            return albumsLineHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (holder.getItemViewType() == ARTIST_HEADER_VIEW_TYPE){

            ((ArtistHolder)holder).artistView.setText(((HeaderItem)items.get(position)).getArtist());

        }else{

            ArrayList<Album> artistAlbums = ((AlbumLine)items.get(position)).getAlbums();

            for (int i = 0; i < albumsInLine; i ++) {

                if (artistAlbums.size() > i) {

                    // Set album visible
                    ((AlbumsLineHolder)holder).albumsView.get(i).setVisibility(View.VISIBLE);
                    ((AlbumsLineHolder)holder).albumsTitleView.get(i).setVisibility(View.VISIBLE);

                    // Get album variables
                    String album = artistAlbums.get(i).getAlbum();
                    String artist = artistAlbums.get(i).getArtist();

                    // Create album image path
                    String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

                    if (!new File(albumImagePath).exists()){
                        albumImagePath = "R.drawable.album_default";
                    }

                    // Load album image
                    ImageAssistant.loadImage(context,
                            albumImagePath,
                            ((AlbumsLineHolder)holder).albumsImageView.get(i),
                            imageHeight);

                    // Set album title
                    ((AlbumsLineHolder) holder).albumsTitleView.get(i).setText(album);


                }else{

                    // Set album invisible
                    ((AlbumsLineHolder)holder).albumsView.get(i).setVisibility(View.INVISIBLE);
                    ((AlbumsLineHolder)holder).albumsTitleView.get(i).setVisibility(View.INVISIBLE);
                }
            }

        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (items.get(position) instanceof HeaderItem){
            return ARTIST_HEADER_VIEW_TYPE;
        }else{
            return ALBUMS_LINE_VIEW_TYPE;
        }
    }

    // Item classes
    private class ListItem{

    }

    private class HeaderItem extends ListItem{

        private String artist;

        HeaderItem(String artist) {
            this.artist = artist;
        }

        String getArtist() {
            return artist;
        }
    }


    private class AlbumLine extends ListItem{

        private ArrayList<Album> albums;

        AlbumLine(ArrayList<Album> albums) {
            this.albums = albums;
        }

        ArrayList<Album> getAlbums() {
            return albums;
        }
    }


    // Setters for listeners
    public void setOnAlbumClickListener(OnAlbumClickListener onAlbumClickListener) {
        this.onAlbumClickListener = onAlbumClickListener;
    }

    public void setOnAlbumLongClickListener(OnAlbumLongClickListener onAlbumLongClickListener) {
        this.onAlbumLongClickListener = onAlbumLongClickListener;
    }
}
