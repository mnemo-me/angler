package com.mnemo.angler.albums;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.artists.ArtistCoverDialogFragment;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

public class AlbumListAdapter extends ArrayAdapter{

    private Context context;
    private int albumsInLine;
    private ArrayList<ListItem> items;

    AlbumListAdapter(@NonNull Context context, ArrayList<Album> albums) {

        super(context, 0, albums);

        this.context = context;

        int orientation = context.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            albumsInLine = 3;
        }else{
            albumsInLine = 5;
        }



        TreeSet<String> artists = new TreeSet<>();
        for (Album album : albums){
                artists.add(album.getArtist());
        }

        items = new ArrayList<>();
        for (String artist : artists){
            items.add(new HeaderItem(artist));

            ArrayList<Album> artistAlbums = new ArrayList<>();

            for (Album album : albums){
                if (album.getArtist().equals(artist)){
                    artistAlbums.add(album);
                }
            }
            Collections.sort(artistAlbums);

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


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ConstraintLayout constraintLayout;

        if (items.get(position) instanceof  HeaderItem){
            constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.alb_artist, parent, false);
        }else {
            constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.alb_album_line, parent, false);
        }


        if (items.get(position) instanceof HeaderItem){
            ((TextView)constraintLayout.findViewById(R.id.artist_title)).setText(((HeaderItem)items.get(position)).getArtist());
        }else{
            fillAlbumLine(constraintLayout, position);
        }


        return constraintLayout;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private void fillAlbumLine(ConstraintLayout constraintLayout, int position){

        ArrayList<Album> artistAlbums = ((AlbumLine)items.get(position)).getAlbums();


        ArrayList<Integer> albumIdentifier = new ArrayList<>();
        albumIdentifier.add(R.id.album_line_album_one);
        albumIdentifier.add(R.id.album_line_album_two);
        albumIdentifier.add(R.id.album_line_album_three);
        albumIdentifier.add(R.id.album_line_album_four);
        albumIdentifier.add(R.id.album_line_album_five);

        ArrayList<Integer> albumImageIdentifier = new ArrayList<>();
        albumImageIdentifier.add(R.id.album_line_album_one_image);
        albumImageIdentifier.add(R.id.album_line_album_two_image);
        albumImageIdentifier.add(R.id.album_line_album_three_image);
        albumImageIdentifier.add(R.id.album_line_album_four_image);
        albumImageIdentifier.add(R.id.album_line_album_five_image);

        ArrayList<Integer> albumTextIdentifier = new ArrayList<>();
        albumTextIdentifier.add(R.id.album_line_album_one_name);
        albumTextIdentifier.add(R.id.album_line_album_two_name);
        albumTextIdentifier.add(R.id.album_line_album_three_name);
        albumTextIdentifier.add(R.id.album_line_album_four_name);
        albumTextIdentifier.add(R.id.album_line_album_five_name);


        for (int i = 0; i < albumsInLine; i ++) {

            if (artistAlbums.size() > i) {

                final ImageView imageOne = constraintLayout.findViewById(albumImageIdentifier.get(i));

                final String album = artistAlbums.get(i).getAlbum();
                final String artist = artistAlbums.get(i).getArtist();
                final String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

                imageOne.setTransitionName(artist + " " + album);

                ImageAssistant.loadImage(context,
                        albumImagePath,
                        imageOne, 125);

                TextView textOne = constraintLayout.findViewById(albumTextIdentifier.get(i));
                textOne.setText(album);

                constraintLayout.findViewById(albumIdentifier.get(i)).setOnClickListener(view -> {

                    AlbumConfigurationFragment albumDetailFragment = new AlbumConfigurationFragment();
                    Bundle args = new Bundle();

                    args.putString("type", "album");
                    args.putString("image", albumImagePath);
                    args.putString("album_name", album);
                    args.putString("artist", artist);

                    albumDetailFragment.setArguments(args);

                    albumDetailFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));

                    albumDetailFragment.setEnterTransition(new Fade().setStartDelay(300));
                    albumDetailFragment.setReturnTransition(null);


                    ImageView back = ((MainActivity)context).findViewById(R.id.albums_drawer_back);

                    FragmentTransaction fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();


                    fragmentTransaction
                            .addSharedElement(back, "back")
                            .replace(R.id.frame, albumDetailFragment)
                            .addToBackStack(null)
                            .commit();
                });


                constraintLayout.findViewById(albumIdentifier.get(i)).setOnLongClickListener(view -> {

                    ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

                    Bundle args = new Bundle();
                    args.putString("artist", artist);
                    args.putString("album", album);
                    args.putString("image", albumImagePath);
                    artistCoverDialogFragment.setArguments(args);

                    artistCoverDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "Album cover fragment");

                    return true;
                });

            }else{
                constraintLayout.findViewById(albumIdentifier.get(i)).setVisibility(View.INVISIBLE);
            }
        }



    }


    private class ListItem{

    }

    private class HeaderItem extends ListItem{

        private String artist;

        HeaderItem(String artist) {
            this.artist = artist;
        }

        public String getArtist() {
            return artist;
        }
    }


    private class AlbumLine extends ListItem{

        private ArrayList<Album> albums;

        AlbumLine(ArrayList<Album> albums) {
            this.albums = albums;
        }

        public ArrayList<Album> getAlbums() {
            return albums;
        }
    }


}
