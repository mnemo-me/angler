package com.mnemo.angler.background_changer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ImageCarouselAdapter extends FragmentStatePagerAdapter{

    private ArrayList<String> images;
    private String imageType;

    ImageCarouselAdapter(FragmentManager fm, ArrayList<String> images, String imageType) {
        super(fm);
        this.images = images;
        this.imageType = imageType;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.createImageFragment(images.get(position), imageType, position);
    }

    @Override
    public int getCount() {
        return images.size();
    }
}
