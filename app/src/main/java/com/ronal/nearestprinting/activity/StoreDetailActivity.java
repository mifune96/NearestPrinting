package com.ronal.nearestprinting.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ronal.nearestprinting.R;
import com.ronal.nearestprinting.adapter.ServicesAdapter;
import com.ronal.nearestprinting.model.Service;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class StoreDetailActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.storeImage)
    ImageView storeImage;

    @BindView(R.id.storeName)
    TextView storeName;

    @BindView(R.id.storeTime)
    TextView storeTime;

    @BindView(R.id.storeAddress)
    TextView storeAddress;

    @BindView(R.id.btCall)
    Button call;

    @BindView(R.id.btChat)
    Button chat;

    @BindView(R.id.btNavigate)
    Button openMap;

    @BindView(R.id.serviceList)
    RecyclerView serviceList;

    private FirebaseFirestore mFirestore;
    private ServicesAdapter servicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);
        ButterKnife.bind(this);

        // setting appbarlayout

        // inisiasi firestore
        mFirestore = FirebaseFirestore.getInstance();
        
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        // setup layoutmanager recyclerview
        serviceList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        getStoreDetail();

        openMap.setOnClickListener(this);
        chat.setOnClickListener(this);
        call.setOnClickListener(this);
        storeImage.setOnClickListener(this);
    }

    //mengambil detail toko
    private void getStoreDetail() {
        storeName.setText(getIntent().getStringExtra("storeName"));
        storeAddress.setText(getIntent().getStringExtra("storeAddress"));
        storeTime.setText(
                getResources().getString(R.string.store_open_time
                        , getIntent().getStringExtra("storeOpen")
                        , getIntent().getStringExtra("storeClose"))
        );

        Glide.with(this).load(getIntent().getStringExtra("storeImage"))
                .into(storeImage);

        getServices(getIntent().getStringExtra("storeUid"));

    }

    // mengambil data service pada toko
    private void getServices(String storeUid) {
        Query query = mFirestore.collection("stores").document(storeUid)
                .collection("services").orderBy("serviceName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Service> options = new FirestoreRecyclerOptions.Builder<Service>()
                .setQuery(query, Service.class).build();

        servicesAdapter = new ServicesAdapter(options, this);
        serviceList.setAdapter(servicesAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        servicesAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (servicesAdapter != null) {
            servicesAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        servicesAdapter.startListening();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btNavigate:
                Intent intent = new Intent(this, DirectionActivity.class);
                intent.putExtra("lat", getIntent().getDoubleExtra("storeLat", 0));
                intent.putExtra("lng", getIntent().getDoubleExtra("storeLng", 0));
                intent.putExtra("title", getIntent().getStringExtra("storeName"));
                startActivity(intent);
                break;
            case R.id.btChat:
                Intent chatIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://wa.me/62" + getIntent().getStringExtra("storePhone")
                ));
                startActivity(chatIntent);
                break;
            case R.id.btCall:
                Intent callIntent = new Intent(Intent.ACTION_DIAL
                        , Uri.parse("tel:" + getIntent().getStringExtra("storePhone")));
                startActivity(callIntent);
                break;
            case R.id.storeImage:
                Intent imageIntent = new Intent(this, StoreImageDetailActivity.class);
                imageIntent.putExtra("image", getIntent().getStringExtra("storeImage"));
                startActivity(imageIntent);
        }
    }
}
