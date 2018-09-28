package com.mnemo.angler.ui.local_load_activity.fragments;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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


public class LandscapeCropFragment extends Fragment {

    @BindView(R.id.fragment_landscape_crop_iwa)
    CropIwaView cropIwaView;

    String image;
    String newImageName;

    Unbinder unbinder;


    public LandscapeCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_landscape_crop, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get image and new image name from arguments
        image = getArguments().getString("image");
        newImageName = getArguments().getString("new_image_name");

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
    @OnClick(R.id.fragment_landscape_crop)
    void crop(){

        File destinationFile = new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE,
                newImageName);

        try {
            destinationFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        cropIwaView.crop(new CropIwaSaveConfig.Builder(Uri.fromFile(destinationFile))
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build());

        Toast.makeText(getContext(),R.string.background_added, Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    // Setup back button
    @OnClick(R.id.fragment_landscape_back)
    void back(){
        getActivity().onBackPressed();
    }

}
