package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

interface PlaylistConfigurationView extends BaseView{

    void setPlaylistTracks(List<Track> tracks);
}
