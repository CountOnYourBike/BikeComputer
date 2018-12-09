package pl.edu.pg.eti.bikecounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_LOCATION_PERMISSION;

public class MainFragment extends Fragment {

    private TextView speedTextView;
    private TextView averageSpeedTextView;
    private TextView distanceTextView;
    MapView mMapView;
    private GoogleMap googleMap;
    boolean mLocationPermissionGranted;
    Location mLastKnownLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    MainActivity mainActivity;

    public static MainFragment newInstance() {
        return new MainFragment();
    }
    private static String TAG = "MainFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        mainActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        final View rootView = inflater.inflate(R.layout.main_fragment, container, false);


        // checking permission for location services
        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(), ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    mainActivity,
                    new String[]{ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            mLocationPermissionGranted = false;
        }
        else {
            mLocationPermissionGranted = true;
        }

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(mainActivity.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                mLocationPermissionGranted =
                        ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED;
                updateLocationUI();
                getDeviceLocation();
            }
        });


        speedTextView = rootView.findViewById(R.id.speed);
        averageSpeedTextView = rootView.findViewById(R.id.average_speed);
        distanceTextView = rootView.findViewById(R.id.distance);

        final FloatingActionButton fabPlay = rootView.findViewById(R.id.fabPlay);
        final FloatingActionButton fabPause = rootView.findViewById(R.id.fabPause);
        final FloatingActionButton fabStop = rootView.findViewById(R.id.fabStop);

        final Animation fabOpen, fabClose;
        fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);

        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getView().findViewById(R.id.fabPlay).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.fabPause).setVisibility(View.VISIBLE);
                fabPlay.setImageDrawable(
                        getResources().getDrawable(R.drawable.pause_to_play_animation, null));
                Drawable pauseDrawable = fabPause.getDrawable();
                ((Animatable) pauseDrawable).start();
                if(!mainActivity.isPaused()) {
                    fabStop.startAnimation(fabOpen);
                    fabStop.setClickable(true);
                    fillRideParams(null);
                }
                mainActivity.setStarted(true);
                mainActivity.setPaused(false);
            }
        });

        fabPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getView().findViewById(R.id.fabPause).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.fabPlay).setVisibility(View.VISIBLE);
                Drawable playDrawable = fabPlay.getDrawable();
                ((Animatable) playDrawable).start();
                mainActivity.setPaused(true);
            }
        });

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getView().findViewById(R.id.fabPause).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.fabPlay).setVisibility(View.VISIBLE);
                Drawable stopDrawable = fabPlay.getDrawable();
                if(!mainActivity.isPaused())
                    ((Animatable) stopDrawable).start();
                fabStop.startAnimation(fabClose);
                fabStop.setClickable(false);
                mainActivity.setPaused(false);
                mainActivity.setStarted(false);
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    CameraPosition cameraPos = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(18)
                            .bearing(location.getBearing())
                            .tilt(40)
                            .build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos),
                            null);
                }
            }
        };
        fillRideParams(null);
        updateLayout(getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        mainActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        mainActivity.unregisterReceiver(mGattUpdateReceiver);
        stopLocationUpdates();
        mainActivity.removeTimeCallback();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mainActivity.unregisterReceiver(mGattUpdateReceiver);
            stopLocationUpdates();
            mainActivity.removeTimeCallback();
        } catch (IllegalArgumentException e) {
            // if the receiver is not registered
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    private void updateLayout(boolean isLandscape) {
        if (isLandscape) {
            mMapView.setVisibility(View.GONE);
            stopLocationUpdates();
        } else {
            mMapView.setVisibility(View.VISIBLE);
            startLocationUpdates();
        }
    }

    void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(mainActivity, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();

                            CameraPosition cameraPos = new CameraPosition.Builder()
                                    .target(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()))
                                    .zoom(18)
                                    .bearing(mLastKnownLocation.getBearing())
                                    .build();
                            googleMap.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(cameraPos),
                                    null);
                            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            // Building of ETI Faculty on Gdansk University of Technology
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(54.3716335,18.6123179),17));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action) &&
                    mainActivity.isStarted() && !mainActivity.isPaused()) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Measurement measurement = Measurement.fromString(data);
                double wheelCirc = mainActivity.getWheelCirc();
                mainActivity.addToDistance(measurement.getDistance(wheelCirc));
                fillRideParams(measurement);
            }
        }
    };

    private void fillRideParams(Measurement measurement) {
        if (measurement == null) {
            speedTextView.setText("0.00");
        } else {
            speedTextView.setText(String.format(
                    Locale.ENGLISH, "%.2f", measurement.getSpeed(mainActivity.getWheelCirc())));
        }
        double averageSpeed = mainActivity.getDistance() / mainActivity.getTotalTimeInHours();
        if(Double.isNaN(averageSpeed))
            averageSpeedTextView.setText("0.00");
        else
            averageSpeedTextView.setText(String.format(Locale.ENGLISH, "%.2f", averageSpeed));
        distanceTextView.setText(String.format(
                Locale.ENGLISH, "%.2f", mainActivity.getDistance()));
        mainActivity.invalidateOptionsMenu();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                    LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_NO_POWER)
                            .setInterval(5000)
                            .setFastestInterval(1000),
                    mLocationCallback,
                    null);
        } catch (SecurityException se) {
            Log.w(TAG, se.getMessage());
        }
    }

    private void stopLocationUpdates() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage());
        }
    }
}
