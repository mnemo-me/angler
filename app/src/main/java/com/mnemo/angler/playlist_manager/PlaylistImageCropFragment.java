package com.mnemo.angler.playlist_manager;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;
import java.io.IOException;


public class PlaylistImageCropFragment extends Fragment {


    private static final int LOADER_UPDATE_COVER_ID = 1;

    public PlaylistImageCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_image_crop, container, false);

        // Get image and new image name from arguments
        final String image = getArguments().getString("image");

        // Setup CropIwa
        final CropIwaView cropIwaView = view.findViewById(R.id.fragment_playlist_image_crop_crop_iwa);
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        // Setup back button
        ImageView back = view.findViewById(R.id.fragment_playlist_image_crop_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        /*
         Setup crop button
         Cropping image in CropIwa borders
          */
        ImageView crop = view.findViewById(R.id.fragment_playlist_image_crop_crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }

}
