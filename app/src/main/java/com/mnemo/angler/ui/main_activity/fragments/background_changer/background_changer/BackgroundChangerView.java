package com.mnemo.angler.ui.main_activity.fragments.background_changer.background_changer;

import com.mnemo.angler.ui.base.BaseView;

import java.util.List;


public interface BackgroundChangerView extends BaseView {

    void setBackgroundImages(List<String> images);
}
