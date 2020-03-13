package com.ronal.nearestprinting.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.ronal.nearestprinting.R;
import com.ronal.nearestprinting.adapter.StoreAdapter;
import com.ronal.nearestprinting.model.Store;
import com.ronal.nearestprinting.util.LocationHelper;

import org.osmdroid.config.Configuration;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.storeList)
    RecyclerView storeList;

    private FirebaseFirestore mFirestore;
    private StoreAdapter storeAdapter;

    private GoogleApiClient googleApiClient;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        // inisiasi osm
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // inisiasi firebase firestore
        mFirestore = FirebaseFirestore.getInstance();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // setup layout manager pada recylcer view
        storeList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        getStoreList();
    }

    // mengambil data toko
    private void getStoreList() {
        Query query = mFirestore.collection("stores")
                .orderBy("storeName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Store> options = new FirestoreRecyclerOptions.Builder<Store>()
                .setQuery(query, Store.class).build();

        //location = locationHelper.getLocation();
        storeAdapter = new StoreAdapter(options, this, lat, lng);
        storeList.setAdapter(storeAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        storeAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (storeAdapter != null) {
            storeAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        storeAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, AboutHelpActivity.class);

        switch (item.getItemId()) {
            case R.id.menu_help:
                intent.putExtra("TAG", "help");
                startActivity(intent);
                break;
            case R.id.menu_about:
                intent.putExtra("TAG", "about");
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }

        startLocationUpdate();
    }

    private void startLocationUpdate() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !"
                    , Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }
    }
}
