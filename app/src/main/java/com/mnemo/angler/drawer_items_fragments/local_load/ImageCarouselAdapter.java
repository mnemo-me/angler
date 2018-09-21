package com.mnemo.angler.drawer_items_fragments.local_load;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ImageCarouselAdapter extends FragmentStatePagerAdapter{

    private ArrayList<String> images;

    ImageCarouselAdapter(FragmentManager fm, ArrayList<String> images) {
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
