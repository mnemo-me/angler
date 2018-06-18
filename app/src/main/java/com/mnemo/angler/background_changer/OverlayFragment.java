package com.mnemo.angler.background_changer;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mnemo.angler.AnglerService;
import com.mnemo.angler.R;


public class OverlayFragment extends Fragment {


    public OverlayFragment() {
        // Required empty public constructor
    }

    String title;
    String artist;
    String album;
    Boolean isInteract;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.bg_fragment_overlay, container, false);

        // Avoid click through fragment
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        ImageView background = view.findViewById(R.id.overlay_background);

        String image = getArguments().getString("background");
        String backgroundImage;

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            backgroundImage = image;
        }else{
            backgroundImage = image.replace("port", "land");
        }

        ImageAssistent.loadImage(getContext(), backgroundImage, background, 520);

        // Setup metadata for random track in current playlist
        if (savedInstanceState != null){
            title = savedInstanceState.getString("title");
            artist = savedInstanceState.getString("artist");
            album = savedInstanceState.getString("album");
            isInteract = savedInstanceState.getBoolean("is_interact");
        }

        TextView titleView = view.findViewById(R.id.overlay_song_text);
        titleView.setText(title);

        TextView artistView = view.findViewById(R.id.overlay_artist_back);
        artistView.setText(artist.replace(" ", "\n"));

        TextView albumView = view.findViewById(R.id.overlay_album_text);
        albumView.setText(album);

        // Change transparency of overlay moving seek bar
        final View overlay = view.findViewById(R.id.overlay_overlay);
        final TextView transparencyDefault = view.findViewById(R.id.overlay_default);

        final TextView accept = view.findViewById(R.id.overlay_accept);
        accept.setActivated(isInteract);

        final SeekBar seekBar = view.findViewById(R.id.overlay_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {
                    isInteract = true;
                    accept.setActivated(true);
                }

                int alpha = i;

                if (alpha > 198 && alpha < 208){
                    alpha = 203;
                    seekBar.setProgress(alpha);
                }

                overlay.setBackgroundColor(Color.argb(alpha,0,0,0));

                if (alpha == 203){
                    view.findViewById(R.id.overlay_default_line).setBackgroundColor(getResources().getColor(R.color.colorAccent,null));
                    transparencyDefault.setTextColor(getResources().getColor(R.color.colorAccent, null));
                }else{
                    view.findViewById(R.id.overlay_default_line).setBackgroundColor(getResources().getColor(R.color.justGrey, null));
                    transparencyDefault.setTextColor(getResources().getColor(R.color.justGrey, null));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(getActivity().getPreferences(Context.MODE_PRIVATE).getInt("overlay",203));

        // Set default transparency on click

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            transparencyDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    seekBar.setProgress(203);
                    isInteract = true;
                    accept.setActivated(true);
                }
            });
        }else{
            TextView setDefault = view.findViewById(R.id.overlay_set_default);
            setDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    seekBar.setProgress(203);
                    isInteract = true;
                    accept.setActivated(true);
                }
            });
        }

        // Setup cancel button
        TextView cancel = view.findViewById(R.id.overlay_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        // Setup accept button
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInteract) {
                    int transparency = seekBar.getProgress();
                    getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt("overlay", transparency).apply();
                    FrameLayout overlay = getActivity().findViewById(R.id.overlay);
                    overlay.setBackgroundColor(Color.argb(transparency, 0, 0, 0));
                    getActivity().onBackPressed();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("artist", artist);
        outState.putString("album", album);
        outState.putBoolean("is_interact", isInteract);
    }

}
