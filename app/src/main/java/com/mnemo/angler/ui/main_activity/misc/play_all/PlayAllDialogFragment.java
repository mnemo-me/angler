package com.mnemo.angler.ui.main_activity.misc.play_all;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.util.List;


public class PlayAllDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get tracks variables
        String playlist = getArguments().getString("playlist");
        List<Track> tracks = getArguments().getParcelableArrayList("tracks");


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_play_all_context_menu, null, false);

        // Contextual menu
        // Play now
        TextView playNow = bodyLayout.findViewById(R.id.play_all_play_now);
        playNow.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().playNow(playlist, 0, tracks);
            new Handler().postDelayed(this::dismiss, 300);
        });

        // Play next
        TextView playNext = bodyLayout.findViewById(R.id.play_all_play_next);
        playNext.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().addToQueue(playlist, tracks, true);
            new Handler().postDelayed(this::dismiss, 300);
        });

        // Add to queue
        TextView addToQueue = bodyLayout.findViewById(R.id.play_all_add_to_queue);
        addToQueue.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().addToQueue(playlist, tracks, false);
            new Handler().postDelayed(this::dismiss, 300);
        });

        builder.setView(bodyLayout);


        return builder.create();
    }

}
