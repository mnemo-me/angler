package com.mnemo.angler.ui.main_activity.misc;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.mnemo.angler.R;


public class LyricsDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("In development");

        builder.setNegativeButton(R.string.close, (dialogInterface, i) -> {

        });

        return builder.create();
    }
}
