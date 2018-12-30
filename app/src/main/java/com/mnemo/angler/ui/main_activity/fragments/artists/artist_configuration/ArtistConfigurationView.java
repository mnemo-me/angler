package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;

import com.mnemo.angler.ui.base.BaseView;


interface ArtistConfigurationView extends BaseView {

    void initializeTabs(int tracksCount, int albumsCount);
    void fillCountViews(int tracksCount, int albumsCount);
}
