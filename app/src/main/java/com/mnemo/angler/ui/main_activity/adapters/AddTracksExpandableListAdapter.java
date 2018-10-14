package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AddTracksExpandableListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private HashMap<Track, Boolean> checkedTracks;
    private ArrayList<String> artists;
    private ArrayList<String> artistChecked;
    private ArrayList<String> mutedArtists;
    private ArrayList<Track> newTracks;
    private boolean[] expandedGroups;


    private OnTrackCountChangeListener onTrackCountChangeListener;
    private OnGroupExpandListener onGroupExpandListener;

    public interface OnTrackCountChangeListener{

        void onTrackCountChange();
    }

    public interface OnGroupExpandListener{

        void onGroupExpand(int position, boolean isExpand);
    }

    public AddTracksExpandableListAdapter(Context context, HashMap<Track, Boolean> checkedTracks) {
        this.context = context;
        this.checkedTracks = checkedTracks;

        // Get artist list
        artists = new ArrayList<>();
        for (Track track : checkedTracks.keySet()){
            if (!artists.contains(track.getArtist())){
                artists.add(track.getArtist());
            }
        }
        Collections.sort(artists);

        // Check artists
        artistChecked = new ArrayList<>();
        mutedArtists = new ArrayList<>();
        addPreCheckedArtists();

        // Initialize track list to insert
        newTracks = new ArrayList<>();
    }

    @Override
    public int getGroupCount() {
        return artists.size();
    }

    @Override
    public int getChildrenCount(int i) {

        int count = 0;

        for (Track track : checkedTracks.keySet()){
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

        for (Track track : checkedTracks.keySet()){
            if (track.getArtist().equals(artists.get(i))){
                artistTracks.add(track);
            }
        }
        artistTracks.sort(Comparator.comparing(Track::getTitle));

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

        // Inflate artist view
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pm_add_track_artist, viewGroup, false);

        // Get artist variable
        String artist = artists.get(i);

        // Setup expand button
        ImageView expandButton = linearLayout.findViewById(R.id.add_track_expand_button);

        if (expandedGroups[i]){
            expandButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_18dp);
        }else {
            expandButton.setImageResource(R.drawable.baseline_keyboard_arrow_right_white_18dp);
        }

        expandButton.setOnClickListener(expView -> {

            expandedGroups[i] = !expandedGroups[i];
            onGroupExpandListener.onGroupExpand(i, expandedGroups[i]);

            if (expandedGroups[i]){
                expandButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_white_18dp);
            }else {
                expandButton.setImageResource(R.drawable.baseline_keyboard_arrow_right_white_18dp);
            }
        });

        // Setup checkbox
        CheckBox checkBox = linearLayout.findViewById(R.id.add_track_artist);
        checkBox.setText(artist);

        // Set checked
        if (artistChecked.contains(artist)){
            checkBox.setChecked(true);
        }

        // Set muted
        if (mutedArtists.contains(artist)){
            checkBox.setEnabled(false);
        }

        // Set listener
        checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {

            if (checked){

                if (!artistChecked.contains(artist)) {
                    artistChecked.add(artist);

                    for (Track track : checkedTracks.keySet()){
                        if (track.getArtist().equals(artist)){
                            if (!newTracks.contains(track) && !checkedTracks.get(track)){
                                newTracks.add(track);
                            }
                        }
                    }
                }

                notifyDataSetChanged();

            }else{

                artistChecked.remove(artist);
                for (Track track : checkedTracks.keySet()){
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

        // Inflate track view
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.pm_add_track_title, viewGroup, false);

        // Get artist and track variables
        String artist = artists.get(i);
        Track track = (Track)getChild(i, i1);

        // Setup track checkbox
        CheckBox checkBox = linearLayout.findViewById(R.id.add_track_title);
        checkBox.setText(track.getTitle());

        // Set checked and muted for already added tracks
        checkBox.setChecked(checkedTracks.get(track));
        checkBox.setEnabled(!checkedTracks.get(track));

        // Set checked for tracks candidates to add
        if (!checkedTracks.get(track)){
            checkBox.setChecked(newTracks.contains(track));
        }

        // Set listener
        checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {

            if (checked){

                if (!newTracks.contains(track)) {
                    newTracks.add(track);

                    boolean isArtistNeedToAddInList = true;

                    for (Track trackForCheck : checkedTracks.keySet()){
                        if (trackForCheck.getArtist().equals(artist)){

                            if (!newTracks.contains(trackForCheck) && !checkedTracks.get(trackForCheck)) {

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


    // Add prechecked artists in artist checked list
    private void addPreCheckedArtists(){

        for (String artist : artists) {

            boolean isArtistNeedToAddInList = true;

            for (Track trackForCheck : checkedTracks.keySet()) {
                if (trackForCheck.getArtist().equals(artist)) {
                    if (!checkedTracks.get(trackForCheck)) {
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

    // Getter and setter
    public ArrayList<Track> getNewTracks() {
        return newTracks;
    }

    public void setNewTracks(ArrayList<Track> newTracks) {
        this.newTracks = newTracks;
    }

    public boolean[] getExpandedGroups() {
        return expandedGroups;
    }

    public void setExpandedGroups(boolean[] expandedGroups) {
        this.expandedGroups = expandedGroups;
    }

    // Set listeners
    public void setOnTrackCountChangeListener(OnTrackCountChangeListener trackCountChangeListener) {
        this.onTrackCountChangeListener = trackCountChangeListener;
    }

    public void setOnGroupExpandListener(OnGroupExpandListener onGroupExpandListener) {
        this.onGroupExpandListener = onGroupExpandListener;
    }
}
