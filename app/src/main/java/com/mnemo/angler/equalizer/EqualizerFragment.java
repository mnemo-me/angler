package com.mnemo.angler.equalizer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class EqualizerFragment extends Fragment implements DrawerItem {

    @BindView(R.id.equalizer_on_off)
    Switch equalizerSwitch;

    @Nullable @BindView(R.id.equalizer_equalizer)
    ImageView equalizer;

    @Nullable @BindView(R.id.equalizer_audio_effects)
    ImageView audioEffects;

    Unbinder unbinder;

    SharedPreferences sharedPreferences;

    public EqualizerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.eq_fragment_equalizer, container, false);

        unbinder = ButterKnife.bind(this, view);

        sharedPreferences = getActivity().getSharedPreferences("equalizer_pref", Context.MODE_PRIVATE);

        // Setup equalizer on/off switch
        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (compoundButton.didTouchFocusSelect()) {
                    Bundle extras = new Bundle();
                    extras.putBoolean("on_off_state", b);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("equalizer_on_off", extras);
                }

                Intent intent = new Intent();
                intent.setAction("equalizer_on_off_state_changed");
                intent.putExtra("equalizer_on_off_state", b);

                getActivity().sendBroadcast(intent);
            }
        });

        equalizerSwitch.setChecked(sharedPreferences.getBoolean("on_off_state", false));


        // Open bands child fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.eq_frame, new BandsFragment())
                .commit();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            // Open audio effects child fragment
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.eq_frame_2, new AudioEffectsFragment())
                    .commit();
        }else{

            equalizer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    audioEffects.setAlpha(0.5f);
                    equalizer.setAlpha(1f);

                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.eq_frame, new BandsFragment())
                            .commit();
                }
            });

            audioEffects.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    equalizer.setAlpha(0.5f);
                    audioEffects.setAlpha(1f);

                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.eq_frame, new AudioEffectsFragment())
                            .commit();
                }
            });
        }

        return view;
    }


    // Setup drawer menu button
    @OnClick(R.id.equalizer_drawer_back)
    void back(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        sharedPreferences.edit().putBoolean("on_off_state", equalizerSwitch.isChecked()).apply();

        unbinder.unbind();
    }
}
