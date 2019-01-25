package com.mnemo.angler.ui.main_activity.misc.welcome;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;

public class WelcomeDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_fragment_welcome, null, false);

        // Setup buttons
        TextView freeTrialButton = bodyLayout.findViewById(R.id.welcome_free_trial);
        freeTrialButton.setOnClickListener(v -> {

            dismiss();
        });

        TextView purchaseButton = bodyLayout.findViewById(R.id.welcome_purchase);
        purchaseButton.setOnClickListener(v -> {

            dismiss();
        });

        builder.setView(bodyLayout);

        return builder.create();
    }

}
