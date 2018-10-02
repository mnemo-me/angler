package com.mnemo.angler.ui.main_activity.misc;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayAllDialogFragment extends Fragment {


    public PlayAllDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);



    /*
    // Play all button
    void playAll(final Cursor data){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LinearLayout bodyLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pm_play_all_context_menu, null, false);
        builder.setView(bodyLayout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Contextual menu

        // Play now
        TextView playNow = bodyLayout.findViewById(R.id.play_all_play_now);
        playNow.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().playNow(localPlaylistName, 0, data);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });

        // Play next
        TextView playNext = bodyLayout.findViewById(R.id.play_all_play_next);
        playNext.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().addToQueue(localPlaylistName, data, true);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });

        // Add to queue
        TextView addToQueue = bodyLayout.findViewById(R.id.play_all_add_to_queue);
        addToQueue.setOnClickListener(view -> {

            ((MainActivity)getActivity()).getAnglerClient().addToQueue(localPlaylistName, data, false);
            new Handler().postDelayed(() -> dialog.dismiss(), 300);
        });
    }*/







/*
    @Override
    public void onTrackRemove(final int position , final Track trackToRemove, final boolean isCurrentTrack) {



        Snackbar snackbar = Snackbar.make(getView(), R.string.track_removed, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, view -> {

            ContentValues contentValues = new ContentValues();
            contentValues.put("_id", trackToRemove.getId());
            contentValues.put(TrackEntry.COLUMN_TITLE, trackToRemove.getTitle());
            contentValues.put(TrackEntry.COLUMN_ARTIST, trackToRemove.getArtist());
            contentValues.put(TrackEntry.COLUMN_ALBUM, trackToRemove.getAlbum());
            contentValues.put(TrackEntry.COLUMN_DURATION, trackToRemove.getDuration());
            contentValues.put(TrackEntry.COLUMN_URI, trackToRemove.getUri());
            contentValues.put(TrackEntry.COLUMN_POSITION, position);


            getActivity().getContentResolver().insert(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName), contentValues);

            adapter.incrementPositions(position - 1);



            if (AnglerService.mPlaylistManager.getCurrentPosition() >= position - 1) {
                AnglerService.mPlaylistManager.incrementCurrentPosition();
            }

            if (PlaybackManager.isCurrentTrackHidden && AnglerService.mPlaylistManager.getCurrentPosition() == position - 2){
                AnglerService.mPlaylistManager.incrementCurrentPosition();
                PlaybackManager.isCurrentTrackHidden = false;
            }


        });
        snackbar.show();
    }*/



        return textView;
    }

}
