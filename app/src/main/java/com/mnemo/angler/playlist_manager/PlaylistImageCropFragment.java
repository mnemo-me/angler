package com.mnemo.angler.playlist_manager;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlaylistImageCropFragment extends Fragment {

    @BindView(R.id.fragment_playlist_image_crop_crop_iwa)
    CropIwaView cropIwaView;

    String image;

    Unbinder unbinder;

    public PlaylistImageCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_image_crop, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get image and new image name from arguments
        image = getArguments().getString("image");

        // Setup CropIwa
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    /*
     Setup crop button
     Cropping image in CropIwa borders
      */
    @OnClick(R.id.fragment_playlist_image_crop_crop)
    void crop(){

        // Get image path
        String imageName  = ((PlaylistOptionsFragment)getActivity().getSupportFragmentManager().findFragmentByTag("playlist_opt_fragment")).getImage();

        File destinationFile = new File(imageName);

        try {
            destinationFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        cropIwaView.crop(new CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build());

        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().popBackStack();
    }

    // Setup back button
    @OnClick(R.id.fragment_playlist_image_crop_back)
    void back(){
        getActivity().onBackPressed();
    }
}
