package com.mnemo.angler.drawer_items_fragments.local_load;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.MimeTypeMap;

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

public class LocalLoadActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    String imageType;

    Unbinder unbinder;

    private ArrayList<String> imageFolders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_load);

        unbinder = ButterKnife.bind(this);

        // Gather info from intent
        Intent intent = getIntent();
        imageType = intent.getStringExtra("image_type");

        // Collect all image folders from device
        imageFolders.addAll(getImageFolders(Environment.getExternalStorageDirectory().getPath()));

        // Setup TabLayout and bind ViewPager with it
        viewPager.setAdapter(new LocalLoadAdapter(getSupportFragmentManager(), imageFolders));
        tabLayout.setupWithViewPager(viewPager);

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

    // Comparator for TreeSet to sort folders alphabetically based on relative path
    public class FolderComparator implements Comparator {
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
        onBackPressed();
    }


    public String getImageType() {
        return imageType;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
