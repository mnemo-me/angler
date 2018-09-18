package com.mnemo.angler.equalizer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.mnemo.angler.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AudioEffectsFragment extends Fragment {

    @BindView(R.id.ae_virtualizer_switch)
    Switch virtualizerSwitch;

    @BindView(R.id.ae_virtualizer_seek_bar)
    SeekBar virtualizerSeekBar;

    @BindView(R.id.ae_virtualizer_level)
    TextView virtualizerLevel;

    @BindView(R.id.ae_bass_boost_switch)
    Switch bassBoostSwitch;

    @BindView(R.id.ae_bass_boost_seek_bar)
    SeekBar bassBoostSeekBar;

    @BindView(R.id.ae_bass_boost_level)
    TextView bassBoostLevel;

    @BindView(R.id.ae_amplifier_switch)
    Switch amplifierSwitch;

    @BindView(R.id.ae_amplifier_seek_bar)
    SeekBar amplifierSeekBar;

    @BindView(R.id.ae_amplifier_level)
    TextView amplifierLevel;

    Unbinder unbinder;

    private BroadcastReceiver receiver;

    SharedPreferences sharedPreferences;


    public AudioEffectsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.eq_fragment_audio_effects, container, false);

        unbinder = ButterKnife.bind(this, view);

        // configure audio effects views from shared preferences
        sharedPreferences =  getActivity().getSharedPreferences("equalizer_pref", Context.MODE_PRIVATE);

        // Virtualizer
        boolean virtualizerOnOffState = sharedPreferences.getBoolean("virtualizer_on_off_state", false);
        short virtualizerStrength = (short)sharedPreferences.getInt("virtualizer_strength", 0);

        // Bass boost
        boolean bassBoostOnOffState = sharedPreferences.getBoolean("bass_boost_on_off_state", false);
        short bassBoostStrength = (short)sharedPreferences.getInt("bass_boost_strength", 0);


        // Amplifier
        boolean amplifierOnOffState = sharedPreferences.getBoolean("amplifier_on_off_state", false);
        short amplifierGain = (short)sharedPreferences.getInt("amplifier_gain", 0);



        // Virtualizer
        virtualizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                virtualizerSeekBar.setEnabled(b);

                if (compoundButton.didTouchFocusSelect()) {
                    Bundle extras = new Bundle();
                    extras.putBoolean("on_off_state", b);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("virtualizer_on_off", extras);
                }

            }
        });

        virtualizerSwitch.setChecked(virtualizerOnOffState);

        virtualizerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {
                    Bundle extras = new Bundle();
                    extras.putShort("virtualizer_band_level", (short)i);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("virtualizer_change_band_level", extras);
                }

                virtualizerLevel.setText(i / 10 + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        virtualizerSeekBar.setProgress(virtualizerStrength);
        virtualizerLevel.setText(virtualizerStrength / 10 + " %");
        virtualizerSeekBar.setEnabled(virtualizerOnOffState);


        // Bass Boost
        bassBoostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                bassBoostSeekBar.setEnabled(b);

                if (compoundButton.didTouchFocusSelect()) {
                    Bundle extras = new Bundle();
                    extras.putBoolean("on_off_state", b);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("bass_boost_on_off", extras);
                }
            }
        });

        bassBoostSwitch.setChecked(bassBoostOnOffState);


        bassBoostSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {

                    Bundle extras = new Bundle();
                    extras.putShort("bass_boost_band_level", (short)i);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("bass_boost_change_band_level", extras);
                }

                bassBoostLevel.setText(i / 10 + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bassBoostSeekBar.setProgress(bassBoostStrength);
        bassBoostLevel.setText(bassBoostStrength / 10 + " %");
        bassBoostSeekBar.setEnabled(bassBoostOnOffState);


        // Amplifier
        amplifierSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                amplifierSeekBar.setEnabled(b);

                if (compoundButton.didTouchFocusSelect()) {
                    Bundle extras = new Bundle();
                    extras.putBoolean("on_off_state", b);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("amplifier_on_off", extras);
                }

            }
        });

        amplifierSwitch.setChecked(amplifierOnOffState);


        amplifierSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {

                    Bundle extras = new Bundle();
                    extras.putShort("amplifier_band_level", (short)i);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("amplifier_change_band_level", extras);
                }

                amplifierLevel.setText(i + " mDb");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        amplifierSeekBar.setProgress(amplifierGain);
        amplifierLevel.setText(amplifierGain + " mDb");
        amplifierSeekBar.setEnabled(amplifierOnOffState);


        // disable views if equalizer off
        if (!((Switch)getActivity().findViewById(R.id.equalizer_on_off)).isChecked()){

            virtualizerSwitch.setEnabled(false);
            virtualizerSeekBar.setEnabled(false);
            bassBoostSwitch.setEnabled(false);
            bassBoostSeekBar.setEnabled(false);
            amplifierSwitch.setEnabled(false);
            amplifierSeekBar.setEnabled(false);
        }


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case "equalizer_on_off_state_changed":

                        boolean equalizerOnOffState = intent.getExtras().getBoolean("equalizer_on_off_state");

                        virtualizerSwitch.setEnabled(equalizerOnOffState);

                        if (virtualizerSwitch.isChecked()) {
                            virtualizerSeekBar.setEnabled(equalizerOnOffState);
                        }else{
                            virtualizerSeekBar.setEnabled(false);
                        }

                        bassBoostSwitch.setEnabled(equalizerOnOffState);

                        if (bassBoostSwitch.isChecked()) {
                            bassBoostSeekBar.setEnabled(equalizerOnOffState);
                        }else{
                            bassBoostSeekBar.setEnabled(false);
                        }


                        amplifierSwitch.setEnabled(equalizerOnOffState);

                        if (amplifierSwitch.isChecked()) {
                            amplifierSeekBar.setEnabled(equalizerOnOffState);
                        }else{
                            amplifierSeekBar.setEnabled(false);
                        }

                        break;

                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("equalizer_on_off_state_changed");

        getContext().registerReceiver(receiver, intentFilter);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        sharedPreferences.edit()
                .putBoolean("virtualizer_on_off_state", virtualizerSwitch.isChecked())
                .putInt("virtualizer_strength", virtualizerSeekBar.getProgress())
                .putBoolean("bass_boost_on_off_state", bassBoostSwitch.isChecked())
                .putInt("bass_boost_strength", bassBoostSeekBar.getProgress())
                .putBoolean("amplifier_on_off_state", amplifierSwitch.isChecked())
                .putInt("amplifier_gain", amplifierSeekBar.getProgress())
                .apply();

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }
}
