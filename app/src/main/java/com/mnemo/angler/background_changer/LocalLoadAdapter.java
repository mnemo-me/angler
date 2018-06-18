package com.mnemo.angler.background_changer;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.File;
import java.util.ArrayList;

public class LocalLoadAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> imageFolders;
    private String imageType;

    LocalLoadAdapter(FragmentManager fm, ArrayList<String> imageFolders, String imageType) {
        super(fm);
        this.imageFolders = imageFolders;
        this.imageType = imageType;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFolderFragment.createImageFolderFragment(imageFolders.get(position), imageType);
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
