package com.mnemo.angler.ui.local_load_activity.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.local_load_activity.adapters.LocalLoadAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalLoadActivity extends AppCompatActivity implements LocalLoadView{

    LocalLoadPresenter presenter;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    String imageType;


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

        // Setup TabLayout and bind ViewPager with it
        viewPager.setAdapter(new LocalLoadAdapter(getSupportFragmentManager(), imageFolders));
        tabLayout.setupWithViewPager(viewPager);
    }


    // Getter
    public String getImageType() {
        return imageType;
    }

}
