package com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist;

import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface AddTrackToPlaylistView extends BaseView {

    void setPlaylists(List<Playlist> playlists, List<String> playlistsWithTrack);
}
