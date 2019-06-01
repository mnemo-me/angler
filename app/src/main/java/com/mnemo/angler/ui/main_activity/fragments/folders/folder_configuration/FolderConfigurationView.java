package com.mnemo.angler.ui.main_activity.fragments.folders.folder_configuration;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface FolderConfigurationView extends BaseView {

    void setFolderTracks(List<Track> tracks);
    void linkAdded();
    void linkRemoved();
    void linkActive(boolean linkStatus);
}
