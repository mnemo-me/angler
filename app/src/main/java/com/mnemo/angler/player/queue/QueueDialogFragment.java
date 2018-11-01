package com.mnemo.angler.player.queue;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class QueueDialogFragment extends BottomSheetDialogFragment {


    Unbinder unbinder;

    @BindView(R.id.qu_count)
    TextView countView;

    @BindView(R.id.qu_recycler_view)
    RecyclerView recyclerView;

    QueueAdapter adapter;

    ArrayList<MediaSessionCompat.QueueItem> queue;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.qu_queue, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
        // Set layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get queue
        if (savedInstanceState != null){
            queue = savedInstanceState.getParcelableArrayList("queue");
        }else {
            queue = ((MainActivity) getActivity()).getAnglerClient().getQueue();
        }

        // Set count view
        countView.setText(String.valueOf(queue.size()));


        // Setup adapter
        adapter = new QueueAdapter(getContext(), queue);
        adapter.setOnQueueRemovedListener(() -> {

            if (Integer.parseInt(countView.getText().toString()) > 1) {
                countView.setText(String.valueOf(Integer.parseInt(countView.getText().toString()) - 1));
            }else{
                dismiss();
                Toast.makeText(getContext(), R.string.empty_queue, Toast.LENGTH_SHORT).show();
            }
        });

        // Set drag'n'drop callback
        DragAndDropCallback dragAndDropCallback = new DragAndDropCallback();
        dragAndDropCallback.setOnDragAndDropListener(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragAndDropCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(adapter);

        try {
            recyclerView.scrollToPosition(((MainActivity) getActivity()).getAnglerClient().getQueuePosition());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set queue position and playback state
        try {
            adapter.setQueuePosition(((MainActivity) getActivity()).getAnglerClient().getQueuePosition());
            adapter.setPlaybackState(((MainActivity) getContext()).getPlaybackState());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case "queue_position_changed":

                        adapter.setQueuePosition(intent.getIntExtra("queue_position", 0));

                        break;

                    case "playback_state_changed":

                        adapter.setPlaybackState(intent.getExtras().getInt("playback_state"));

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("queue_position_changed");
        intentFilter.addAction("playback_state_changed");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("queue", queue);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }
}
