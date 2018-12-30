package com.mnemo.angler.ui.main_activity.misc.play_all;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlayAllDialogFragment extends DialogFragment {

    private Unbinder unbinder;

    private String type;
    private String playlist;
    private List<Track> tracks;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Get tracks variables
        type = getArguments().getString("type");
        playlist = getArguments().getString("playlist");
        tracks = getArguments().getParcelableArrayList("tracks");

        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_play_all_context_menu, null, false);

        // Inject views
        unbinder = ButterKnife.bind(this, bodyLayout);

        builder.setView(bodyLayout);

        return builder.create();
    }


    // Setup context menu item listeners
    @OnClick(R.id.play_all_play_now)
    void playNow(){

        ((MainActivity)getActivity()).getAnglerClient().playNow(type, playlist, 0, tracks);
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
