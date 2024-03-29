package com.mnemo.angler.ui.local_load_activity.misc;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;


public class ImageDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;

    public ImageDecoration(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int itemPosition = ((RecyclerView.LayoutParams)view.getLayoutParams()).getViewAdapterPosition();

        if (itemPosition >= spanCount) {
            outRect.top = (int)(3 * MainActivity.density);
        }

        outRect.right = (int)(3 * MainActivity.density);
    }
}
