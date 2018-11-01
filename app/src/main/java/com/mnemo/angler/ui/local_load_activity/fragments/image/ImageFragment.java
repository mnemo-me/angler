package com.mnemo.angler.ui.local_load_activity.fragments.image;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.mnemo.angler.util.ImageAssistant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ImageFragment extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.image_fragment_image)
    ImageView imageView;

    String image;

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

        // Inject views
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

}
