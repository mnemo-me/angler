package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;


public interface MainPlaylistView extends BaseView {

    void setTracks(List<Track> playlistTracks);

    String getFilter();
}
