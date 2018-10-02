package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;

import com.mnemo.angler.ui.base.BaseView;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.List;


public interface ArtistAlbumsView extends BaseView {

    void setArtistAlbums(List<Album> albums);
}
