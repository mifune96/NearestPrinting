package com.ronal.nearestprinting.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

//class untuk mengambil lokasi dan meminta permisison
public class LocationHelper implements PermissionUtil.PermissionResultCallback{

    private Context context;
    private Activity activity;

    private boolean isPermissionGranted;

    private Location mLastLocation;

    // Google client untuk interaksi dengan google api

    private GoogleApiClient mGoogleApiClient;

    // daftar permissions

    private ArrayList<String> permissions = new ArrayList<>();
    private PermissionUtil permissionUtil;

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;

    public LocationHelper(Context context) {
        this.context = context;
        this.activity = (Activity) context;

        permissionUtil = new PermissionUtil(context, this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * Metode untuk mengecek ketersediaan permission untuk mengambil lokasi
     */

    public void checkPermission(){
        permissionUtil.CheckPermission(permissions, "Need GPS permission for getting" +
                " your location", 1);
    }

    private boolean isPermissionGranted() { return isPermissionGranted; }

    /**
     * Metode untuk mengecek google play service pada device
     */

    public boolean checkPlayService(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS){
            if (googleApiAvailability.isUserResolvableError(resultCode)){
                googleApiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_REQUEST).show();
            } else {
                showToast("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Metode untuk menampilkan lokasi pada UI
     */

    public Location getLocation(){

        if (isPermissionGranted){
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                return mLastLocation;
            } catch (SecurityException e){
                e.printStackTrace();
            }
        }

        return null;

    }

    /**
     * Metode untuk membangun googleapiclinet
     */

    public void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) activity)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) activity)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(locationSettingsResult -> {
            final Status status = locationSettingsResult.getStatus();

            switch (status.getStatusCode()){
                case LocationSettingsStatusCodes.SUCCESS:
                    // Semua permission lokasi terpenuhi, menampilkan lokasi terkini pada device
                    mLastLocation = getLocation();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        // menampilkan dialog dengan memanggil startResolutionForResult(),
                        // dan mengecek result pada onActivityResult().
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e){
                        // Ignore the error
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    /**
     * Metode untuk menghubungkan GoogleApiClient
     */

    public void connectApiClient() { mGoogleApiClient.connect(); }

    /**
     * Metode untuk mendapatkan googleapiclient
     */
    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient; }

    /**
     * Menangani hasil permissions
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        permissionUtil.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // Semua perubahan yang dibutuhkan terpenuhi
                    mLastLocation = getLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    // User sudah diberitahu untuk merubah setting gps, tapi menolak
                    break;
                default:
                    break;
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void PermissionGranted(int REQUEST_CODE) {
        Log.i("PERMISSION","GRANTED");
        isPermissionGranted=true;
    }

    @Override
    public void PartialPermissionGranted(int REQUEST_CODE, ArrayList<String> GRANTED_PERMISSIONS) {
        Log.i("PERMISSION PARTIALLY","GRANTED");
    }

    @Override
    public void PermissionDenied(int REQUEST_CODE) {
        Log.i("PERMISSION","DENIED");
    }

    @Override
    public void NeverAskAgain(int REQUEST_CODE) {
        Log.i("PERMISSION","NEVER ASK AGAIN");
    }
}
