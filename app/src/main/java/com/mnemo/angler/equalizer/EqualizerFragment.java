package com.mnemo.angler.equalizer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

                Bundle extras = new Bundle();
                extras.putBoolean("on_off_state", b);
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("equalizer_on_off", extras);
            }
        });

        equalizerSwitch.setChecked(sharedPreferences.getBoolean("on_off_state", false));


        // Open bands child fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.eq_frame, new BandsFragment())
                .commit();


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
