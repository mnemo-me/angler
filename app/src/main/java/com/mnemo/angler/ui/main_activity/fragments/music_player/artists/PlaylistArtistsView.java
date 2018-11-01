package com.mnemo.angler.ui.main_activity.fragments.music_player.artists;


import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

public interface PlaylistArtistsView extends BaseView{

    void setArtists(List<String> artists);

    String getFilter();
}
