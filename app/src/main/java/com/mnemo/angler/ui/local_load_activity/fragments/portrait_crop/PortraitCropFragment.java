package com.mnemo.angler.ui.local_load_activity.fragments.portrait_crop;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.fragments.landscape_crop.LandscapeCropFragment;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.steelkiwi.cropiwa.AspectRatio;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PortraitCropFragment extends Fragment implements PortraitCropView{

    private PortraitCropPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.fragment_portrait_crop_iwa)
    CropIwaView cropIwaView;

    private String image;


    public PortraitCropFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.ll_fragment_portrait_crop, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get image from arguments
        image = getArguments().getString("image");

        // Setup CropIwa
        Point size = new Point();

        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        int aspectRatioW;
        int aspectRatioH;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

            aspectRatioW = (int)(size.x + 20 * MainActivity.density);
            aspectRatioH = (int)(size.y - (48 + 140) * MainActivity.density);
        }else{
            aspectRatioW = (int)(size.y + 20 * MainActivity.density);
            aspectRatioH = (int)(size.x - (48 + 140) * MainActivity.density);
        }

        cropIwaView.configureOverlay()
                .setAspectRatio(new AspectRatio(aspectRatioW, aspectRatioH))
                .apply();

        cropIwaView.setImageUri(Uri.fromFile(new File(image)));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new PortraitCropPresenter();
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
    @OnClick(R.id.fragment_portrait_crop)
    void crop(){

        // Generate new background image file name
        String backgroundImageFileName = presenter.generateBackgroundImageFileName(image);

        // Crop image and save it to file storage
        cropIwaView.crop(new CropIwaSaveConfig.Builder(presenter.createNewBackgroundImageFile(backgroundImageFileName))
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .build());


        // Open landscape crop fragment
        LandscapeCropFragment landscapeCropFragment = new LandscapeCropFragment();

        Bundle args = new Bundle();
        args.putString("image", image);
        args.putString("background_image_file_name", backgroundImageFileName);
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
