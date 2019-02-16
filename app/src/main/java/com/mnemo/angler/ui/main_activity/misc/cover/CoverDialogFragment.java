package com.mnemo.angler.ui.main_activity.misc.cover;


import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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

        Point size = new Point();
        alertDialog.getWindow().getWindowManager().getDefaultDisplay().getSize(size);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            alertDialog.getWindow().setLayout(size.x, size.x);
        }else {
            alertDialog.getWindow().setLayout((int) (size.y - 24 * MainActivity.density), (int) (size.y - 24 * MainActivity.density));
        }
    }
}
