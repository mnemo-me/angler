package com.mnemo.angler.ui.main_activity.misc;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContextualMenuDialogFragment extends Fragment {


    public ContextualMenuDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
/*
        // Contextual menu

        // Play
        TextView contextMenuPlay = bodyLinearLayout.findViewById(R.id.context_menu_play);
        contextMenuPlay.setOnClickListener(view16 -> {

            ((MainActivity)mContext).getAnglerClient().playNow(localPlaylistName, position, cursor);

            new Handler().postDelayed(() -> dialog.dismiss(), 300);

        });

        // Play next
        TextView contextMenuPlayNext = bodyLinearLayout.findViewById(R.id.context_menu_play_next);
        contextMenuPlayNext.setOnClickListener(view15 -> {

            ((MainActivity)mContext).getAnglerClient().addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), true);

            new Handler().postDelayed(() -> dialog.dismiss(), 300);

        });

        // Add to queue
        TextView contextMenuAddToQueue = bodyLinearLayout.findViewById(R.id.context_menu_add_to_queue);
        contextMenuAddToQueue.setOnClickListener(view14 -> {

            ((MainActivity)mContext).getAnglerClient().addToQueue(MediaAssistant.mergeMediaDescription(id, title, artist, album, duration, uri, localPlaylistName), false);

            new Handler().postDelayed(() -> dialog.dismiss(), 300);

        });


        // Lyrics
        TextView contextMenuLyrics = bodyLinearLayout.findViewById(R.id.context_menu_lyrics);
        contextMenuLyrics.setOnClickListener(view13 -> new Handler().postDelayed(() -> {
            dialog.dismiss();

            LyricsDialogFragment lyricsDialogFragment = new LyricsDialogFragment();
            lyricsDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Lyrics dialog");

        }, 300));


        // Go to artist
        TextView contextMenuGoToArtist = bodyLinearLayout.findViewById(R.id.context_menu_go_to_artist);
        contextMenuGoToArtist.setOnClickListener(view12 -> {

            String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

            final ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("image", imagePath);
            args.putString("artist", artist);
            artistConfigurationFragment.setArguments(args);


            new Handler().postDelayed(() -> {
                dialog.dismiss();

                if (((MainActivity)mContext).findViewById(R.id.main_frame).getVisibility() == View.VISIBLE) {
                    ((MainActivity) mContext).findViewById(R.id.main_frame).setVisibility(View.GONE);
                }

                ((AppCompatActivity) AlbumCursorAdapter.this.mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame,artistConfigurationFragment, "artist_conf_fragment")
                        .addToBackStack(null)
                        .commit();
            }, 300);

        });


        // Add to playlist
        TextView contextMenuAdd = bodyLinearLayout.findViewById(R.id.context_menu_add);
        contextMenuAdd.setOnClickListener(view1 -> new Handler().postDelayed(() -> {
            dialog.dismiss();

            AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();

            Bundle args = MediaAssistant.putMetadataInBundle(id, title, artist, album, duration, uri);
            addTrackToPlaylistDialogFragment.setArguments(args);

            addTrackToPlaylistDialogFragment.show(((AppCompatActivity)AlbumCursorAdapter.this.mContext).getSupportFragmentManager(), "Add track to playlist dialog");

        }, 300));


        // Set visibility of contextual menu items
        contextMenuGoToArtist.setVisibility(View.VISIBLE);
*/

        return textView;
    }

}
