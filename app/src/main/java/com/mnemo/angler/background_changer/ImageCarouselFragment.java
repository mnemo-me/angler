package com.mnemo.angler.background_changer;


import android.os.Bundle;

import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.R;

import java.util.ArrayList;



public class ImageCarouselFragment extends Fragment {


    public ImageCarouselFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_image_carousel, container, false);

        /*
        Get arguments
        images = list of images in current folder
        image - selected image
        */
        Bundle args = getArguments();
        final ArrayList<String> images = args.getStringArrayList("images");
        String image = args.getString("image");

        // Initialize TabLayout and bind it with ViewPager
        final TabLayout tabLayout = view.findViewById(R.id.image_carousel_tab);
        ViewPager viewPager = view.findViewById(R.id.image_carousel_view_pager);

        viewPager.setAdapter(new ImageCarouselAdapter(getActivity().getSupportFragmentManager(), images, getArguments().getString("image_type")));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(images.indexOf(image)).select();

        // Animation with disappearing advice text
        final TextView adviceText = view.findViewById(R.id.image_carousel_advice);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adviceText.animate()
                        .alpha(0f)
                        .setDuration(1000);
            }
        },2000);

        viewPager.setScaleX(0f);
        viewPager.setScaleY(0f);
        viewPager.setAlpha(0f);

        viewPager.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }

}
