package com.mnemo.angler.ui.local_load_activity.fragments.image_folder;


import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.adapters.ImageFolderAdapter;
import com.mnemo.angler.ui.local_load_activity.misc.ImageDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ImageFolderFragment extends Fragment implements ImageFolderView{

    private ImageFolderPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.image_folder_list)
    RecyclerView recyclerView;

    private ImageFolderAdapter imageFolderAdapter;

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
        View view = inflater.inflate(R.layout.ll_fragment_image_folder, container, false);

        // Get orientation
        int orientation = getContext().getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

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

        return view;
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
    public void onDestroy() {
        super.onDestroy();

        presenter.deattachView();
        unbinder.unbind();
    }

    // MVP View methods
    @Override
    public void setImages(List<String> images) {

        imageFolderAdapter = new ImageFolderAdapter(getContext(), images);
        recyclerView.setAdapter(imageFolderAdapter);
    }
}
