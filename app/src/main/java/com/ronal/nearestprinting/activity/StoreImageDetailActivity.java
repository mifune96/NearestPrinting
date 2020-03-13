package com.ronal.nearestprinting.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ronal.nearestprinting.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoreImageDetailActivity extends AppCompatActivity {

    @BindView(R.id.storeImage)
    ImageView storeImage;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_image_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        Glide.with(this).load(getIntent().getStringExtra("image")).into(storeImage);
    }
}
