package com.mnemo.angler.ui.main_activity.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums.ArtistAlbumsFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio.ArtistBioFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks.ArtistTracksFragment;


public class ArtistTabsAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private String[] tabs = {"tracks", "albums", "bio"};
    private String artist;
    private int tracksCount;
    private int albumsCount;
    private int orientation;

    public ArtistTabsAdapter(FragmentManager fm, Context context, String artist, int tracksCount, int albumsCount, int orientation) {
        super(fm);
        this.context = context;
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

        switch (tabs[position]){

            case "tracks":

                return context.getString(R.string.tracks) + " " + tracksCount;

            case "albums":

                return context.getString(R.string.albums) + ": " + albumsCount;

            case "bio":

                return context.getString(R.string.bio);

            default:

                return null;
        }
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
