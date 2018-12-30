package com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

interface ArtistTracksView extends BaseView {

    void setArtistTracks(List<Track> tracks);
}
