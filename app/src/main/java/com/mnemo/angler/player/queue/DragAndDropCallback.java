package com.mnemo.angler.player.queue;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class DragAndDropCallback extends ItemTouchHelper.Callback {

    private OnDragAndDropListener onDragAndDropListener;

    public interface OnDragAndDropListener{
        void onDragAndDrop(int oldPosition, int newPosition);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        int oldPosition = viewHolder.getAdapterPosition();
        int newPosition = target.getAdapterPosition();

        onDragAndDropListener.onDragAndDrop(oldPosition, newPosition);

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    void setOnDragAndDropListener(OnDragAndDropListener onDragAndDropListener) {
        this.onDragAndDropListener = onDragAndDropListener;
    }
}
