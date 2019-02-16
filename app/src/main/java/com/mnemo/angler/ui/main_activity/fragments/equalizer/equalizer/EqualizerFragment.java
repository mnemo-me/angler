package com.mnemo.angler.ui.main_activity.fragments.equalizer.equalizer;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.audio_effects.AudioEffectsFragment;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.bands.BandsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;


public class EqualizerFragment extends Fragment implements DrawerItem, EqualizerView {

    private EqualizerPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.equalizer_on_off)
    Switch equalizerSwitch;

    @Nullable @BindView(R.id.equalizer_equalizer)
    ImageView equalizer;

    @Nullable @BindView(R.id.equalizer_audio_effects)
    ImageView audioEffects;

    private int orientation;

    public EqualizerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.eq_fragment_equalizer, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Remove fragment from eq_frame_2 in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){

            Fragment fragment = getChildFragmentManager().findFragmentById(R.id.eq_frame_2);

            if (fragment != null) {
                getChildFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Bind Presenter to View
        presenter = new EqualizerPresenter();
        presenter.attachView(this);

        // Setup equalizer on/off switch
        equalizerSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

            try{

            ((MainActivity)getActivity()).getAnglerClient().setEqualizer(b);

            Intent intent = new Intent();
            intent.setAction("equalizer_on_off_state_changed");
            intent.putExtra("equalizer_on_off_state", b);

            getActivity().sendBroadcast(intent);

            }catch (NullPointerException e){
                e.printStackTrace();
            }
        });


        // Get equalizer state
        boolean equalizerState = presenter.getEqualizerState();
        equalizerSwitch.setChecked(equalizerState);

        // Open bands child fragment
        showEqualizerBands();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){

            // Open audio effects child fragment
            showAudioEffects();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.saveEqualizerState(equalizerSwitch.isChecked());

        presenter.deattachView();
        unbinder.unbind();
    }

    // Setup listeners
    @Optional
    @OnClick(R.id.equalizer_equalizer)
    void showEqualizerBands(){

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            audioEffects.setAlpha(0.5f);
            equalizer.setAlpha(1f);
        }

        BandsFragment bandsFragment = new BandsFragment();

        Bundle args = new Bundle();
        args.putBoolean("equalizer_on_off_state", equalizerSwitch.isChecked());
        bandsFragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.eq_frame, bandsFragment)
                .commit();
    }

    @Optional
    @OnClick(R.id.equalizer_audio_effects)
    void showAudioEffects(){

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            equalizer.setAlpha(0.5f);
            audioEffects.setAlpha(1f);
        }

        AudioEffectsFragment audioEffectsFragment = new AudioEffectsFragment();

        Bundle args = new Bundle();
        args.putBoolean("equalizer_on_off_state", equalizerSwitch.isChecked());
        audioEffectsFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            fragmentTransaction.replace(R.id.eq_frame, audioEffectsFragment);
        }else{
            fragmentTransaction.replace(R.id.eq_frame_2, audioEffectsFragment);
        }

        fragmentTransaction.commit();
    }

    // Setup drawer menu button
    @OnClick(R.id.equalizer_drawer_back)
    void back(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }
}
