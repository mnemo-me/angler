package com.mnemo.angler.ui.local_load_activity.fragments.landscape_crop;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class LandscapeCropFragment extends Fragment implements LandscapeCropView{

    LandscapeCropPresenter presenter;

    Unbinder unbinder;

    @BindView(R.id.fragment_landscape_crop_iwa)
    CropIwaView cropIwaView;

    String image;
    String backgroundImageFileName;


    public LandscapeCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_landscape_crop, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get image and new image name from arguments
        image = getArguments().getString("image");
        backgroundImageFileName = getArguments().getString("background_image_file_name");

        // Setup CropIwa
        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new LandscapeCropPresenter();
        presenter.attachView(this);
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

        // Crop image and save it to file storage
        cropIwaView.crop(new CropIwaSaveConfig.Builder(presenter.createNewBackgroundImageFile(backgroundImageFileName, Configuration.ORIENTATION_LANDSCAPE))
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build());

        // Show toast
        Toast.makeText(getContext(),R.string.background_added, Toast.LENGTH_SHORT).show();

        // Destroy local load activity
        getActivity().finish();
    }

    // Setup back button
    @OnClick(R.id.fragment_landscape_back)
    void back(){
        presenter.deleteBackgroundImage(backgroundImageFileName);
        getActivity().onBackPressed();
    }

}
