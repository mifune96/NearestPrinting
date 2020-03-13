package com.ronal.nearestprinting.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ronal.nearestprinting.R;
import com.ronal.nearestprinting.util.LocationHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DirectionActivity extends AppCompatActivity implements View.OnClickListener
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , ActivityCompat.OnRequestPermissionsResultCallback{

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.mapView)
    MapView mapView;

    @BindView(R.id.draw_route)
    FloatingActionButton navigate;

    private MyLocationNewOverlay myLocationNewOverlay;
    private RoadManager roadManager;
    private RotationGestureOverlay rotationGestureOverlay;
    private GpsMyLocationProvider myLocationProvider;
    private CompassOverlay compassOverlay;
    private IMapController mapController;
    protected FolderOverlay roadNodeMarkers;

    private GeoPoint startPoint, destPoint;
    private Bitmap startIcon;

    private LocationHelper locationHelper;
    private Location location;

    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        // inisiasi osm
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        setContentView(R.layout.activity_direction);
        ButterKnife.bind(this);

        // cek permission lokasi
        locationHelper = new LocationHelper(this);
        locationHelper.checkPermission();

        // inisiasi road manager dengan graphhoper key
        roadManager = new GraphHopperRoadManager(getString(R.string.gh_api_key), true);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Navigasi");
        toolbar.setNavigationOnClickListener(view -> finish());

        navigate.setOnClickListener(this);

        startIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_start);

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        setupOverlay();
        setupMap();
        addMarker();

        if (locationHelper.checkPlayService()){
            locationHelper.buildGoogleApiClient();
        }
    }

    // menambah marker toko
    private void addMarker() {
        Marker destMarker = new Marker(mapView);

        destPoint = new GeoPoint(lat, lng);
        destMarker.setPosition(destPoint);
        destMarker.setTitle(getIntent().getStringExtra("title"));
        destMarker.setIcon(getDrawable(R.drawable.marker_dest));

        mapView.getOverlays().add(destMarker);
        mapView.invalidate();
    }

    private void setupMap() {
        BingMapTileSource.setBingKey(getString(R.string.bing_key));
        BingMapTileSource bingMap = new BingMapTileSource(null);
        bingMap.setStyle(BingMapTileSource.IMAGERYSET_ROAD);

        mapView.getOverlays().add(rotationGestureOverlay);
        mapView.getOverlays().add(compassOverlay);
        mapView.getOverlays().add(myLocationNewOverlay);
        mapView.getOverlays().add(roadNodeMarkers);

        mapView.setTileSource(bingMap);
        mapView.setTilesScaledToDpi(true);
        mapView.setMinZoomLevel(1.0);
        mapView.setMaxZoomLevel(21.0);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapController.animateTo(new GeoPoint(-5.402213, 105.264093));
    }

    // setup gesture dan compass
    private void setupOverlay() {
        mapController = mapView.getController();
        mapController.setZoom(13.0);

        roadNodeMarkers = new FolderOverlay();
        roadNodeMarkers.setName("Route Steps");

        compassOverlay = new CompassOverlay(this
                , new InternalCompassOrientationProvider(this), mapView);
        compassOverlay.enableCompass();
        compassOverlay.setPointerMode(true);

        myLocationProvider = new GpsMyLocationProvider(this.getBaseContext());
        myLocationProvider.setLocationUpdateMinTime(1000);
        myLocationProvider.setLocationUpdateMinDistance(50);

        rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);

        myLocationNewOverlay = new MyLocationNewOverlay(myLocationProvider, mapView);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        myLocationNewOverlay.setPersonIcon(startIcon);
    }

    // mengambil lokasi
    @SuppressLint("MissingPermission")
    private void getRoute() {

        location = locationHelper.getLocation();

        startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        GeoPoint destination = destPoint;
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(startPoint);
        waypoints.add(destination);

        // request algoritma astar bi
        roadManager.addRequestOption("algorithm=astarbi");

        // mengambil rute sesuai waypoints
        Road road = roadManager.getRoad(waypoints);

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

        roadOverlay.setColor(R.color.colorPrimary);
        roadOverlay.setWidth(8);

        // direction node
        putRoadNodes(road);

        // menampilkan rute pada map
        mapView.getOverlays().add(roadOverlay);
        mapView.invalidate();
    }

    private void putRoadNodes(Road road) {
        roadNodeMarkers.getItems().clear();
        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);

        int n = road.mNodes.size();

        MarkerInfoWindow infoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, mapView);
        TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);

        for (int i = 0; i < n; i++) {
            RoadNode node = road.mNodes.get(i);
            String instructions = (node.mInstructions == null ? "" : node.mInstructions);
            Marker nodeMarker = new Marker(mapView);
            nodeMarker.setTitle("Step " + (i + 1));
            nodeMarker.setSnippet(instructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setInfoWindow(infoWindow);
            int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
            if (iconId != R.drawable.ic_empty) {
                Drawable image = ResourcesCompat.getDrawable(getResources(), iconId, null);
                nodeMarker.setImage(image);
            }
            nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            roadNodeMarkers.add(nodeMarker);
        }

        iconIds.recycle();
    }

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
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.draw_route) {
            getRoute();
        }
    }
}
