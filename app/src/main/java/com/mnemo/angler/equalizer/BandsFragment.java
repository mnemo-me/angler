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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class BandsFragment extends Fragment {


    @BindView(R.id.bands_layout)
    LinearLayout bandsLayout;

    @BindView(R.id.bands_spinner)
    Spinner presetsSpinner;

    Unbinder unbinder;

    private short lowerEqualizerBandLevel;
    private short upperEqualizerBandLevel;

    private ArrayList<Integer> bandsFrequencies;
    private ArrayList<String> equalizerPresets;

    private BroadcastReceiver receiver;

    SharedPreferences sharedPreferences;

    public BandsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.eq_fragment_bands, container, false);

        unbinder = ButterKnife.bind(this, view);

        sharedPreferences =  getActivity().getSharedPreferences("equalizer_pref", Context.MODE_PRIVATE);


        // Get equalizer parameters from service
        Bundle bundle = ((MainActivity)getActivity()).getServiceBundle();
        lowerEqualizerBandLevel = bundle.getShort("lower_equalizer_band_level");
        upperEqualizerBandLevel = bundle.getShort("upper_equalizer_band_level");
        bandsFrequencies = bundle.getIntegerArrayList("bands_frequencies");
        equalizerPresets = bundle.getStringArrayList("equalizer_presets");

        if (!equalizerPresets.contains(getResources().getString(R.string.custom))) {
            equalizerPresets.add(0, getResources().getString(R.string.custom));
        }


        // create bands
        for (int i = 0; i < bandsFrequencies.size(); i++){

            final short bandNumber = (short)i;
            int bandLevel = sharedPreferences.getInt("band_" + i + "_level", 0);

            LinearLayout bandLayout = (LinearLayout) inflater.inflate(R.layout.eq_band, bandsLayout, false);

            final TextView bandLevelView = bandLayout.findViewById(R.id.band_level);

            if (bandLevel / 100 > 0) {
                bandLevelView.setText("+" + bandLevel / 100 + " db");
            }else {
                bandLevelView.setText(bandLevel / 100 + " db");
            }

            SeekBar seekBar = bandLayout.findViewById(R.id.band_band);
            seekBar.setId(i);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
            seekBar.setProgress(bandLevel - lowerEqualizerBandLevel);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                    if ((progress + lowerEqualizerBandLevel)/100 > 0) {
                        bandLevelView.setText("+" + (progress + lowerEqualizerBandLevel) / 100 + " db");
                    }else{
                        bandLevelView.setText((progress + lowerEqualizerBandLevel) / 100 + " db");
                    }

                    Bundle extras = new Bundle();
                    extras.putShort("band_number", bandNumber);
                    extras.putShort("band_level", (short)(progress + lowerEqualizerBandLevel));

                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("equalizer_change_band_level", extras);

                    if (b) {
                        presetsSpinner.setSelection(0);
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            TextView frequencyView = bandLayout.findViewById(R.id.band_frequency);
            frequencyView.setText(bandsFrequencies.get(i) / 1000 + " Hz");


            bandsLayout.addView(bandLayout);
        }


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case "equalizer_preset_changed":

                        for (short i = 0; i < bandsFrequencies.size(); i++){

                            short bandLevel = intent.getExtras().getShort("band_" + i + "_level");
                            ((SeekBar)view.findViewById(i)).setProgress(bandLevel - lowerEqualizerBandLevel, true);
                        }

                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("equalizer_preset_changed");

        getContext().registerReceiver(receiver, intentFilter);



        // Setup spinner with array adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.playlist_spinner_item, R.id.playlist_spinner_item_title, equalizerPresets);
        adapter.setDropDownViewResource(R.layout.playlist_spinner_dropdown_item);
        presetsSpinner.setAdapter(adapter);
        presetsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i != 0) {
                    Bundle extras = new Bundle();
                    extras.putShort("preset_number", (short) (i - 1));
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("equalizer_change_preset", extras);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        presetsSpinner.setSelection(sharedPreferences.getInt("active_preset", 0));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        sharedPreferences.edit()
                .putInt("active_preset", presetsSpinner.getSelectedItemPosition())
                .apply();

        for (short i = 0; i < bandsFrequencies.size(); i++){
            sharedPreferences.edit().putInt("band_" + i + "_level", ((SeekBar)getView().findViewById(i)).getProgress() + lowerEqualizerBandLevel).apply();
        }

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }

}
