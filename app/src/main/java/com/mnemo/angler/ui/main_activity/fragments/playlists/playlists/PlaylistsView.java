package com.mnemo.angler.ui.main_activity.fragments.playlists.playlists;


import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface PlaylistsView extends BaseView {

    void setPlaylists(List<Playlist> playlists);
}
