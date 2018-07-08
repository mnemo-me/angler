package com.mnemo.angler.background_changer;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class BackgroundChangerFragmentv2 extends Fragment implements DrawerItem {


    public BackgroundChangerFragmentv2() {
        // Required empty public constructor
    }

    @BindView(R.id.overlay_background)
    ImageView background;

    @BindView(R.id.overlay_overlay)
    View overlay;

    @BindView(R.id.background_changer_select_background)
    View select;

    @BindView(R.id.background_changer_recycler_view)
    RecyclerView recyclerView;


    String backgroundImage;
    int imageHeight;

    Boolean isInteract;

    Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_background_changer_v2, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Hide background
        ((MainActivity)getActivity()).hideBackground();

        int orientation = getResources().getConfiguration().orientation;


        // Get background image from shared preferences
        String image = getActivity().getPreferences(Context.MODE_PRIVATE).getString("background", "R.drawable.back");

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            backgroundImage = image;
            imageHeight = 520;
        }else{
            backgroundImage = image.replace("port", "land");
            imageHeight = 203;
        }

        ImageAssistent.loadImage(getContext(), backgroundImage, background, imageHeight);



        // Change transparency of overlay moving seek bar
        final SeekBar seekBar = view.findViewById(R.id.overlay_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {
                    isInteract = true;
                }

                int alpha = i;

                if (alpha > 198 && alpha < 208){
                    alpha = 203;
                    seekBar.setProgress(alpha);
                }

                overlay.setBackgroundColor(Color.argb(alpha,0,0,0));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(getActivity().getPreferences(Context.MODE_PRIVATE).getInt("overlay",203));



        // Create list of images
        ArrayList<String> images = new ArrayList<>();

        // Add 'load from storage' image
        images.add("R.drawable.null_rectangle");

        // Add images from app folder to list
        TreeSet<String> backgroundFolderImages = new TreeSet<>(new BackgroundComparator());
        backgroundFolderImages.addAll(ImageFolderFragment.getImages(AnglerFolder.PATH_BACKGROUND_PORTRAIT));

        // Delete single orientation backgrounds
        for (String s : backgroundFolderImages){
            File port = new File(s);
            File land = new File(s.replace("port","land"));

            if (port.exists() && land.exists()){
                images.add(s);
            }else{
                port.delete();
                land.delete();
            }
        }

        // Add default images to list
        images.add("R.drawable.back");
        images.add("R.drawable.back2");
        images.add("R.drawable.back3");
        images.add("R.drawable.back4");


        // Initialize recycler view and background image adapter for it
        final BackgroundImageAdapter backgroundImageAdapter = new BackgroundImageAdapter(getActivity(), images);

        backgroundImageAdapter.setOnImageClickListener(new BackgroundImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(final String image) {

                ImageAssistent.loadImage(getContext(), image, background, imageHeight);

            }
        });

        recyclerView.setAdapter(backgroundImageAdapter);


        // Set layout manager for recycler view based on orientation

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
        }


        // Setup select button
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

                String selectedBackgroundImage = backgroundImageAdapter.getSelectedImage();

                if (selectedBackgroundImage != null){
                    sharedPref.edit().putString("background", selectedBackgroundImage).apply();

                    ImageView background = getActivity().findViewById(R.id.main_fragment_background);

                    ImageAssistent.loadImage(getContext(), backgroundImageAdapter.getSelectedImage(), background, imageHeight);
                }

                sharedPref.edit().putInt("overlay", seekBar.getProgress()).apply();

                ((MainActivity)getActivity()).setOverlay(seekBar.getProgress());


                getActivity().onBackPressed();
            }
        });


        return  view;
    }


    private class BackgroundComparator implements Comparator<String>{

        @Override
        public int compare(String s, String t1) {

            File one = new File(s);
            File two = new File(t1);

            return (int)(two.lastModified() - one.lastModified());
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show background
        ((MainActivity)getActivity()).showBackground();

        unbinder.unbind();
    }


    // Setup drawer menu button
    @OnClick(R.id.background_changer_drawer_back)
    void back() {
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }

}
