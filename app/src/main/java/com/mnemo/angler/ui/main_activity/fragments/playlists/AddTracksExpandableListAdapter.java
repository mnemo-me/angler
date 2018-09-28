package com.mnemo.angler.ui.main_activity.fragments.playlists;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;
import java.util.Collections;

public class AddTracksExpandableListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private ArrayList<Track> tracksToAdd;
    private ArrayList<String> artists;
    private ArrayList<Track> newTracks;
    private ArrayList<String> artistChecked;
    private ArrayList<String> mutedArtists;

    private OnTrackCountChangeListener onTrackCountChangeListener;

    public interface OnTrackCountChangeListener{

        void onTrackCountChange();
    }

    AddTracksExpandableListAdapter(Context context, ArrayList<Track> tracksToAdd) {
        this.context = context;
        this.tracksToAdd = tracksToAdd;

        artists = new ArrayList<>();
        for (Track track : tracksToAdd){
            if (!artists.contains(track.getArtist())){
                artists.add(track.getArtist());
            }
        }
        Collections.sort(artists);

        artistChecked = new ArrayList<>();
        mutedArtists = new ArrayList<>();
        addPreCheckedArtists();

        newTracks = new ArrayList<>();
    }

    @Override
    public int getGroupCount() {
        return artists.size();
    }

    @Override
    public int getChildrenCount(int i) {

        int count = 0;

        for (Track track : tracksToAdd){
            if (track.getArtist().equals(artists.get(i))){
                count++;
            }
        }

        return count;
    }

    @Override
    public Object getGroup(int i) {
        return artists.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {

        ArrayList<Track> artistTracks = new ArrayList<>();

        for (Track track : tracksToAdd){
            if (track.getArtist().equals(artists.get(i))){
                artistTracks.add(track);
            }
        }

        Collections.sort(artistTracks);

        return artistTracks.get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pm_add_track_d, viewGroup, false);

        CheckBox checkBox = linearLayout.findViewById(R.id.add_track_artist);

        final String artist = artists.get(i);

        checkBox.setText(artist);

        if (artistChecked.contains(artist)){
            checkBox.setChecked(true);
        }

        if (mutedArtists.contains(artist)){
            checkBox.setEnabled(false);
        }

        checkBox.setOnCheckedChangeListener((compoundButton, b1) -> {


            if (b1){
                if (!artistChecked.contains(artist)) {
                    artistChecked.add(artist);

                    for (Track track : tracksToAdd){
                        if (track.getArtist().equals(artist)){
                            if (!newTracks.contains(track)){
                                newTracks.add(track);
                            }
                        }
                    }
                }
                notifyDataSetChanged();
            }else{
                artistChecked.remove(artist);
                for (Track track : tracksToAdd){
                    if (track.getArtist().equals(artist)){
                        newTracks.remove(track);
                    }
                }
                notifyDataSetChanged();

            }

            onTrackCountChangeListener.onTrackCountChange();
        });
        return linearLayout;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pm_add_track_d, viewGroup, false);
        linearLayout.setPadding((int)(62 * MainActivity.density), 0, 0, 0);

        final String artist = artists.get(i);

        CheckBox checkBox = linearLayout.findViewById(R.id.add_track_artist);
        final Track track = (Track)getChild(i, i1);

        checkBox.setText(track.getTitle());

        checkBox.setChecked(track.isAlreadyAdded());

        if (!track.isAlreadyAdded()){
            checkBox.setChecked(newTracks.contains(track));
        }

        checkBox.setEnabled(!track.isAlreadyAdded());

        checkBox.setOnCheckedChangeListener((compoundButton, b1) -> {


            if (b1){
                if (!newTracks.contains(track)) {
                    newTracks.add(track);

                    boolean isArtistNeedToAddInList = true;

                    for (Track trackForCheck : tracksToAdd){
                        if (trackForCheck.getArtist().equals(artist)){
                            if (!newTracks.contains(trackForCheck)){
                                isArtistNeedToAddInList = false;
                                break;
                            }
                        }
                    }

                    if (isArtistNeedToAddInList && !artistChecked.contains(artist)){
                        artistChecked.add(artist);
                        notifyDataSetChanged();
                    }
                }
            }else{
                newTracks.remove(track);
                if (artistChecked.contains(artist)){
                    artistChecked.remove(artist);
                    notifyDataSetChanged();
                }
            }

            onTrackCountChangeListener.onTrackCountChange();
        });

        return linearLayout;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    ArrayList<Track> getNewTracks() {
        return newTracks;
    }

    void setOnTrackCountChangeListener(OnTrackCountChangeListener trackCountChangeListener) {
        this.onTrackCountChangeListener = trackCountChangeListener;
    }

    private void addPreCheckedArtists(){

        for (String artist : artists) {

            boolean isArtistNeedToAddInList = true;

            for (Track trackForCheck : tracksToAdd) {
                if (trackForCheck.getArtist().equals(artist)) {
                    if (!trackForCheck.isAlreadyAdded()) {
                        isArtistNeedToAddInList = false;
                        break;
                    }
                }
            }

            if (isArtistNeedToAddInList) {
                artistChecked.add(artist);
                mutedArtists.add(artist);
            }
        }

    }
}
