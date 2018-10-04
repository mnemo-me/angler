package com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist;


import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.adapters.AddTracksExpandableListAdapter;

import java.util.HashMap;


public class AddTracksDialogFragment extends DialogFragment implements AddTracksView {

    AddTracksPresenter presenter;

    ExpandableListView expandableListView;
    AddTracksExpandableListAdapter adapter;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Bind Presenter to View
        presenter = new AddTracksPresenter();
        presenter.attachView(this);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Get playlist title
        String title = getArguments().getString("title");

        // Setup header
        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.misc_dialog_title, null, false);
        TextView titleView = titleLayout.findViewById(R.id.dialog_title);
        titleView.setText(R.string.add_tracks_to_playlist);
        builder.setCustomTitle(titleLayout);


        // Setup body
        expandableListView = new ExpandableListView(getContext());
        expandableListView.setDividerHeight(0);
        expandableListView.setSelector(android.R.color.transparent);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            expandableListView.addHeaderView(LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks_header, null, false));
            expandableListView.addFooterView(LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks_header, null, false));
        }

        builder.setView(expandableListView);

        // Load tracks
        presenter.loadTracks(title);


        // Setup buttons
        builder.setPositiveButton(R.string.add, (dialogInterface, i) -> presenter.addTracksToPlaylist(title, adapter.getNewTracks()));

        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

        });


        return builder.create();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
    }



    // MVP View methods
    @Override
    public void setTracks(HashMap<Track, Boolean> checkedTracks) {

        // Setup adapter and bind with ExpandableListView
        adapter = new AddTracksExpandableListAdapter(getContext(), checkedTracks);
        expandableListView.setAdapter(adapter);

        // Set on track count change listener
        adapter.setOnTrackCountChangeListener(() -> {

            if (adapter.getNewTracks().size() == 0){

                ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            }else{

                if (!((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        // Setup expand behavior
        for (int i = 0; i < adapter.getGroupCount(); i++){
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnGroupClickListener((expandableListView, view, i, l) -> {

            if (expandableListView.isGroupExpanded(i)){
                expandableListView.collapseGroup(i);
            }else{
                expandableListView.expandGroup(i);
            }

            return true;
        });
    }

}
