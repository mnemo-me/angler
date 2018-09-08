package com.mnemo.angler.queue_manager;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;


public class QueueDialogFragment extends DialogFragment {

    TextView countView;

    RecyclerView recyclerView;
    QueueAdapter adapter;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Fill queue
        final ArrayList<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
        queue.addAll(MediaControllerCompat.getMediaController(getActivity()).getQueue());


        // Set title
        ConstraintLayout titleLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.qu_dialog_title,null, false);
        countView = titleLayout.findViewById(R.id.qu_dialog_count);
        countView.setText(String.valueOf(queue.size()));
        builder.setCustomTitle(titleLayout);

        // Set body
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.qu_queue, null, false);
        recyclerView = linearLayout.findViewById(R.id.queue_recycler_view);
        adapter = new QueueAdapter(getContext(), queue);
        adapter.setOnQueueRemovedListener(new QueueAdapter.OnQueueRemovedListener() {
            @Override
            public void onQueueRemove() {

                if (Integer.parseInt(countView.getText().toString()) > 1) {
                    countView.setText(String.valueOf(Integer.parseInt(countView.getText().toString()) - 1));
                }else{
                    dismiss();
                    Toast.makeText(getContext(), R.string.empty_queue, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set drag'n'drop callback
        DragAndDropCallback dragAndDropCallback = new DragAndDropCallback();
        dragAndDropCallback.setOnDragAndDropListener(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragAndDropCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        builder.setView(linearLayout);


        // Set Negative button
        builder.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        recyclerView.scrollToPosition(((MainActivity)getActivity()).getQueuePosition());

        return builder.create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "queue_position_changed":

                        adapter.setQueuePosition(intent.getIntExtra("queue_position", 0));
                        adapter.notifyDataSetChanged();

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("queue_position_changed");

        getContext().registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(receiver);
    }
}
