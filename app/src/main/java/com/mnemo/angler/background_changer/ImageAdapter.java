package com.mnemo.angler.background_changer;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mnemo.angler.R;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter{

    ArrayList<String> images;

    public ImageAdapter(ArrayList<String> images) {
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {


        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bg_background_item, null);

        ImageView imageView = relativeLayout.findViewById(R.id.background);
        ImageAssistent.loadImage(viewGroup.getContext(), images.get(i), imageView, 260);

        return relativeLayout;
    }
}
