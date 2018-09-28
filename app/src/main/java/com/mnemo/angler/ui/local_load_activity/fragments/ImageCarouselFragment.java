package com.mnemo.angler.ui.local_load_activity.fragments;


import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.adapters.ImageCarouselAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ImageCarouselFragment extends Fragment {


    @BindView(R.id.image_carousel_tab)
    TabLayout tabLayout;

    @BindView(R.id.image_carousel_view_pager)
    ViewPager viewPager;

    Unbinder unbinder;

    public ImageCarouselFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_image_carousel, container, false);

        unbinder = ButterKnife.bind(this, view);

        /*
        Get arguments
        images - list of images in current folder
        image - selected image
        imageType - tag to recognize starter fragment
        */
        Bundle args = getArguments();
        ArrayList<String> images = args.getStringArrayList("images");
        String image = args.getString("image");



        // Bind TabLayout with ViewPager
        viewPager.setAdapter(new ImageCarouselAdapter(getActivity().getSupportFragmentManager(), images));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(images.indexOf(image)).select();

        // Image appear animation
        viewPager.setScaleX(0f);
        viewPager.setScaleY(0f);
        viewPager.setAlpha(0f);

        viewPager.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f);


        // Show toast with navigation advice
        Toast.makeText(getContext(), R.string.image_carousel_advice_text, Toast.LENGTH_SHORT).show();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
