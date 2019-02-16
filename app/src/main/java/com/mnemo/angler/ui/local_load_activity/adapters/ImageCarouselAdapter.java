package com.mnemo.angler.ui.local_load_activity.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mnemo.angler.ui.local_load_activity.fragments.image.ImageFragment;

import java.util.ArrayList;

public class ImageCarouselAdapter extends FragmentStatePagerAdapter{

    private ArrayList<String> images;

    public ImageCarouselAdapter(FragmentManager fm, ArrayList<String> images) {
        super(fm);
        this.images = images;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.createImageFragment(images.get(position), position);
    }

    @Override
    public int getCount() {
        return images.size();
    }
}
