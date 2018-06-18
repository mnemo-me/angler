package com.mnemo.angler.playlist_manager;


import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Fade;
import android.support.transition.Slide;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistent;
import com.mnemo.angler.data.AnglerContract.*;



public class PlaylistManagerFragment extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {

    GridView playlistGrid;
    SimpleCursorAdapter adapter;

    TextView topTitle;
    View separator;
    ImageView back;

    View addNewPlaylist;

    public static final int LOADER_PLAYLIST_ID = 0;

    public PlaylistManagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_manager, container, false);

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        // Setup playlist gridview with adapter
        playlistGrid = view.findViewById(R.id.playlist_grid);

        adapter = new SimpleCursorAdapter(getContext(),R.layout.pm_playlist_v2, null,
                new String[]{PlaylistEntry.COLUMN_IMAGE_RESOURCE, PlaylistEntry.COLUMN_NAME, PlaylistEntry.COLUMN_TRACKS_TABLE},
                new int[]{R.id.playlist_options_image, R.id.playlist_options_name},0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {

                if (view.getId() == R.id.playlist_options_image){
                    ImageAssistent.loadImage(getContext(),cursor.getString(2), (ImageView) view, 125);
                    view.setTransitionName(cursor.getString(1) + " cover");

                    ((CardView)view.getParent().getParent()).setTransitionName(cursor.getString(1) + " card");
                    return true;
                }
                return false;
            }
        });

        playlistGrid.setAdapter(adapter);

        topTitle = view.findViewById(R.id.playlist_manager_top_title);
        separator = view.findViewById(R.id.playlist_manager_separator);

        back = view.findViewById(R.id.playlist_manager_drawer_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
            }
        });

        // Setup add new playlist button
        addNewPlaylist = view.findViewById(R.id.new_playlist_button);

        addNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlaylistOptionsFragment playlistOptionsFragment = new PlaylistOptionsFragment();

                Bundle args = new Bundle();
                args.putString("image", "R.drawable.back3");
                args.putString("type", "playlist");
                args.putString("playlist_name", getResources().getString(R.string.new_playlist));
                args.putBoolean("playlist_inside", false);
                playlistOptionsFragment.setArguments(args);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, playlistOptionsFragment, "playlist_opt_fragment")
                        .addToBackStack(null)
                        .commit();
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
                        args.putString("type", "playlist");
                        args.putString("image",data.getString(2));
                        args.putString("playlist_name", playlistName);
                        playlistConfigurationFragment.setArguments(args);

                        playlistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));

                        playlistConfigurationFragment.setEnterTransition(new Fade().setStartDelay(300));
                        playlistConfigurationFragment.setReturnTransition(null);

                        setReenterTransition(new Fade().setStartDelay(200));

                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            fragmentTransaction.addSharedElement(separator, "separator");

                        }else{
                            ImageView im = view.findViewById(R.id.playlist_options_image);
                            CardView cardView = view.findViewById(R.id.playlist_options_cardview);

                            fragmentTransaction.addSharedElement(cardView, playlistName + " card")
                                    .addSharedElement(im, playlistName + " cover");
                        }

                        fragmentTransaction.addSharedElement(back, "back")
                                .replace(R.id.frame,playlistConfigurationFragment, "playlist_conf_fragment")
                                .addToBackStack(null)
                                .commit();


                    }
                });

                playlistGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {


                        ImageView im = view.findViewById(R.id.playlist_options_image);
                        CardView cardView = view.findViewById(R.id.playlist_options_cardview);

                        data.moveToPosition(position);

                        String playlistName = data.getString(1);

                        PlaylistOptionsFragment playlistOptionsFragment = new PlaylistOptionsFragment();
                        Bundle args = new Bundle();
                        args.putString("type", "playlist");
                        args.putString("playlist_name", playlistName);
                        args.putString("image",data.getString(2));
                        args.putBoolean("playlist_inside", false);
                        playlistOptionsFragment.setArguments(args);

                        playlistOptionsFragment.setSharedElementEnterTransition(new TransitionSet()
                            .addTransition(new ChangeBounds())
                            .addTransition(new ChangeTransform()));


                        int enterTransitionGravity;

                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            enterTransitionGravity = Gravity.BOTTOM;
                        }else{
                            enterTransitionGravity = Gravity.RIGHT;
                        }

                        playlistOptionsFragment.setEnterTransition(new Slide(enterTransitionGravity));

                        setReenterTransition(null);

                        im.setTransitionName(playlistName + " cover");
                        cardView.setTransitionName(playlistName + " card");
                        topTitle.setTransitionName(playlistName + " title");

                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            fragmentTransaction.addSharedElement(separator, "separator");
                        }
                            fragmentTransaction.addSharedElement(cardView, playlistName + " card")
                                .addSharedElement(im, playlistName + " cover")
                                .addSharedElement(topTitle, playlistName + " title")
                                .addSharedElement(back, "back")
                                .replace(R.id.frame,playlistOptionsFragment, "playlist_opt_fragment")
                                        .addToBackStack(null)
                                        .commit();


                        return false;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (addNewPlaylist != null) {
                addNewPlaylist.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }
}
