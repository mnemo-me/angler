package com.mnemo.angler.local_load;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.util.ArrayList;

public class LocalLoadAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> imageFolders;

    public LocalLoadAdapter(FragmentManager fm, ArrayList<String> imageFolders) {
        super(fm);
        this.imageFolders = imageFolders;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFolderFragment.createImageFolderFragment(imageFolders.get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        File file = new File(imageFolders.get(position));
        return file.getName();
    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }
}
