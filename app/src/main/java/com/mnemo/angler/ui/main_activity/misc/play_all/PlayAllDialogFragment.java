package com.mnemo.angler.ui.main_activity.misc.play_all;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlayAllDialogFragment extends BottomSheetDialogFragment {

    Unbinder unbinder;

    String playlist;
    List<Track> tracks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.misc_play_all_context_menu, container, false);

        // Get tracks variables
        playlist = getArguments().getString("playlist");
        tracks = getArguments().getParcelableArrayList("tracks");

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    // Setup context menu item listeners
    @OnClick(R.id.play_all_play_now)
    void playNow(){

        ((MainActivity)getActivity()).getAnglerClient().playNow(playlist, 0, tracks);
        new Handler().postDelayed(this::dismiss, 300);
    }

    @OnClick(R.id.play_all_play_next)
    void playNext(){

        ((MainActivity)getActivity()).getAnglerClient().addToQueue(playlist, tracks, true);
        new Handler().postDelayed(this::dismiss, 300);
    }

    @OnClick(R.id.play_all_add_to_queue)
    void addToQueue(){

        ((MainActivity)getActivity()).getAnglerClient().addToQueue(playlist, tracks, false);
        new Handler().postDelayed(this::dismiss, 300);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }
}
