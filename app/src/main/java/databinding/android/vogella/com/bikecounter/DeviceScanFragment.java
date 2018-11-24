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

package databinding.android.vogella.com.bikecounter;

import android.Manifest;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;
import static databinding.android.vogella.com.bikecounter.SampleGattAttributes.*;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanFragment extends Fragment {
    private static final String TAG = "DeviceScanFragment";
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBTAdapter;
    private LeDeviceListAdapter mBTArrayAdapter;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private boolean mScanning;
    private Handler mHandler;
    private ListView mDevicesListView;
    private Button button_scan;
    private Button button_stop;
    private View mView;

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 2;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public static DeviceScanFragment newInstance() {
        return new DeviceScanFragment();
    }

    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.configuration_fragment, container, false);

        mBTArrayAdapter = new LeDeviceListAdapter();
        mDevicesListView=mView.findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mHandler = new Handler();

        button_scan = mView.findViewById(R.id.button_scan);
        button_stop = mView.findViewById(R.id.button_stop);


        // Use this check to determine whether BLE is supported on the device.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance()).commitNow();
        }

        // checking permission for location services
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION))
                        showExplanation(getString(R.string.permission_needed), getString(R.string.location_permission_message), Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
            else
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }



        // Initializes a Bluetooth adapter.
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();


        // Checks if Bluetooth is supported on the device.
        if (mBTAdapter == null) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance()).commitNow();
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
        LocationManager lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        boolean network_enabled;

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(getString(R.string.location_needed_for_searching_devices));
            dialog.setPositiveButton(getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getActivity().startActivityForResult(myIntent, REQUEST_LOCATION_PERMISSION);
                }
            });
            dialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // check if user didn't turned it
                    LocationManager lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        Toast.makeText(getContext(), getString(R.string.location_disabled_nothing_will_be_found), Toast.LENGTH_SHORT).show();
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
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{permissionName}, permissionRequestCode);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance()).commitNow();
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
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.

        // Initializes list view adapter.
        //mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        //scanLeDevice(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
        mBTArrayAdapter.clear();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
        if(mBluetoothLeService != null)
            mBluetoothLeService.close();
        try {
            getActivity().unregisterReceiver(mGattUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // if the receiver is not registered
            e.printStackTrace();
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)) {
                mConnected = false;
                Log.d(TAG, "Connecting");
                mView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.d(TAG, "Connected");
                mView.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
                getActivity().invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d(TAG, "Disconnected");
                mView.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
                getActivity().invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Choose the CSC Measurement characteristics from all the supported services and characteristics
                BluetoothGattService gattService = null;
                List<BluetoothGattService> gattServiceList =  mBluetoothLeService.getSupportedGattServices();
                for (BluetoothGattService service : gattServiceList) {
                    if (service.getUuid().toString().equals(CYCLING_SPEED_AND_CADENCE_SERVICE)) {
                        gattService = service;
                        break;
                    }
                }
                if (gattService != null) {
                    BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(CSC_MEASUREMENT_CHARACTERISTICS));
                    final int characteristicProperties = characteristic.getProperties();
                    if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        mBluetoothLeService.readCharacteristic(characteristic);
                    }
                    if ((characteristicProperties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.csc_not_supported), Toast.LENGTH_SHORT).show();
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Measurement measurement = Measurement.fromString(data);
                Log.d(TAG, "Speed: " + measurement.getSpeed());
                //TODO: Manage incoming data
                //displayData());
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopScanning();
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            startScanning();
        } else {
            stopScanning();
            // it is removing activity created in mHandler.postDelayed
            // as we don't want to stop new scanning 10 sec after old scan started
            mHandler.removeCallbacks(runnable);
        }
    }

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
                Toast.makeText(getActivity().getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            //mBluetoothStatus.setText("Connecting...");
            BluetoothDevice selectedDevice = (BluetoothDevice)av.getItemAtPosition(position);

            Intent gattServiceIntent = new Intent(getActivity().getApplicationContext(), BluetoothLeService.class);
            getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
        getActivity().invalidateOptionsMenu();

    }

    private void startScanning() {
        if(!mBTAdapter.isEnabled()) {
            mBTAdapter.enable();
            Toast.makeText(getContext(), getString(R.string.turning_on_bluetooth), Toast.LENGTH_SHORT).show();
        }
        checkIfLocationIsEnabled();
        mBTArrayAdapter.clear();
        mBTArrayAdapter.notifyDataSetChanged();
        if(mBTAdapter.getBluetoothLeScanner() != null) {
            mBTAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
            mView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.button_scan).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.button_stop).setVisibility(View.VISIBLE);
            mScanning = true;
            getActivity().invalidateOptionsMenu();
        }
    }
    
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = DeviceScanFragment.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
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
                view = mInflator.inflate(R.layout.listitem_device, null);
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

    // make filter to find only this intents with broadcast receiver
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}