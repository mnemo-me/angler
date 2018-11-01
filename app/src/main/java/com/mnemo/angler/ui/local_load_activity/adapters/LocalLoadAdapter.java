package com.mnemo.angler.ui.local_load_activity.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mnemo.angler.ui.local_load_activity.fragments.image_folder.ImageFolderFragment;

import java.io.File;
import java.util.List;

public class LocalLoadAdapter extends FragmentStatePagerAdapter {

    private List<String> imageFolders;

    public LocalLoadAdapter(FragmentManager fm, List<String> imageFolders) {
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
