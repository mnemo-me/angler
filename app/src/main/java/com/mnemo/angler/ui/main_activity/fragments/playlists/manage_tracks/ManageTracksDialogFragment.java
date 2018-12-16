package com.mnemo.angler.ui.main_activity.fragments.playlists.manage_tracks;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.queue.DragAndDropCallback;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.ManageableTrackAdapter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_clear.PlaylistClearDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class ManageTracksDialogFragment extends DialogFragment implements ManageTracksView{

    ManageTracksPresenter presenter;

    RecyclerView recyclerView;
    ManageableTrackAdapter adapter;

    TextView saveButton;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Bind Presenter to View
        presenter = new ManageTracksPresenter();
        presenter.attachView(this);

        // Get playlist title
        String title = getArguments().getString("title");


        // Setup body
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_fragment_manage_tracks_dialog, null, false);
        recyclerView = bodyLayout.findViewById(R.id.manage_tracks_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Setup buttons
        ImageButton clearPlaylistButton = bodyLayout.findViewById(R.id.manage_tracks_clear_button);
        clearPlaylistButton.setOnClickListener(view -> {

            PlaylistClearDialogFragment playlistClearDialogFragment = new PlaylistClearDialogFragment();

            Bundle args = new Bundle();
            args.putString("title", title);
            playlistClearDialogFragment.setArguments(args);

            playlistClearDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_clear_dialog_fragment");
        });

        TextView cancelButton = bodyLayout.findViewById(R.id.manage_tracks_cancel);
        cancelButton.setOnClickListener(view -> dismiss());

        saveButton = bodyLayout.findViewById(R.id.manage_tracks_save);
        saveButton.setOnClickListener(view -> {

            List<Track> tracks = adapter.getTracks();

            presenter.saveTracks(title, tracks);

            // Update queue (if playlist is current playing)
            if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(title)){

                String mediaId = ((MainActivity)getActivity()).getAnglerClient().getCurrentMediaId();

                ((MainActivity)getActivity()).getAnglerClient().clearQueue();

                ((MainActivity)getActivity()).getAnglerClient().addToPlaylistQueue(tracks);

                for (int i = 0; i < tracks.size(); i++){

                    if (tracks.get(i).get_id().equals(mediaId)){
                        ((MainActivity)getActivity()).getAnglerClient().setQueuePosition(i);

                        dismiss();
                        return;
                    }
                }

                ((MainActivity)getActivity()).getAnglerClient().setQueuePosition(-1);
            }

            dismiss();
        });

        builder.setView(bodyLayout);


        // Load tracks
        if (savedInstanceState != null) {
            setTracks(savedInstanceState.getParcelableArrayList("tracks"));

            if (savedInstanceState.getBoolean("tracks_changed")){
                saveButton.setEnabled(true);
                saveButton.setAlpha(1f);
            }

        }else {
            presenter.loadTracks(title);
        }

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "playlist_cleared":

                        dismiss();

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("playlist_cleared");

        getContext().registerReceiver(receiver, intentFilter);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) adapter.getTracks());
        outState.putBoolean("tracks_changed", saveButton.isEnabled());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void setTracks(List<Track> tracks) {

        adapter = new ManageableTrackAdapter(tracks);

        // Set drag'n'drop callback
        DragAndDropCallback dragAndDropCallback = new DragAndDropCallback();
        dragAndDropCallback.setOnDragAndDropListener(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragAndDropCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.setOnTracksChangeListener(() -> {
            if (!saveButton.isEnabled()){
                saveButton.setEnabled(true);
                saveButton.setAlpha(1f);
            }
        });

        recyclerView.setAdapter(adapter);
    }
}
