package com.mnemo.angler.background_changer;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.R;

import java.io.File;
import java.util.ArrayList;



public class ImageFolderFragment extends Fragment {

    private ArrayList<String> images;

    public ImageFolderFragment() {
        // Required empty public constructor
    }

    // Constructor for ImageFolderFragment with arguments
    public static ImageFolderFragment createImageFolderFragment(String imageFolder, String imageType) {

        Bundle args = new Bundle();
        args.putString("image_folder", imageFolder);
        args.putString("image_type", imageType);
        ImageFolderFragment imageFolderFragment = new ImageFolderFragment();
        imageFolderFragment.setArguments(args);

        return imageFolderFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.bg_fragment_image_folder, container, false);

        // Get list of images in image folder from arguments
        String imageFolder = getArguments().getString("image_folder");
        images = getImages(imageFolder);
        String imageType = getArguments().getString("image_type");

        // Setup RecyclerView with adapter
        RecyclerView recyclerView = view.findViewById(R.id.image_folder_recycler_view);
        ImageFolderAdapter imageFolderAdapter = new ImageFolderAdapter(getContext(), images, imageType);
        recyclerView.setAdapter(imageFolderAdapter);

        // Set FlexBox as layout manager for recycler view
        //FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext(), FlexDirection.ROW);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }


    // Get list of images in selected folder
    public static ArrayList<String> getImages(String imageFolder) {

        ArrayList<String> images = new ArrayList<>();

        File directory = new File(imageFolder);
        String[] files = directory.list();

        for (String file : files) {

            File temp = new File(imageFolder + File.separator + file);
            String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (mimeType != null) {
                if (mimeType.contains("image/")) {
                    images.add(imageFolder + File.separator + file);
                }
            }
        }

        return images;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }
}
