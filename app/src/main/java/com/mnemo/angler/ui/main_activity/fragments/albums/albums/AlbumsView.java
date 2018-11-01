package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import com.mnemo.angler.ui.base.BaseView;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.List;

public interface AlbumsView extends BaseView {

    void setAlbums(List<Album> albums);
}
