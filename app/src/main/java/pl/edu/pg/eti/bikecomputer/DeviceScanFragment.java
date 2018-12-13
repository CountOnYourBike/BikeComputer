/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.edu.pg.eti.bikecomputer;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;
import static pl.edu.pg.eti.bikecomputer.CyclingGattAttributes.*;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanFragment extends Fragment {
    private static final String TAG = "DeviceScanFragment";
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBTAdapter;
    private LeDeviceListAdapter mBTArrayAdapter;
    private String mDeviceAddress;
    private boolean mScanning;
    private Handler mHandler;
    ListView mDevicesListView;
    Button button_scan;
    Button button_stop;
    private View mView;
    private MainActivity mainActivity;

    public static final int REQUEST_LOCATION_PERMISSION = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public static DeviceScanFragment newInstance() {
        return new DeviceScanFragment();
    }

    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity)getActivity();

        mView = inflater.inflate(R.layout.configuration_fragment, container, false);

        mBTArrayAdapter = new LeDeviceListAdapter();
        mDevicesListView=mView.findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mHandler = new Handler();

        button_scan = mView.findViewById(R.id.button_scan);
        button_stop = mView.findViewById(R.id.button_stop);

        TextView connectedTextView = mView.findViewById(R.id.counter_connection_state);
        if(mainActivity.isConnected())
            connectedTextView.setText(getText(R.string.connected));
        else
            connectedTextView.setText(getText(R.string.disconnected));


        // Use this check to determine whether BLE is supported on the device.
        if (!mainActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mainActivity.getApplicationContext(),
                    R.string.ble_not_supported,
                    Toast.LENGTH_SHORT)
                    .show();
            mainActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance(), "home")
                    .addToBackStack("home")
                    .commit();
        }

        // checking permission for location services
        if(ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION))
                showExplanation(
                        getString(R.string.permission_needed),
                        getString(R.string.location_permission_message),
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_LOCATION_PERMISSION);
            else
                ActivityCompat.requestPermissions(
                        mainActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
        }

        // Initializes a Bluetooth adapter.
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();


        // Checks if Bluetooth is supported on the device.
        if (mBTAdapter == null) {
            Toast.makeText(mainActivity.getApplicationContext(),
                    R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT)
                    .show();
            mainActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance(), "home")
                    .addToBackStack("home")
                    .commit();
            return mView;
        }

        button_scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanLeDevice(false);
            }
        });
        return mView;
    }

    private void checkIfLocationIsEnabled() {
        LocationManager lm =
                (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(getString(R.string.location_needed_for_searching_devices));
            dialog.setPositiveButton(getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mainActivity.startActivityForResult(myIntent, REQUEST_LOCATION_PERMISSION);
                }
            });
            dialog.setNegativeButton(getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // check if user didn't turned it
                    LocationManager lm =
                            (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                            !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        Toast.makeText(getContext(),
                                getString(R.string.location_disabled_nothing_will_be_found),
                                Toast.LENGTH_SHORT)
                                .show();
                }
            });
            dialog.show();
        }
    }

    // function to build an alert explaining needed permission(s)
    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(mainActivity,
                new String[]{permissionName}, permissionRequestCode);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance(), "home")
                        .addToBackStack("home")
                        .commit();
                return;
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
        mBTArrayAdapter.clear();
        mainActivity.unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
        try {
            mainActivity.unregisterReceiver(mGattUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // if the receiver is not registered
            e.printStackTrace();
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            TextView counterConnectionState = mView.findViewById(R.id.counter_connection_state);
            ProgressBar progressBar = mView.findViewById(R.id.progress_bar);
            if(action == null)
                return;
            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTING:
                    Log.d(TAG, "Connecting");
                    progressBar.setVisibility(View.VISIBLE);
                    mainActivity.setConnected(false);
                    counterConnectionState.setText(getText(R.string.disconnected));
                    mainActivity.invalidateOptionsMenu();
                    break;

                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    Log.d(TAG, "Connected");
                    progressBar.setVisibility(View.INVISIBLE);
                    mainActivity.setConnected(true);
                    counterConnectionState.setText(getText(R.string.connected));
                    mainActivity.invalidateOptionsMenu();
                    break;

                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    Log.d(TAG, "Disconnected");
                    progressBar.setVisibility(View.INVISIBLE);
                    mainActivity.setConnected(false);
                    counterConnectionState.setText(getText(R.string.disconnected));
                    mainActivity.invalidateOptionsMenu();
                    break;

                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    // Find the CSC Service from all services
                    BluetoothGattService gattService =
                            getBluetoothGattService(CYCLING_SPEED_AND_CADENCE_SERVICE);
                    if (gattService != null) {
                        BluetoothGattCharacteristic characteristic =
                                gattService.getCharacteristic(
                                        UUID.fromString(CSC_MEASUREMENT_CHARACTERISTIC));
                        final int characteristicProperties = characteristic.getProperties();
                        if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        Toast.makeText(mainActivity.getApplicationContext(),
                                getString(R.string.successfully_connected),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        Toast.makeText(mainActivity.getApplicationContext(),
                                getString(R.string.csc_not_supported),
                                Toast.LENGTH_SHORT)
                                .show();
                        mBluetoothLeService.disconnect();
                    }
                    break;
            }
        }

        // Choose the given bluetooth service from all the supported services and characteristics
        private BluetoothGattService getBluetoothGattService(String bluetoothServiceUUID) {
            BluetoothGattService gattService = null;
            List<BluetoothGattService> gattServiceList =
                    mBluetoothLeService.getSupportedGattServices();
            for (BluetoothGattService service : gattServiceList) {
                if (service.getUuid().toString().equals(bluetoothServiceUUID)) {
                    gattService = service;
                    break;
                }
            }
            return gattService;
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            startScanning();
        } else {
            stopScanning();
            // it is removing activity created in mHandler.postDelayed (stopScanning)
            // otherwise new scanning may be stopped faster than 10 seconds
            mHandler.removeCallbacks(runnable);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopScanning();
        }
    };

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mBTArrayAdapter.addDevice(result.getDevice());
            mBTArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult r : results) {
                mBTArrayAdapter.addDevice(r.getDevice());
            }
            mBTArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(mainActivity.getBaseContext(),
                        "Bluetooth not on",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            TextView connectedTextView = mView.findViewById(R.id.counter_connection_state);
            connectedTextView.setText(getString(R.string.connecting));
            BluetoothDevice selectedDevice = (BluetoothDevice) av.getItemAtPosition(position);

            Intent gattServiceIntent =
                    new Intent(mainActivity.getApplicationContext(), BluetoothLeService.class);
            mainActivity.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            mDeviceAddress = selectedDevice.getAddress();
            if (mScanning) {
                scanLeDevice(false);
            }
            // first connect will be called in onServiceConnected method
            if(mBluetoothLeService != null)
                mBluetoothLeService.connect(mDeviceAddress);
        }
    };

    private void stopScanning() {
        if(mBTAdapter.getBluetoothLeScanner() != null)
            mBTAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        mView.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.button_stop).setVisibility(View.INVISIBLE);
        mScanning = false;
        mainActivity.invalidateOptionsMenu();

    }

    private void startScanning() {
        if(!mBTAdapter.isEnabled()) {
            mBTAdapter.enable();
            Toast.makeText(getContext(),
                    getString(R.string.turning_on_bluetooth),
                    Toast.LENGTH_SHORT)
                    .show();
        }
        checkIfLocationIsEnabled();
        mBTArrayAdapter.clear();
        mBTArrayAdapter.notifyDataSetChanged();
        if(mBTAdapter.getBluetoothLeScanner() != null) {
            mBTAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
            mView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.button_scan).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.button_stop).setVisibility(View.VISIBLE);
            ((TextView) mView.findViewById(R.id.counter_connection_state))
                    .setText(getString(R.string.disconnected));
            mScanning = true;
            mainActivity.invalidateOptionsMenu();
        }
    }
    
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflater = DeviceScanFragment.this.getLayoutInflater();
        }

        void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.
                        deviceAddress = view.findViewById(R.id.device_address);
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // make filter to listen only for that types of intent with broadcast receiver
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }
}