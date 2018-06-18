package com.mnemo.angler.background_changer;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.AnglerApplication;
import com.mnemo.angler.AnglerService;
import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;


public class BackgroundChangerFragmentv2 extends Fragment implements DrawerItem {


    public BackgroundChangerFragmentv2() {
        // Required empty public constructor
    }

    String backgroundImage;

    String title;
    String artist;
    String album;
    Boolean isInteract;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.bg_fragment_background_changer_v2, container, false);

        // Hide background
        ((MainActivity)getActivity()).hideBackground();

        // Initialize working background image
        final ImageView background = view.findViewById(R.id.overlay_background);

        final String image = getActivity().getPreferences(Context.MODE_PRIVATE).getString("background", "R.drawable.back");

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            backgroundImage = image;
        }else{
            backgroundImage = image.replace("port", "land");
        }

        ImageAssistent.loadImage(getContext(), backgroundImage, background, 280);



        // Change transparency of overlay moving seek bar
        final View overlay = view.findViewById(R.id.overlay_overlay);

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
        final ArrayList<String> images = new ArrayList<>();

        // Add 'load from storage' image
        images.add("R.drawable.ic_add_white_24dp");

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

        // Initialize select button
        final ImageView select = view.findViewById(R.id.background_changer_select_background);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                sharedPref.edit().putString("background",backgroundImage).apply();
                sharedPref.edit().putInt("overlay", seekBar.getProgress()).apply();

                ImageView background = getActivity().findViewById(R.id.main_fragment_background);

                ImageAssistent.loadImage(getContext(), backgroundImage, background, 520);
                ((MainActivity)getActivity()).setOverlay(seekBar.getProgress());

                getActivity().onBackPressed();
            }
        });

        // Initialize recycler view and background image adapter for it
        final RecyclerView recyclerView = view.findViewById(R.id.background_changer_recycler_view);
        BackgroundImageAdapter backgroundImageAdapter = new BackgroundImageAdapter(getActivity(), images);

        /*
        Setup listeners:
        OnImageClickListener = set background
        OnImageLongClickListener - delete non-default background
         */

        backgroundImageAdapter.setOnImageClickListener(new BackgroundImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(final String image) {

                ImageAssistent.loadImage(getContext(), image, background, 280);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    backgroundImage = image;
                }else{
                    backgroundImage = image.replace("port", "land");
                }

/*
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (image.startsWith("R.drawable.")) {
                            Toast.makeText(getContext(),"Can't delete default background",Toast.LENGTH_SHORT).show();
                        }else{

                            BackgroundDeleteFragment backgroundDeleteFragment = new BackgroundDeleteFragment();
                            Bundle args = new Bundle();
                            args.putString("image",image);
                            backgroundDeleteFragment.setArguments(args);

                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .add(R.id.frame, backgroundDeleteFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                });*/

            }
        });

        recyclerView.setAdapter(backgroundImageAdapter);


        // Set layout manager for recycler view based on orientation

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
        }




        // Setup drawer menu button
        ImageView drawerBack = view.findViewById(R.id.background_changer_drawer_back);
        drawerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("artist", artist);
        outState.putString("album", album);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show background
        ((MainActivity)getActivity()).showBackground();
    }

}
