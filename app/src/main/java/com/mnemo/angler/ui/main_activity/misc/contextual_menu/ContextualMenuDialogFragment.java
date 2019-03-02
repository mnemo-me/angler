package com.mnemo.angler.ui.main_activity.misc.contextual_menu;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration.AlbumConfigurationFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration.ArtistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ContextualMenuDialogFragment extends BottomSheetDialogFragment implements ContextualMenuView{

    private ContextualMenuPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.context_menu_title)
    TextView titleView;

    @BindView(R.id.context_menu_artist)
    TextView artistView;

    @BindView(R.id.context_menu_album_cover)
    ImageView albumCoverView;

    @BindView(R.id.context_menu_go_to_artist)
    TextView contextMenuGoToArtist;

    @BindView(R.id.context_menu_go_to_album)
    TextView contextMenuGoToAlbum;

    @BindView(R.id.context_menu_remove_from_playlist)
    TextView contextMenuRemoveFromPlaylist;

    private String type;
    private String playlist;
    private String albumCover;
    private Track track;
    private List<Track> tracks;

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

        View view = inflater.inflate(R.layout.misc_track_context_menu, container, false);

        // Get variables
        type = getArguments().getString("type");
        playlist = getArguments().getString("playlist");
        albumCover = getArguments().getString("album_cover");
        track = getArguments().getParcelable("track");
        tracks = getArguments().getParcelableArrayList("tracks");

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Fill text views
        titleView.setText(track.getTitle());
        artistView.setText(track.getArtist());

        // Set visibility of optional actions
        switch (type){

            case "music_player":

                contextMenuGoToArtist.setVisibility(View.VISIBLE);
                contextMenuGoToAlbum.setVisibility(View.VISIBLE);

                break;

            case "playlist":

                contextMenuGoToArtist.setVisibility(View.VISIBLE);
                contextMenuGoToAlbum.setVisibility(View.VISIBLE);
                contextMenuRemoveFromPlaylist.setVisibility(View.VISIBLE);

                break;


            case "playlist(land)":

                contextMenuGoToArtist.setVisibility(View.VISIBLE);
                contextMenuGoToAlbum.setVisibility(View.VISIBLE);
                contextMenuRemoveFromPlaylist.setVisibility(View.VISIBLE);

                break;

            case "album":

                contextMenuGoToArtist.setVisibility(View.VISIBLE);

                break;

            case "artist":

                contextMenuGoToAlbum.setVisibility(View.VISIBLE);

                break;
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ContextualMenuPresenter();
        presenter.attachView(this);

        // Fill album cover
        if (presenter.checkAlbumCoverExist(track.getArtist(), track.getAlbum())){
            ImageAssistant.loadImage(getContext(), albumCover, albumCoverView, 80);
        }else{
            ImageAssistant.loadImage(getContext(), "R.drawable.black_logo", albumCoverView, 80);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }

    // Context menu item listeners
    @OnClick(R.id.context_menu_play)
    void play(){

        ((MainActivity) getContext()).getAnglerClient().playNow(type, playlist, tracks.indexOf(track), tracks);
        new Handler().postDelayed(this::dismiss, 300);
    }

    @OnClick(R.id.context_menu_play_next)
    void playNext(){

        ((MainActivity) getContext()).getAnglerClient().addToQueue(track, playlist, true);
        new Handler().postDelayed(this::dismiss, 300);
    }

    @OnClick(R.id.context_menu_add_to_queue)
    void addToQueue(){

        ((MainActivity) getContext()).getAnglerClient().addToQueue(track, playlist, false);
        new Handler().postDelayed(this::dismiss, 300);
    }

    @OnClick(R.id.context_menu_add_to_playlist)
    void addToPlaylist(){

        new Handler().postDelayed(() -> {
            dismiss();

            AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();

            Bundle args = new Bundle();
            args.putParcelable("track", track);
            addTrackToPlaylistDialogFragment.setArguments(args);

            addTrackToPlaylistDialogFragment.show(((MainActivity) getContext()).getSupportFragmentManager(), "add_track_to_playlist_dialog_fragment");

        }, 300);
    }

    // Optional items
    @OnClick(R.id.context_menu_go_to_artist)
    void goToArtist(){

        String artistImagePath = presenter.getArtistImagePath(track.getArtist());

        ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

        Bundle args = new Bundle();
        args.putString("image", artistImagePath);
        args.putString("artist", track.getArtist());
        artistConfigurationFragment.setArguments(args);


        new Handler().postDelayed(() -> {
            dismiss();

            if (((MainActivity) getContext()).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                ((MainActivity) getContext()).findViewById(R.id.main_frame).setVisibility(View.GONE);
            }

            ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, artistConfigurationFragment, "artist_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        }, 300);
    }


    @OnClick(R.id.context_menu_go_to_album)
    void goToAlbum(){

        String albumImagePath = presenter.getAlbumImagePath(track.getArtist(), track.getAlbum());

        AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

        Bundle args = new Bundle();
        args.putString("image", albumImagePath);
        args.putString("album_name", track.getAlbum());
        args.putString("artist", track.getArtist());
        albumConfigurationFragment.setArguments(args);


        new Handler().postDelayed(() -> {
            dismiss();

            if (((MainActivity) getContext()).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                ((MainActivity) getContext()).findViewById(R.id.main_frame).setVisibility(View.GONE);
            }

            ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, albumConfigurationFragment, "album_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        }, 300);
    }

    @OnClick(R.id.context_menu_remove_from_playlist)
    void deleteTrack(){

        presenter.deleteTrack(playlist, track.get_id());

        if (playlist.equals(((MainActivity)getActivity()).getAnglerClient().getQueueTitle())) {
            ((MainActivity) getActivity()).getAnglerClient().removeQueueItemAt(tracks.indexOf(track));
        }
    }


    // MVP View methods
    @Override
    public void showDeleteTrackSnackbar(String playlist, String trackId, int position) {

        // Show snackbar
        Snackbar snackbar = Snackbar.make(((MainActivity)getContext()).findViewById(R.id.frame), R.string.track_removed, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, snackbarView -> {

            // Undo deleting
            presenter.restoreTrack(playlist, trackId, position);
/*
            // Restore track in playlist queue if needed
            if (playlist.equals(((MainActivity)getActivity()).getAnglerClient().getQueueTitle())){
                ((MainActivity)getActivity()).getAnglerClient().addToPlaylistQueue(position, Collections.singletonList(track));
            }*/

        });

        snackbar.show();
        dismiss();
    }


}
