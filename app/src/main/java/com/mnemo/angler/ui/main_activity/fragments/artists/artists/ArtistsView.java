package com.mnemo.angler.ui.main_activity.fragments.artists.artists;

import com.mnemo.angler.ui.base.BaseView;

import java.util.List;


interface ArtistsView extends BaseView {

    void setArtists(List<String> artists);
}
