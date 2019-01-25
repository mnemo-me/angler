package com.mnemo.angler.ui.main_activity.fragments.equalizer.audio_effects;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.mnemo.angler.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AudioEffectsFragment extends Fragment implements AudioEffectsView{

    private AudioEffectsPresenter presenter;

    private Unbinder unbinder;

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


    private BroadcastReceiver receiver;


    public AudioEffectsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.eq_fragment_audio_effects, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Bind Presenter to View
        presenter = new AudioEffectsPresenter();
        presenter.attachView(this);

        // Virtualizer
        virtualizerSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

            if (compoundButton.isPressed()) {
                virtualizerSeekBar.setEnabled(b);

                Bundle extras = new Bundle();
                extras.putBoolean("on_off_state", b);

                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("virtualizer_on_off", extras);
            }
        });

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

        // Get virtualizer state and strength
        boolean virtualizerOnOffState = presenter.getVirtualizerState();
        int virtualizerStrength = presenter.getVirtualizerStrength();

        virtualizerSwitch.setChecked(virtualizerOnOffState);

        virtualizerSeekBar.setProgress(virtualizerStrength);
        virtualizerLevel.setText(virtualizerStrength / 10 + " %");
        virtualizerSeekBar.setEnabled(virtualizerOnOffState);


        // Bass Boost
        bassBoostSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

            if (compoundButton.isPressed()){
                bassBoostSeekBar.setEnabled(b);

                Bundle extras = new Bundle();
                extras.putBoolean("on_off_state", b);

                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("bass_boost_on_off", extras);
            }
        });

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

        // Get bass boost state and strength
        boolean bassBoostOnOffState = presenter.getBassBoostState();
        int bassBoostStrength = presenter.getBassBoostStrength();

        bassBoostSwitch.setChecked(bassBoostOnOffState);

        bassBoostSeekBar.setProgress(bassBoostStrength);
        bassBoostLevel.setText(bassBoostStrength / 10 + " %");
        bassBoostSeekBar.setEnabled(bassBoostOnOffState);


        // Amplifier
        amplifierSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

            if (compoundButton.isPressed()){
                amplifierSeekBar.setEnabled(b);

                Bundle extras = new Bundle();
                extras.putBoolean("on_off_state", b);

                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("amplifier_on_off", extras);
            }
        });

        amplifierSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b) {

                    Bundle extras = new Bundle();
                    extras.putShort("amplifier_band_level", (short)i);

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("amplifier_change_band_level", extras);
                }

                amplifierLevel.setText(i + " mDB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Get amplifier state and gain
        boolean amplifierOnOffState = presenter.getAmplifierState();
        int amplifierGain = presenter.getAmplifierGain();

        amplifierSwitch.setChecked(amplifierOnOffState);

        amplifierSeekBar.setProgress(amplifierGain);
        amplifierLevel.setText(amplifierGain + " mDB");
        amplifierSeekBar.setEnabled(amplifierOnOffState);


        // Disable views if equalizer off
        boolean equalizerOnOffState = getArguments().getBoolean("equalizer_on_off_state");

        if (!equalizerOnOffState){

            virtualizerSwitch.setEnabled(false);
            virtualizerSeekBar.setEnabled(false);
            bassBoostSwitch.setEnabled(false);
            bassBoostSeekBar.setEnabled(false);
            amplifierSwitch.setEnabled(false);
            amplifierSeekBar.setEnabled(false);
        }


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

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
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.saveVirtualizerState(virtualizerSwitch.isChecked());
        presenter.saveVirtualizerStrength(virtualizerSeekBar.getProgress());

        presenter.saveBassBoostState(bassBoostSwitch.isChecked());
        presenter.saveBassBoostStrength(bassBoostSeekBar.getProgress());

        presenter.saveAmplifierState(amplifierSwitch.isChecked());
        presenter.saveAmplifierGain(amplifierSeekBar.getProgress());

        presenter.deattachView();

        unbinder.unbind();
    }
}
