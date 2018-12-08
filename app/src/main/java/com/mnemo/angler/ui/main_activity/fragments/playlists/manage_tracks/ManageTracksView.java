package com.mnemo.angler.ui.main_activity.fragments.playlists.manage_tracks;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface ManageTracksView extends BaseView {

    void setTracks(List<Track> tracks);
}
