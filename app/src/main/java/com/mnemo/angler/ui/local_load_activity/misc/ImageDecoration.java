package com.mnemo.angler.ui.local_load_activity.misc;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;


public class ImageDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;

    public ImageDecoration(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int itemPosition = ((RecyclerView.LayoutParams)view.getLayoutParams()).getViewAdapterPosition();

        if (itemPosition >= spanCount) {
            outRect.top = (int)(3 * MainActivity.density);
        }

        if ((itemPosition + 1) % spanCount != 0){
            outRect.right = (int)(3 * MainActivity.density);
        }
    }
}
