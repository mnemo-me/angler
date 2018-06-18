package com.mnemo.angler.background_changer;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerFolder;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;
import java.io.IOException;


public class LandscapeCropFragment extends Fragment {


    public LandscapeCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_landscape_crop, container, false);

        // Get image and new image name from arguments
        final String image = getArguments().getString("image");
        final String newImageName = getArguments().getString("new_image_name");

        // Setup CropIwa
        final CropIwaView cropIwaView = view.findViewById(R.id.fragment_landscape_crop_iwa);
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        // Setup back button
        TextView back = view.findViewById(R.id.fragment_landscape_back);
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
        TextView crop = view.findViewById(R.id.fragment_landscape_crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame, new BackgroundChangerFragmentv2())
                        .addToBackStack(null)
                        .commit();

                Toast.makeText(getContext(),R.string.background_added, Toast.LENGTH_SHORT).show();
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
