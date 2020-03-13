package com.ronal.nearestprinting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ronal.nearestprinting.activity.MainActivity;
import com.ronal.nearestprinting.util.LocationHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , ActivityCompat.OnRequestPermissionsResultCallback{

    @BindView(R.id.getStarted)
    Button getStarted;

    @BindView(R.id.wrapper)
    View mView;

    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);

        AnimatorSet mAnimator = new AnimatorSet();

        // setting durasi animasi dan jenis animasi
        long ANIMATION_DURATION_MILLISECONDS = 1000;
        mAnimator.setDuration(ANIMATION_DURATION_MILLISECONDS);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // membuat animasi untuk memperbesar ukuran object
        ValueAnimator mScaleUpXAnimator = ObjectAnimator.ofFloat(mView, "scaleX", 0.0f, 1.0f);
        ValueAnimator mScaleUpYAnimator = ObjectAnimator.ofFloat(mView, "scaleY", 0.0f, 1.0f);

        mAnimator.play(mScaleUpXAnimator).with(mScaleUpYAnimator);
        mAnimator.start();

        // cek permission lokasi
        locationHelper = new LocationHelper(this);
        locationHelper.checkPermission();

        if (locationHelper.checkPlayService()){
            locationHelper.buildGoogleApiClient();
        }

        getStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Connection failed:", "ConnectionResult.getErrorCode()"
                + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
