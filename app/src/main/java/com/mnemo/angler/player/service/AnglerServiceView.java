package com.mnemo.angler.player.service;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface AnglerServiceView extends BaseView {

    void setQueue(List<Track> tracks);

    void updateQueue(List<Track> tracks);

    void initializeFirstTrack();
}
