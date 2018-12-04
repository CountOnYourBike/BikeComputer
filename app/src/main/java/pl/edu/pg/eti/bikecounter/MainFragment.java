package pl.edu.pg.eti.bikecounter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationServices;

import static pl.edu.pg.eti.bikecounter.DeviceScanFragment.REQUEST_LOCATION_PERMISSION;

public class MainFragment extends Fragment {

    private TextView speedTextView;
    private TextView averageSpeedTextView;
    private TextView distanceTextView;
    private TextView timeTextView;
    MapView mMapView;
    private GoogleMap googleMap;
    boolean mLocationPermissionGranted;
    Location mLastKnownLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        final View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        // checking permission for location services
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            mLocationPermissionGranted = false;
        }
        else {
            mLocationPermissionGranted = true;
        }

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize((getActivity().getApplicationContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = false;
                }
                else {
                    mLocationPermissionGranted = true;
                }
                updateLocationUI();
                getDeviceLocation();
            }
        });


        speedTextView = rootView.findViewById(R.id.speed);
        averageSpeedTextView = rootView.findViewById(R.id.average_speed);
        distanceTextView = rootView.findViewById(R.id.distance);
        timeTextView = rootView.findViewById(R.id.total_time);

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
                fabPlay.setImageDrawable(getResources().getDrawable(R.drawable.pause_to_play_animation, null));
                ((Animatable)fabPause.getDrawable()).start();
                fabStop.startAnimation(fabOpen);
                //TODO: Manage play
                startLocationUpdates();
                ((MainActivity)getActivity()).setStarted(true);
                ((MainActivity)getActivity()).setPaused(false);
            }
        });

        fabPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getView().findViewById(R.id.fabPause).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.fabPlay).setVisibility(View.VISIBLE);
                Drawable d = fabPlay.getDrawable();
                ((Animatable)d).start();
                fabStop.startAnimation(fabClose);
                //TODO: Manage pause
                stopLocationUpdates();
                ((MainActivity)getActivity()).setPaused(true);
            }
        });

        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getView().findViewById(R.id.fabPause).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.fabPlay).setVisibility(View.VISIBLE);
                Drawable d = fabPlay.getDrawable();
                ((Animatable)d).start();
                fabStop.startAnimation(fabClose);
                //TODO: Manage stop
                stopLocationUpdates();
                ((MainActivity)getActivity()).setPaused(false);
                ((MainActivity)getActivity()).setStarted(false);
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), 18));
                }
            }
        };
        fillRideParams(null);

        //((MainActivity)getActivity()).getWheelCirc().toString();
        Toast toast = Toast.makeText(getContext(), ((MainActivity)getActivity()).getWheelCirc().toString()+"mm", Toast.LENGTH_SHORT);
        toast.show();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mGattUpdateReceiver);
            stopLocationUpdates();
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
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult()!=null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                        } else {
                            Log.d("MainFragment", "Current location is null. Using defaults.");
                            Log.e("MainFragment", "Exception: %s", task.getException());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.3716335,18.6123179),17));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            MainActivity mainActivity = (MainActivity)getActivity();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action) && mainActivity.isStarted() && !mainActivity.isPaused()) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Measurement measurement = Measurement.fromString(data);
                //TODO: Podmienić z bazy danych
                double wheelCirc = mainActivity.getWheelCirc();
                mainActivity.addToDistance(measurement.getNumberOfRevolutions()*wheelCirc/100000);
                fillRideParams(measurement);
            }
        }
    };

    private void fillRideParams(Measurement measurement) {
        MainActivity mainActivity = (MainActivity)getActivity();
        if (measurement == null) {
            speedTextView.setText("0.0");
        } else {
            speedTextView.setText(String.valueOf(measurement.getSpeed(mainActivity.getWheelCirc())));
        }
        averageSpeedTextView.setText(String.valueOf(mainActivity.getDistance() / mainActivity.getTotalTime()));
        distanceTextView.setText(String.valueOf(mainActivity.getDistance()));
        timeTextView.setText(String.valueOf(mainActivity.getTotalTime()));
        mainActivity.invalidateOptionsMenu();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(LocationRequest
                                                                .create()
                                                                .setPriority(LocationRequest.PRIORITY_NO_POWER)
                                                                .setInterval(5000)
                                                                .setFastestInterval(500),
                    mLocationCallback,
                    null /* Looper */);
        } catch (SecurityException se) {
            Log.w("MainFragment", se.getMessage());
        }
    }

    private void stopLocationUpdates() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception ex) {
            Log.w("MainFragment", ex.getMessage());
        }
    }

}
