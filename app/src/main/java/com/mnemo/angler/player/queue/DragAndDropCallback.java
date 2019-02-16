package com.mnemo.angler.player.queue;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class DragAndDropCallback extends ItemTouchHelper.Callback {

    private OnDragAndDropListener onDragAndDropListener;

    public interface OnDragAndDropListener{
        void onDragAndDrop(int oldPosition, int newPosition);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

        int oldPosition = viewHolder.getAdapterPosition();
        int newPosition = target.getAdapterPosition();

        onDragAndDropListener.onDragAndDrop(oldPosition, newPosition);

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public void setOnDragAndDropListener(OnDragAndDropListener onDragAndDropListener) {
        this.onDragAndDropListener = onDragAndDropListener;
    }
}
