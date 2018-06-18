package com.mnemo.angler.background_changer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;

import java.io.File;

/*
Fragment that waiting user for confirm deleting background image
 */
public class BackgroundDeleteFragment extends Fragment {


    public BackgroundDeleteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_background_delete, container, false);

        final String image = getArguments().getString("image");

        // Setup yes/no buttons
        TextView no = view.findViewById(R.id.background_delete_no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        TextView yes = view.findViewById(R.id.background_delete_yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File fileToDeletePort = new File(image);
                fileToDeletePort.delete();

                File fileToDeleteLand = new File(image.replace("port","land"));
                fileToDeleteLand.delete();

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame, new BackgroundChangerFragmentv2())
                        .addToBackStack(null)
                        .commit();

            }
        });

        return view;
    }

}
