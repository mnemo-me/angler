package com.mnemo.angler.ui.main_activity.fragments.music_player.music_player;


import com.mnemo.angler.ui.base.BaseView;

import java.util.List;

interface MusicPlayerView extends BaseView {

    void updateSpinner(List<String> playlists);
}
