package com.mnemo.angler.background_changer;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class LocalLoadFragment extends Fragment {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    Unbinder unbinder;

    private ArrayList<String> imageFolders = new ArrayList<>();

    public LocalLoadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bg_fragment_local_load, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Hide background, media panel and frame
        ((MainActivity)getActivity()).hideBackground();
        ((MainActivity)getActivity()).hideMediaPanel();
        ((MainActivity)getActivity()).hideFrame();

        // Collect all image folders from device
        imageFolders.addAll(getImageFolders(Environment.getExternalStorageDirectory().getPath()));

        // Setup TabLayout and bind ViewPager with it
        viewPager.setAdapter(new LocalLoadAdapter(getActivity().getSupportFragmentManager(), imageFolders, getArguments().getString("image_type")));
        tabLayout.setupWithViewPager(viewPager);


        return view;
    }

    /*
     Recursively collect all folders with images on device
     ignoring folders, contains .nomedia file and hidden folders
      */
    private TreeSet<String> getImageFolders(String path) {

        TreeSet<String> imageFolders = new TreeSet<>(new FolderComparator());

        File directory = new File(path);
        ArrayList<String> files = new ArrayList<>(Arrays.asList(directory.list()));

        if (directory.getName().startsWith(".") || files.contains(".nomedia")){
            return imageFolders;
        }

        for (String file : files) {

            File temp = new File(path + File.separator + file);

            if (temp.isDirectory()) {
                imageFolders.addAll(getImageFolders(path + File.separator + file));
            }else{

                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                if (mimeType != null) {
                    if (mimeType.contains("image/")) {

                            imageFolders.add(path);

                    }
                }
            }
        }

        return imageFolders;
    }

    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show background, media panel and frame
        ((MainActivity)getActivity()).showBackground();
        ((MainActivity)getActivity()).showMediaPanel();
        ((MainActivity)getActivity()).showFrame();

        unbinder.unbind();
    }

    // Comparator for TreeSet to sort folders alphabetically based on relative path
    public class FolderComparator implements Comparator{
        @Override
        public int compare(Object o, Object t1) {
            String folderName1 = (String) o;
            String folderName2 = (String) t1;

            File folder1 = new File(folderName1);
            File folder2 = new File(folderName2);

            return folder1.getName().compareTo(folder2.getName());
        }
    }


    @OnClick(R.id.local_load_back_button)
    void back(){
        getActivity().onBackPressed();
    }
}
