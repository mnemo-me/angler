package com.mnemo.angler.ui.local_load_activity.fragments.landscape_crop;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.steelkiwi.cropiwa.AspectRatio;
import com.steelkiwi.cropiwa.CropIwaView;
import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class LandscapeCropFragment extends Fragment implements LandscapeCropView{

    private LandscapeCropPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.fragment_landscape_crop_iwa)
    CropIwaView cropIwaView;

    private String backgroundImageFileName;


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
        String image = getArguments().getString("image");
        backgroundImageFileName = getArguments().getString("background_image_file_name");

        // Setup CropIwa
        Point size = new Point();

        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        int aspectRatioW;
        int aspectRatioH;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

            aspectRatioW = size.y;
            aspectRatioH = (int)(size.x - (48 + 65) * MainActivity.density);
        }else{

            aspectRatioW = size.x;
            aspectRatioH = (int)(size.y - (48 + 65) * MainActivity.density);
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

        presenter = new LandscapeCropPresenter();
        presenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    /*
    Setup crop button
    Cropping image in CropIwa borders
    */
    @OnClick(R.id.fragment_landscape_crop)
    void crop(){

        // Crop image and save it to file storage
        cropIwaView.crop(new CropIwaSaveConfig.Builder(presenter.createNewBackgroundImageFile(backgroundImageFileName))
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
