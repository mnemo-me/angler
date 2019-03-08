package com.mnemo.angler.ui.local_load_activity.activity;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.adapters.LocalLoadAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalLoadActivity extends AppCompatActivity implements LocalLoadView{

    private LocalLoadPresenter presenter;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.empty_image_text)
    TextView emptyText;

    private String imageType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_load);

        // Inject views
        ButterKnife.bind(this);

        // Get image type
        imageType = getIntent().getStringExtra("image_type");

        // Bind Presenter to View
        presenter = new LocalLoadPresenter();
        presenter.attachView(this);

        // Collect all image folders from device
        presenter.gatherImageFolders();
    }

    @Override
    protected void onStart() {
        super.onStart();

        presenter.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.deattachView();
    }

    @OnClick(R.id.local_load_back_button)
    void back(){
        onBackPressed();
    }

    // MVP View methods
    @Override
    public void setImageFolders(List<String> imageFolders) {

        // Show empty text if no image folders
        if (imageFolders.size() == 0){
            emptyText.setVisibility(View.VISIBLE);
        }

        // Setup TabLayout and bind ViewPager with it
        viewPager.setAdapter(new LocalLoadAdapter(getSupportFragmentManager(), imageFolders));
        tabLayout.setupWithViewPager(viewPager);
    }


    // Getter
    public String getImageType() {
        return imageType;
    }

}
