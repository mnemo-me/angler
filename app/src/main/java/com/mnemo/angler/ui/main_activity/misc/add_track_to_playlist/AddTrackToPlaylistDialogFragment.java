package com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist;


import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mnemo.angler.data.database.Entities.Playlist;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.AddTrackToPlaylistAdapter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AddTrackToPlaylistDialogFragment extends BottomSheetDialogFragment implements AddTrackToPlaylistView  {

    private AddTrackToPlaylistPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.add_track_to_playlist_recycler_view)
    RecyclerView recyclerView;

    private Track track;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Configure bottom sheet dialog behavior
        BottomSheetDialog dialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {

            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;

            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);

        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.misc_add_track_to_playlist_dialog, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get track
        track = getArguments().getParcelable("track");

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 3);
        }else{
            gridLayoutManager = new GridLayoutManager(getContext(), 5);
        }

        recyclerView.setLayoutManager(gridLayoutManager);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new AddTrackToPlaylistPresenter();
        presenter.attachView(this);

        // Load playlists
        presenter.loadPlaylistsAndTitlesWithTrack(track.get_id());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void setPlaylists(List<Playlist> playlists, List<String> playlistsWithTrack) {

        // Fix bottom padding (if needed)
        int playlistsOnScreen;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            playlistsOnScreen = 8;
        }else{
            playlistsOnScreen = 4;
        }

        if (playlists.size() > playlistsOnScreen){
            recyclerView.setPadding((int)(12 * MainActivity.density), (int)(12 * MainActivity.density), (int)(4 * MainActivity.density), (int)(60 * MainActivity.density));
        }


        AddTrackToPlaylistAdapter adapter = new AddTrackToPlaylistAdapter(getContext(), playlists, playlistsWithTrack);
        adapter.setOnAddTrackToPlaylistListener(playlist -> {

            if (playlist.equals(getResources().getString(R.string.create_new_playlist))){

                PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

                Bundle args = getArguments();
                args.putString("action", "create");
                args.putParcelable("track", track);
                playlistCreationDialogFragment.setArguments(args);

                playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
            }else {

                presenter.addTrackToPlaylist(playlist, track);
                Toast.makeText(getContext(), "'" + track.getArtist() + " - " + track.getTitle() + "' " + getString(R.string.added_to) + " '" + playlist + "'",
                        Toast.LENGTH_SHORT).show();
            }
                dismiss();

        });

        recyclerView.setAdapter(adapter);
    }

}
