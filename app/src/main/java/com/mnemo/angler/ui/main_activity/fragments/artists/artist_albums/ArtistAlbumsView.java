package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;

import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;


public interface ArtistAlbumsView extends BaseView {

    void setArtistAlbums(List<Album> albums);
}
