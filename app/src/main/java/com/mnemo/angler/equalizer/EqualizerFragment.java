package com.mnemo.angler.equalizer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.mnemo.angler.AnglerService;
import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;


public class EqualizerFragment extends Fragment implements DrawerItem {


    public EqualizerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.eq_fragment_equalizer, container, false);

        // Setup drawer menu button
        ImageView drawerBack = view.findViewById(R.id.equalizer_drawer_back);
        drawerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
            }
        });

        Switch equalizerSwitch = view.findViewById(R.id.equalizer_on_off);
        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
/*
                if (compoundButton.isChecked()){

                    if (AnglerService.mEqualizer != null) {
                        AnglerService.mEqualizer.setEnabled(true);
                    }

                }else{

                    if (AnglerService.mEqualizer != null) {
                        AnglerService.mEqualizer.setEnabled(false);
                    }
                }*/
            }
        });

        return view;
    }

}
