package com.mnemo.angler.ui.local_load_activity.fragments.image_folder;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.ui.local_load_activity.adapters.ImageFolderAdapter;
import com.mnemo.angler.ui.local_load_activity.misc.ImageDecoration;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.util.ArrayList;


public class ImageFolderFragment extends Fragment implements ImageFolderView{

    ImageFolderPresenter presenter;

    RecyclerView recyclerView;

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

        // Get orientation
        int orientation = getContext().getResources().getConfiguration().orientation;

        // Setup RecyclerView
        recyclerView = new RecyclerView(getContext());
        recyclerView.setPadding(0, (int)(16 * MainActivity.density), 0, 0);
        recyclerView.setClipToPadding(false);

        // Set grid as layout manager for recycler view
        int spanCount;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            spanCount = 3;
        }else{
            spanCount = 5;
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new ImageDecoration(spanCount));

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ImageFolderPresenter();
        presenter.attachView(this);

        // Get list of images in image folder from arguments
        String imageFolder = getArguments().getString("image_folder");
        presenter.getImages(imageFolder);
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.deattachView();
    }

    // MVP View methods
    @Override
    public void setImages(ArrayList<String> images) {

        ImageFolderAdapter imageFolderAdapter = new ImageFolderAdapter(getContext(), images);
        recyclerView.setAdapter(imageFolderAdapter);
    }
}
