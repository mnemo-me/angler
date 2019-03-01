package com.mnemo.angler.ui.main_activity.misc.trial_expired;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

public class TrialExpiredDialogFragment extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_fragment_trial_expired, null, false);

        // Setup buttons
        TextView quitButton = bodyLayout.findViewById(R.id.trial_expired_quit);
        quitButton.setOnClickListener(v -> getActivity().finish());

        TextView purchaseButton = bodyLayout.findViewById(R.id.trial_expired_purchase);
        purchaseButton.setOnClickListener(v -> ((MainActivity)getActivity()).purchaseAngler());

        builder.setView(bodyLayout);


        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        getDialog().setCanceledOnTouchOutside(false);


        return view;
    }
}
