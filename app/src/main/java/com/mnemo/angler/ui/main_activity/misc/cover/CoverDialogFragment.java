package com.mnemo.angler.ui.main_activity.misc.cover;


import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.util.ImageAssistant;



public class CoverDialogFragment extends DialogFragment {

    private Bundle imageSize;
    private int maxImageHeight;
    private int maxImageWidth;

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

        // Set maximum size
        maxImageHeight = (int)(getResources().getConfiguration().screenHeightDp * MainActivity.density - 16 * MainActivity.density);
        coverView.setMaxHeight(maxImageHeight);

        maxImageWidth = (int)(getResources().getConfiguration().screenWidthDp * MainActivity.density - 16 * MainActivity.density);
        coverView.setMaxWidth(maxImageWidth);

        // Fill views
        imageSize = ImageAssistant.loadCoverImage(getContext(), image, coverView);
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

        int imageHeight = imageSize.getInt("image_height");
        int imageWidth = imageSize.getInt("image_width");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (imageHeight < maxImageHeight) {
                alertDialog.getWindow().setLayout(imageWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
            }else{
                alertDialog.getWindow().setLayout(FrameLayout.LayoutParams.WRAP_CONTENT, maxImageHeight);
            }
        }else {
            if (imageWidth < maxImageWidth) {
                alertDialog.getWindow().setLayout(imageWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
            }else{
                alertDialog.getWindow().setLayout(maxImageWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
            }
        }
    }
}
