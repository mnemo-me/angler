package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

interface AlbumsView extends BaseView {

    void setAlbums(List<Album> albums);
}
