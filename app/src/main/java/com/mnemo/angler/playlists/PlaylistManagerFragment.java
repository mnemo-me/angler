package com.mnemo.angler.playlists;


import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerContract.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlaylistManagerFragment extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_PLAYLIST_ID = 0;

    Unbinder unbinder;

    @BindView(R.id.playlist_manager_top_title)
    TextView topTitle;

    @BindView(R.id.playlist_manager_drawer_back)
    ImageView back;

    @BindView(R.id.playlist_grid)
    GridView playlistGrid;

    SimpleCursorAdapter adapter;


    public PlaylistManagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlists, container, false);

        unbinder = ButterKnife.bind(this, view);

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        // Setup playlist GridView with adapter
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            playlistGrid.setNumColumns(3);
        }else{
            playlistGrid.setNumColumns(5);
        }

        adapter = new SimpleCursorAdapter(getContext(),R.layout.pm_playlist_v2, null,
                new String[]{PlaylistEntry.COLUMN_IMAGE_RESOURCE, PlaylistEntry.COLUMN_NAME, PlaylistEntry.COLUMN_TRACKS_TABLE},
                new int[]{R.id.playlist_options_image, R.id.playlist_options_name},0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {

                if (view.getId() == R.id.playlist_options_image){
                    ImageAssistant.loadImage(getContext(),cursor.getString(2), (ImageView) view, 125);
                    view.setTransitionName(cursor.getString(1) + " cover");

                    ((CardView)view.getParent().getParent()).setTransitionName(cursor.getString(1) + " card");
                    return true;
                }
                return false;
            }
        });

        playlistGrid.setAdapter(adapter);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
            }
        });

        return view;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_PLAYLIST_ID:
                return new CursorLoader(getContext(),
                        PlaylistEntry.CONTENT_URI, null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " = 0", null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(data);

                final int orientation = getResources().getConfiguration().orientation;

                playlistGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        data.moveToPosition(position);

                        String playlistName = data.getString(1);

                        PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();

                        Bundle args = new Bundle();
                        args.putString("image",data.getString(2));
                        args.putString("playlist_name", playlistName);

                        playlistConfigurationFragment.setArguments(args);

                        playlistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));

                        playlistConfigurationFragment.setEnterTransition(new Fade().setStartDelay(100));
                        playlistConfigurationFragment.setReturnTransition(null);

                        setReenterTransition(new Fade().setStartDelay(100));


                        getActivity().getSupportFragmentManager().beginTransaction()
                                .addSharedElement(back, "back")
                                .replace(R.id.frame,playlistConfigurationFragment, "playlist_conf_fragment")
                                .addToBackStack(null)
                                .commit();


                    }
                });

                playlistGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                        data.moveToPosition(position);

                        String title = data.getString(1);
                        String image = data.getString(2);

                        PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

                        Bundle args = new Bundle();
                        args.putString("action", "change");
                        args.putString("title", title);
                        args.putString("image", image);
                        playlistCreationDialogFragment.setArguments(args);

                        playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");

                        return true;
                    }
                });

                break;

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(null);
                break;
        }
    }


    // Setup add new playlist button
    @OnClick(R.id.new_playlist_button)
    void createNewPlayist(){

        PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

        Bundle args = new Bundle();
        args.putString("action", "create");
        playlistCreationDialogFragment.setArguments(args);

        playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
    }

    public void updateGrid(){
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }



}
