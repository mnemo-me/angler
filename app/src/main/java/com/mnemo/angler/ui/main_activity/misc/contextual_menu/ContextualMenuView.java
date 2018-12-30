package com.mnemo.angler.ui.main_activity.misc.contextual_menu;

import com.mnemo.angler.ui.base.BaseView;


interface ContextualMenuView extends BaseView {

    void showDeleteTrackSnackbar(String playlist, String trackId, int position);
}
