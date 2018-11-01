package com.mnemo.angler.ui.local_load_activity.fragments.image_carousel;


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
import com.mnemo.angler.ui.local_load_activity.activity.LocalLoadActivity;
import com.mnemo.angler.ui.local_load_activity.adapters.ImageCarouselAdapter;
import com.mnemo.angler.ui.local_load_activity.fragments.cover_crop.CoverCropFragment;
import com.mnemo.angler.ui.local_load_activity.fragments.portrait_crop.PortraitCropFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ImageCarouselFragment extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.image_carousel_tab)
    TabLayout tabLayout;

    @BindView(R.id.image_carousel_view_pager)
    ViewPager viewPager;

    ArrayList<String> images;
    String image;

    public ImageCarouselFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_image_carousel, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        /*
        Get arguments
        images - list of images in current folder
        image - selected image
        imageType - tag to recognize starter fragment
        */
        Bundle args = getArguments();
        images = args.getStringArrayList("images");
        image = args.getString("image");


        // Bind TabLayout with ViewPager
        viewPager.setAdapter(new ImageCarouselAdapter(getActivity().getSupportFragmentManager(), images));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(images.indexOf(image)).select();


        // Show toast with navigation advice
        Toast.makeText(getContext(), R.string.image_carousel_advice_text, Toast.LENGTH_SHORT).show();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Setup select button
    @OnClick(R.id.image_carousel_select)
    void imageSelect(){

        Bundle args = new Bundle();
        args.putString("image", images.get(tabLayout.getSelectedTabPosition()));

        String imageType = ((LocalLoadActivity)getActivity()).getImageType();

        switch (imageType){

            case "background":

                PortraitCropFragment portraitCropFragment = new PortraitCropFragment();
                portraitCropFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.full_frame, portraitCropFragment)
                        .addToBackStack(null)
                        .commit();
                break;

            case "cover":

                CoverCropFragment coverCropFragment = new CoverCropFragment();
                coverCropFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.full_frame, coverCropFragment)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

    // Setup hide button
    @OnClick(R.id.image_carousel_close)
    void imageHide(){
        getActivity().onBackPressed();
    }
}
