package com.mnemo.angler.ui.main_activity.fragments.equalizer.bands;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class BandsFragment extends Fragment implements BandsView {

    private BandsPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.bands_layout)
    LinearLayout bandsLayout;

    @BindView(R.id.bands_spinner)
    Spinner presetsSpinner;

    // Equalizer bands variables;
    private short lowerEqualizerBandLevel;

    private ArrayList<Integer> bandsFrequencies;


    private BroadcastReceiver receiver;

    public BandsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.eq_fragment_bands, container, false);

        // Get equalizerOnOffState
        boolean equalizerOnOffState = getArguments().getBoolean("equalizer_on_off_state");

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Bind Presenter to View
        presenter = new BandsPresenter();
        presenter.attachView(this);

        // Get equalizer parameters from service
        Bundle bundle = ((MainActivity)getActivity()).getAnglerClient().getServiceBundle();
        lowerEqualizerBandLevel = bundle.getShort("lower_equalizer_band_level");
        short upperEqualizerBandLevel = bundle.getShort("upper_equalizer_band_level");
        bandsFrequencies = bundle.getIntegerArrayList("bands_frequencies");
        ArrayList<String> equalizerPresets = bundle.getStringArrayList("equalizer_presets");

        if (!equalizerPresets.contains(getResources().getString(R.string.custom))) {
            equalizerPresets.add(0, getResources().getString(R.string.custom));
        }


        // Get bands level
        List<Short> bandsLevel = presenter.getBandsLevel(bandsFrequencies.size());

        // Create bands
        for (int i = 0; i < bandsFrequencies.size(); i++){

            short band = (short) i;

            LinearLayout bandLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.eq_band, bandsLayout, false);

            // Initialize band level view
            TextView bandLevelView = bandLayout.findViewById(R.id.band_level);
            bandLevelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_text_size));

            // Setup band SeekBar
            SeekBar seekBar = bandLayout.findViewById(R.id.band_band);
            seekBar.setId(i);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                    if ((progress + lowerEqualizerBandLevel) / 100 > 0) {
                        bandLevelView.setText("+" + (progress + lowerEqualizerBandLevel) / 100 + " db");
                    }else{
                        bandLevelView.setText((progress + lowerEqualizerBandLevel) / 100 + " db");
                    }

                    if (b) {

                        ((MainActivity)getActivity()).getAnglerClient().setEqualizerBandLevel(band, (short)(progress + lowerEqualizerBandLevel));
                    }

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

            seekBar.setProgress(bandsLevel.get(i) - lowerEqualizerBandLevel);
            seekBar.setEnabled(equalizerOnOffState);

            // Setup frequency view
            TextView frequencyView = bandLayout.findViewById(R.id.band_frequency);
            frequencyView.setPadding((int)(4 * MainActivity.density), 0, (int)(4 * MainActivity.density), 0);
            frequencyView.setLines(1);
            frequencyView.setEllipsize(TextUtils.TruncateAt.END);
            frequencyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_text_size));
            frequencyView.setText(bandsFrequencies.get(i) / 1000 + " Hz");

            // Add band to bands layout
            bandsLayout.addView(bandLayout);
        }


        // Setup spinner with array adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.eq_spinner_item, R.id.playlist_spinner_item_title, equalizerPresets);
        adapter.setDropDownViewResource(R.layout.mp_playlist_spinner_dropdown_item);
        presetsSpinner.setAdapter(adapter);
        presetsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i != 0) {

                    try {
                        ((MainActivity)getActivity()).getAnglerClient().setEqualizerPreset((short) (i - 1));
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get preset
        int preset = presenter.getEqualizerPreset();
        presetsSpinner.setSelection(preset);

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

                        for (short i = 0; i < bandsFrequencies.size(); i++){

                            getView().findViewById(i).setEnabled(equalizerOnOffState);
                        }


                        break;

                    case "equalizer_preset_changed":

                        List<Integer> bandsLevel = intent.getIntegerArrayListExtra("bands_level");

                        for (short i = 0; i < bandsLevel.size(); i++){

                            ((SeekBar)getView().findViewById(i)).setProgress(bandsLevel.get(i) - lowerEqualizerBandLevel);
                        }

                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("equalizer_on_off_state_changed");
        intentFilter.addAction("equalizer_preset_changed");

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

        // Save preset
        presenter.saveEqualizerPreset(presetsSpinner.getSelectedItemPosition());

        // Save bands
        List<Short> bandsLevel = new ArrayList<>();

        for (int i = 0; i < bandsFrequencies.size(); i++){
            short level = (short)(((SeekBar)getView().findViewById(i)).getProgress() + lowerEqualizerBandLevel);
            bandsLevel.add(level);
        }

        presenter.saveBandsLevel(bandsLevel);

        presenter.deattachView();
        unbinder.unbind();
    }

}
