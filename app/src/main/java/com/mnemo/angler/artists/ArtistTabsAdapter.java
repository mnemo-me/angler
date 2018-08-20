package com.mnemo.angler.artists;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;



public class ArtistTabsAdapter extends FragmentStatePagerAdapter {

    private String[] tabs = {"tracks", "albums", "bio"};
    private String artist;

    ArtistTabsAdapter(FragmentManager fm, String artist) {
        super(fm);
        this.artist = artist;
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
        return tabs[position];
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
