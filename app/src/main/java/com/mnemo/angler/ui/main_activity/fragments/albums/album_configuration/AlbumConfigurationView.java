package com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface AlbumConfigurationView extends BaseView {

    void setAlbumTracks(List<Track> tracks);
}
