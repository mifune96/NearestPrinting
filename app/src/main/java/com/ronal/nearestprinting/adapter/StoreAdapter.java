package com.ronal.nearestprinting.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ronal.nearestprinting.R;
import com.ronal.nearestprinting.activity.StoreDetailActivity;
import com.ronal.nearestprinting.model.Store;
import com.ronal.nearestprinting.util.LocationHelper;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoreAdapter extends FirestoreRecyclerAdapter<Store, StoreAdapter.ViewHolder>{

    private Context context;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
    //private LocationHelper locationHelper;
    private Double userLat, userLng;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public StoreAdapter(@NonNull FirestoreRecyclerOptions<Store> options, Context context
            , Double lat, Double lng) {
        super(options);
        this.context = context;
        this.userLat = lat;
        this.userLng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int i, @NonNull Store store) {
        LocalTime now = LocalTime.now();
        LocalTime openTime = LocalTime.parse(store.getOpenTime(), dateFormat);
        LocalTime closeTime = LocalTime.parse(store.getCloseTime(), dateFormat);

        Double storelat = store.getStoreLatitude();
        Double storeLng = store.getStoreLongitude();

        if (now.isBefore(openTime) || now.isAfter(closeTime)) {
            holder.storeStatus.setText(context.getString(R.string.store_close));
            holder.storeStatus.setBackgroundDrawable(context.getDrawable(R.drawable.closed_store_background));
        } else {
            holder.storeStatus.setText(context.getString(R.string.store_open));
            holder.storeStatus.setBackgroundDrawable(context.getDrawable(R.drawable.open_store_background));
        }
        holder.storeName.setText(store.getStoreName());
        holder.storeAddress.setText(store.getStoreAddress());
        holder.storeTime.setText(context.getString(R.string.store_open_time
                , store.getOpenTime(), store.getCloseTime()));

        Glide.with(context).load(store.getStoreImage()).into(holder.storeImage);
        holder.storeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        holder.wrapper.setOnClickListener(view -> {
            Intent intent = new Intent(context, StoreDetailActivity.class);
            intent.putExtra("storeUid", store.getStoreUid());
            intent.putExtra("storeName", store.getStoreName());
            intent.putExtra("storeAddress", store.getStoreAddress());
            intent.putExtra("storePhone", store.getStorePhoneNumber());
            intent.putExtra("storeOpen", store.getOpenTime());
            intent.putExtra("storeClose", store.getCloseTime());
            intent.putExtra("storeLat", store.getStoreLatitude());
            intent.putExtra("storeLng", store.getStoreLongitude());
            intent.putExtra("storeImage", store.getStoreImage());
            context.startActivity(intent);
        });

        /*RoadManager roadManager = new GraphHopperRoadManager(
                context.getString(R.string.gh_api_key), false);

        GeoPoint startPoint = new GeoPoint(storeLng, storeLng);
        GeoPoint destination = new GeoPoint(store.getStoreLatitude(), store.getStoreLongitude());
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(startPoint);
        waypoints.add(destination);

        // request algoritma astar bi
        roadManager.addRequestOption("algorithm=astarbi");

        // mengambil rute sesuai waypoints
        Road road = roadManager.getRoad(waypoints);

        holder.storeRange.setText(Road.getLengthDurationText(context, road.mLength, road.mDuration));*/

        int rM = 3961; // miles

        double latDis = store.getStoreLatitude() - userLat;
        double lngDis = store.getStoreLongitude() - userLng;
        double a = ((Math.sin(latDis / 2)) * Math.sin(latDis / 2)) + Math.cos(userLat) * Math.cos(store.getStoreLongitude())
                * ((Math.sin(lngDis / 2)) * (Math.sin(lngDis / 2)));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dM = rM * c;

        holder.storeRange.setText(String.format("%.2f", dM / 1000) + " km");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_item, parent, false);
//        // cek permission lokasi
//        locationHelper = new LocationHelper(context);
//        locationHelper.checkPermission();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.storeImage)
        AppCompatImageView storeImage;

        @BindView(R.id.storeName)
        AppCompatTextView storeName;

        @BindView(R.id.storeAddress)
        AppCompatTextView storeAddress;

        @BindView(R.id.storeTime)
        AppCompatTextView storeTime;

        @BindView(R.id.storeStatus)
        AppCompatTextView storeStatus;

        @BindView(R.id.wrapper)
        ViewGroup wrapper;

        @BindView(R.id.storeRange)
        AppCompatTextView storeRange;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
/*
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        location=locationHelper.getLocation();
    }

    // cek koneksi suspen
    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();
    }

    // cek koneksi gagal
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Connection failed:", "ConnectionResult.getErrorCode()"
                + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/
}
