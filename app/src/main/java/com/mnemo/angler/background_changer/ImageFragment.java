package com.mnemo.angler.background_changer;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.R;
import com.mnemo.angler.playlist_manager.PlaylistImageCropFragment;


public class ImageFragment extends Fragment {


    public ImageFragment() {
        // Required empty public constructor
    }

    // Constructor for ImageFragment with arguments
    public static ImageFragment createImageFragment(String image, String imageType, int position) {

        Bundle args = new Bundle();
        args.putString("image", image);
        args.putString("image_type", imageType);
        args.putInt("position", position);
        ImageFragment imageFragment = new ImageFragment();
        imageFragment.setArguments(args);

        return imageFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_image, container, false);

        // Get selected image from arguments
        Bundle args = getArguments();
        final String image = args.getString("image");

        ImageView imageView = view.findViewById(R.id.image_fragment_image);
        imageView.setTransitionName(getResources().getString(R.string.local_load_image_transition) + getArguments().getInt("position"));

        ImageAssistent.loadImage(getContext(), image, imageView, 400);

        // Setup cancel button
        final ImageView cancel = view.findViewById(R.id.image_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        // Setup load button
        final ImageView load = view.findViewById(R.id.image_load);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle args = new Bundle();
                args.putString("image", image);

                switch (getArguments().getString("image_type")){

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
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }

}
