package com.mnemo.angler.ui.main_activity.fragments.folders.folders;


import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.adapters.FoldersAdapter;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.ui.main_activity.fragments.folders.folder_configuration.FolderConfigurationFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class FoldersFragment extends Fragment implements DrawerItem, FoldersView {

    private FoldersPresenter presenter;

    // Bind views with ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.folders_grid)
    RecyclerView recyclerView;

    @BindView(R.id.folders_empty_text)
    TextView emptyTextView;

    private FoldersAdapter adapter;


    public FoldersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fold_fragment_folders, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new FoldersPresenter();
        presenter.attachView(this);

        // Load playlists
        presenter.loadFolders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    // Setup back button
    @OnClick(R.id.folders_drawer_back)
    void back(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
    }



    // MVP View methods
    @Override
    public void setFolders(List<String> folders) {

        // Empty text visibility
        if (folders.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        adapter = new FoldersAdapter(getContext(), folders);
        adapter.setOnFolderClickListener((folder) -> {

            FolderConfigurationFragment folderConfigurationFragment = new FolderConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("folder", folder);
            folderConfigurationFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, folderConfigurationFragment, "folder_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });


        recyclerView.setAdapter(adapter);
    }
}
