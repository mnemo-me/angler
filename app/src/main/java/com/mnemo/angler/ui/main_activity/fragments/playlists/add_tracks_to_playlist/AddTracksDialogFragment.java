package com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist;


import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.AddTracksExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class AddTracksDialogFragment extends DialogFragment implements AddTracksView {

    private AddTracksPresenter presenter;

    private TextView tracksCountView;

    private ExpandableListView expandableListView;
    private AddTracksExpandableListAdapter adapter;

    private ArrayList<Track> newTracks;
    private boolean[] expandedGroups;

    private TextView addButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Bind Presenter to View
        presenter = new AddTracksPresenter();
        presenter.attachView(this);


        // Get playlist title
        String title = getArguments().getString("title");

        // Get saved new tracks
        if (savedInstanceState != null){
            newTracks = savedInstanceState.getParcelableArrayList("new_tracks");
            expandedGroups = savedInstanceState.getBooleanArray("expanded_groups");
        }

        // Setup header
        ConstraintLayout titleLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks_header, null, false);
        tracksCountView = titleLayout.findViewById(R.id.add_tracks_count);
        builder.setCustomTitle(titleLayout);


        // Setup body
        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks, null, false);
        expandableListView = bodyLayout.findViewById(R.id.add_tracks_expandable_list);
        expandableListView.setDividerHeight(0);
        expandableListView.setSelector(android.R.color.transparent);

        // Setup buttons
        TextView closeButton = bodyLayout.findViewById(R.id.add_tracks_close);
        closeButton.setOnClickListener(view -> dismiss());

        addButton = bodyLayout.findViewById(R.id.add_tracks_add);
        addButton.setOnClickListener(view -> {
            presenter.addTracksToPlaylist(title, adapter.getNewTracks());

            if (title.equals(((MainActivity)getActivity()).getAnglerClient().getQueueTitle())){
                ((MainActivity)getActivity()).getAnglerClient().addToPlaylistQueue(adapter.getNewTracks());
            }

            dismiss();
        });

        builder.setView(bodyLayout);

        // Load tracks
        presenter.loadTracks(title);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("new_tracks", adapter.getNewTracks());
        outState.putBooleanArray("expanded_groups", adapter.getExpandedGroups());
        outState.putInt("first_visible_position", expandableListView.getFirstVisiblePosition());
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

        // Add saved new tracks
        if (newTracks != null){
            adapter.setNewTracks(newTracks);

            if (newTracks.size() > 0){

                tracksCountView.setText(String.valueOf(newTracks.size()));

                addButton.setEnabled(true);
                addButton.setAlpha(1f);
            }
        }

        // Initialize expanded groups
        if (expandedGroups == null) {

            expandedGroups = new boolean[adapter.getGroupCount()];

            for (int i = 0; i < expandedGroups.length; i ++){
                expandedGroups[i] = true;
            }
        }

        adapter.setExpandedGroups(expandedGroups);

        expandableListView.setAdapter(adapter);

        // Set on track count change listener
        adapter.setOnTrackCountChangeListener(() -> {

            if (adapter.getNewTracks().size() == 0){

                tracksCountView.setText("");

                addButton.setEnabled(false);
                addButton.setAlpha(0.3f);

            }else{


                tracksCountView.setText(String.valueOf(adapter.getNewTracks().size()));

                if (!addButton.isEnabled()) {

                    addButton.setEnabled(true);
                    addButton.setAlpha(1f);
                }
            }
        });

        // Setup expand behavior
        for (int i = 0; i < expandedGroups.length; i++){
            if (expandedGroups[i]) {
                expandableListView.expandGroup(i);
            }
        }
        adapter.setOnGroupExpandListener((position, isExpand) -> {

            if (isExpand){
                expandableListView.expandGroup(position);
            }else{
                expandableListView.collapseGroup(position);
            }
        });

        //expandableListView.smoothScrollToPositionFromTop(firstVisiblePosition, 0, 0);
    }

}
