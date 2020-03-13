package com.ronal.nearestprinting.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ronal.nearestprinting.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutHelpActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.helpLayout)
    View help;

    @BindView(R.id.aboutLayout)
    View about;

    @BindView(R.id.activityTitle)
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_help);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(view -> finish());
        if (getIntent().getStringExtra("TAG").equals("help")) {
            title.setText("Help");
            about.setVisibility(View.GONE);
        } else {
            title.setText("About");
            help.setVisibility(View.GONE);
        }
    }
}
