package com.mnemo.angler.artists;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;



public class ArtistTabsAdapter extends FragmentStatePagerAdapter {

    private String[] tabs = {"tracks", "albums", "bio"};
    private String artist;
    private int tracksCount;
    private int albumsCount;
    private int orientation;

    ArtistTabsAdapter(FragmentManager fm, String artist, int tracksCount, int albumsCount, int orientation) {
        super(fm);
        this.artist = artist;
        this.tracksCount = tracksCount;
        this. albumsCount = albumsCount;
        this.orientation = orientation;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle args = new Bundle();
        args.putString("artist", artist);

        switch (tabs[position]){

            case "tracks":

                ArtistTracksFragment artistTracksFragment = new ArtistTracksFragment();
                artistTracksFragment.setArguments(args);

                return artistTracksFragment;

            case "albums":

                ArtistAlbumsFragment artistAlbumsFragment = new ArtistAlbumsFragment();
                artistAlbumsFragment.setArguments(args);

                return artistAlbumsFragment;

            case "bio":

                ArtistBioFragment artistBioFragment = new ArtistBioFragment();
                artistBioFragment.setArguments(args);

                return artistBioFragment;

            default:

                return null;
        }


    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        String lineShift = "";

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            lineShift = "\n";
        }

        switch (tabs[position]){

            case "tracks":

                return tabs[position] + ": " + lineShift + tracksCount;

            case "albums":

                return tabs[position] + ": " + lineShift + albumsCount;

            case "bio":

                return tabs[position] + lineShift;

            default:

                return null;
        }
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
