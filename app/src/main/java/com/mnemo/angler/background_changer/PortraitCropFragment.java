package com.mnemo.angler.background_changer;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerFolder;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;
import java.io.IOException;


public class PortraitCropFragment extends Fragment {


    public PortraitCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_portrait_crop, container, false);

        // Get image from arguments
        final String image = getArguments().getString("image");

        // Setup CropIwa
        final CropIwaView cropIwaView = view.findViewById(R.id.fragment_portrait_crop_iwa);
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        // Setup back button
        TextView back = view.findViewById(R.id.fragment_portrait_back);
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
        TextView crop = view.findViewById(R.id.fragment_portrait_crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newImageName = getNewImageName(image);

                File destinationFile = new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT,
                        newImageName);

                try {
                    destinationFile.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                cropIwaView.crop(new CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .build());

                LandscapeCropFragment landscapeCropFragment = new LandscapeCropFragment();
                Bundle args = new Bundle();
                args.putString("image", image);
                args.putString("new_image_name", newImageName);
                landscapeCropFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.full_frame, landscapeCropFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    // Generate new name for image file
    private String getNewImageName(String image){

        String newImageName = new File(image).getName();
        newImageName = newImageName.replace("R.drawable.","d");
        newImageName = newImageName.substring(0, newImageName.lastIndexOf("."));

        if (new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT,newImageName + ".jpeg").exists()){

            int count = 2;
            while (new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT, newImageName + "(" + count + ").jpeg").exists()){
                 count++;
            }
            newImageName = newImageName + "(" + count + ")";
        }

        newImageName = newImageName + ".jpeg";

        return newImageName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }

}
