package com.mnemo.angler.ui.local_load_activity.fragments;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PortraitCropFragment extends Fragment {

    @BindView(R.id.fragment_portrait_crop_iwa)
    CropIwaView cropIwaView;


    String image;

    Unbinder unbinder;


    public PortraitCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_portrait_crop, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get image from arguments
        image = getArguments().getString("image");

        // Setup CropIwa
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    /*
        Setup crop button
        Cropping image in CropIwa borders
          */
    @OnClick(R.id.fragment_portrait_crop)
    void crop(){

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

    // Setup back button
    @OnClick(R.id.fragment_portrait_back)
    void back(){
        getActivity().onBackPressed();
    }

}
