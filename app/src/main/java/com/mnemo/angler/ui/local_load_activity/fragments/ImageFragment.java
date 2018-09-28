package com.mnemo.angler.ui.local_load_activity.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.activity.LocalLoadActivity;
import com.mnemo.angler.utils.ImageAssistant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ImageFragment extends Fragment {

    @BindView(R.id.image_fragment_image)
    ImageView imageView;

    String image;

    Unbinder unbinder;

    public ImageFragment() {
        // Required empty public constructor
    }

    // Constructor for ImageFragment with arguments
    public static ImageFragment createImageFragment(String image, int position) {

        Bundle args = new Bundle();
        args.putString("image", image);
        args.putInt("position", position);
        ImageFragment imageFragment = new ImageFragment();
        imageFragment.setArguments(args);

        return imageFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_image, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get selected image from arguments
        image = getArguments().getString("image");

        // Assign image to view
        imageView.setTransitionName(getResources().getString(R.string.local_load_image_transition) + getArguments().getInt("position"));
        ImageAssistant.loadImage(getContext(), image, imageView, 400);


        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    // Setup load button
    @OnClick(R.id.image_load)
    void imageLoad(){

        Bundle args = new Bundle();
        args.putString("image", image);

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
                PlaylistImageCropFragment playlistImageCropFragment = new PlaylistImageCropFragment();
                playlistImageCropFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.full_frame, playlistImageCropFragment)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

    // Setup hide button
    @OnClick(R.id.image_hide)
    void imageHide(){
        getActivity().onBackPressed();
    }
}
