package com.mnemo.angler.ui.main_activity.fragments.background_changer.background_changer;


import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.adapters.BackgroundImageAdapter;
import com.mnemo.angler.util.ImageAssistant;
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class BackgroundChangerFragment extends Fragment implements DrawerItem, BackgroundChangerView {

    private BackgroundChangerPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.overlay_background)
    ImageView background;

    @BindView(R.id.overlay_overlay)
    View overlay;

    @BindView(R.id.background_changer_select_background)
    ImageView select;

    @BindView(R.id.background_changer_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.overlay_seekbar)
    SeekBar seekBar;

    private BackgroundImageAdapter backgroundImageAdapter;

    // Background variables
    private String backgroundImage;
    private String selectedImage;
    private String imageFolder;
    private int imageHeight;
    private int opacity;

    private Boolean isInteract = false;
    private Boolean isImageInteract = false;

    // CropIwa receiver
    private CropIwaResultReceiver resultReceiver;

    public BackgroundChangerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_background_changer_v2, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Get image height & folder based on orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageFolder = AnglerFolder.PATH_BACKGROUND_PORTRAIT;
            imageHeight = 520;
        }else{
            imageFolder = AnglerFolder.PATH_BACKGROUND_LANDSCAPE;
            imageHeight = 203;
        }

        // Get is interact
        if (savedInstanceState != null){
            isInteract = savedInstanceState.getBoolean("is_interact");
            isImageInteract = savedInstanceState.getBoolean("is_image_interact");
        }

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Hide background
        ((MainActivity)getActivity()).hideBackground();

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        }else{
            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }

        recyclerView.setLayoutManager(linearLayoutManager);

        // Change transparency of overlay moving seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {

                    if (!isInteract) {
                        isInteract = true;
                        selectEnable(true);
                    }

                    opacity = i;
                }

                overlay.setBackgroundColor(Color.argb(i,0,0,0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Set select button enabled/disabled base on interact
        selectEnable(isInteract);

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new BackgroundChangerPresenter();
        presenter.attachView(this);

        // Get current background image and opacity
        if (savedInstanceState != null) {

            backgroundImage = savedInstanceState.getString("background_image");
            selectedImage = savedInstanceState.getString("selected_image");
            opacity = savedInstanceState.getInt("opacity");

        }else{

            backgroundImage = presenter.getCurrentBackgroundImage();
            selectedImage = backgroundImage;
            opacity = presenter.getCurrentOpacity();
        }

        showBackgroundImage(selectedImage);
        seekBar.setProgress(opacity);

        // Gather images
        presenter.gatherBackgroundImages();
    }

    @Override
    public void onStart() {
        super.onStart();

        // CropIwa result receiver updating image cover when new cover cropped
        resultReceiver = new CropIwaResultReceiver();
        resultReceiver.setListener(new CropIwaResultReceiver.Listener() {
            @Override
            public void onCropSuccess(Uri croppedUri) {
                presenter.gatherBackgroundImages();
            }

            @Override
            public void onCropFailed(Throwable e) {

            }
        });
        resultReceiver.register(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();

        resultReceiver.unregister(getContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("background_image", backgroundImage);
        outState.putString("selected_image", selectedImage);
        outState.putInt("opacity", opacity);
        outState.putBoolean("is_interact", isInteract);
        outState.putBoolean("is_image_interact", isImageInteract);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show background
        ((MainActivity)getActivity()).showBackground();

        presenter.deattachView();
        unbinder.unbind();
    }

    // Setup select button
    @OnClick(R.id.background_changer_select_background)
    void selectBackground() {

        // Save background image and opacity
        presenter.saveBackground(selectedImage, opacity);

        // Set new backgroundImage and opacity
        ((MainActivity)getActivity()).setBackground(selectedImage, opacity);

        // Select music player drawer item
        ((MainActivity)getActivity()).selectDrawerItem(0);

        getActivity().onBackPressed();
    }

    // Setup drawer menu button
    @OnClick(R.id.background_changer_drawer_back)
    void back() {
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }



    // MVP View methods
    @Override
    public void setBackgroundImages(List<String> images) {

        backgroundImageAdapter = new BackgroundImageAdapter(getContext(), images);

        backgroundImageAdapter.setOnImageClickListener(image -> {

            if (!isInteract){
                isInteract = true;
                selectEnable(true);
            }

            if (!isImageInteract){
                isImageInteract = true;
            }

            selectedImage = image;
            showBackgroundImage(selectedImage);
        });

        backgroundImageAdapter.setOnImageDeleteListener(image -> {

            presenter.deleteBackgroundImage(image);

            if (image.equals(selectedImage)){
                isImageInteract = false;

                selectedImage = backgroundImage;
                showBackgroundImage(selectedImage);
            }

            if (image.equals(backgroundImage)){

                if (selectedImage.equals(backgroundImage)){
                    selectedImage = "R.drawable.back1";
                }
                backgroundImage = "R.drawable.back1";

                presenter.saveBackground(backgroundImage, presenter.getCurrentOpacity());
                showBackgroundImage(backgroundImage);
                ((MainActivity)getActivity()).setBackground(backgroundImage, presenter.getCurrentOpacity());

                backgroundImageAdapter.setDefaultBackground();
            }
        });

        backgroundImageAdapter.setCurrentBackground(backgroundImage);

        if (isImageInteract) {
            backgroundImageAdapter.setSelectedImage(selectedImage);
        }

        recyclerView.setAdapter(backgroundImageAdapter);
        backgroundImageAdapter.notifyDataSetChanged();
    }


    // Support methods
    private void showBackgroundImage(String image){

        if (selectedImage.startsWith("R.drawable.")){
            ImageAssistant.loadImage(getContext(), image, background, imageHeight);
        }else {
            ImageAssistant.loadImage(getContext(), imageFolder + File.separator + image, background, imageHeight);
        }
    }

    private void selectEnable(Boolean isInteract){
        select.setEnabled(isInteract);

        if (isInteract){
            select.setAlpha(0.5f);
        }
    }
}
