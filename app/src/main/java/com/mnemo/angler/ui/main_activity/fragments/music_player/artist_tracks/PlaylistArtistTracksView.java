package com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

interface PlaylistArtistTracksView extends BaseView{

    void setTracks(List<Track> artistTracks);

    String getFilter();
}
