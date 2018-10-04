package com.mnemo.angler.ui.main_activity.misc.cover;


import android.app.Dialog;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.util.ImageAssistant;



public class CoverDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        CardView cardView = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.misc_cover, null, false);

        // Get artist and image (album optional)
        String image = getArguments().getString("image");
        String artist = getArguments().getString("artist");
        String album = getArguments().getString("album");

        // Initialize views
        ImageView coverView = cardView.findViewById(R.id.artist_cover_image);
        TextView artistView = cardView.findViewById(R.id.artist_cover_artist);

        // Fill views
        ImageAssistant.loadImage(getContext(), image, coverView, 340);
        artistView.setText(artist);

        // Fill album (optional)
        if (album != null){
            TextView albumView = cardView.findViewById(R.id.artist_cover_album);
            albumView.setText(album);
        }

        builder.setView(cardView);

        return builder.create();

    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog)getDialog();
        alertDialog.getWindow().setLayout((int)(392 * MainActivity.density), (int)(392 * MainActivity.density));
    }
}
