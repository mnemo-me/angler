package com.mnemo.angler.drawer_items_fragments.local_load;


import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.R;

import java.io.File;
import java.util.ArrayList;



public class ImageFolderFragment extends Fragment {

    public ImageFolderFragment() {
        // Required empty public constructor
    }

    // Constructor for ImageFolderFragment with arguments
    public static ImageFolderFragment createImageFolderFragment(String imageFolder) {

        Bundle args = new Bundle();
        args.putString("image_folder", imageFolder);
        ImageFolderFragment imageFolderFragment = new ImageFolderFragment();
        imageFolderFragment.setArguments(args);

        return imageFolderFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.ll_fragment_image_folder, container, false);

        // Get list of images in image folder from arguments
        String imageFolder = getArguments().getString("image_folder");
        ArrayList<String> images = getImages(imageFolder);

        // Setup RecyclerView with adapter
        RecyclerView recyclerView = view.findViewById(R.id.image_folder_recycler_view);
        ImageFolderAdapter imageFolderAdapter = new ImageFolderAdapter(getContext(), images);
        recyclerView.setAdapter(imageFolderAdapter);

        // Set grid as layout manager for recycler view
        int orientation = getContext().getResources().getConfiguration().orientation;
        int spanCount;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            spanCount = 3;
        }else{
            spanCount = 5;
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
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

}
