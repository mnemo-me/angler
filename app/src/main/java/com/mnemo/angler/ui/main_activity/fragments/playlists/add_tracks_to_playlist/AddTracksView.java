package com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.HashMap;

interface AddTracksView extends BaseView{

    void setTracks(HashMap<Track, Boolean> checkedTracks);
}
