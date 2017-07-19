package org.cmucreatelab.android.honeybee;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.cmucreatelab.android.genericblemodule.generic_ble.GenericBleScanner;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
//    private BluetoothGatt gatt;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    // ----
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ScanActivity.this.getLayoutInflater();
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
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            // set click listener
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(Constants.TAG, "listitem_device onClick");
                    GlobalHandler.getInstance(getApplicationContext()).genericBleScanner.scanLeDevice(false, mLeScanCallback);

                    // attempt ble connection
                    Log.v(Constants.TAG, "attempting connectGatt on device="+deviceName);
                    GlobalHandler.getInstance(getApplicationContext()).connectDevice(device, ScanActivity.this);

                }
            });

            return view;
        }
    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
    // ----


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // check for location permissions (api level 23 or higher)
        if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        // click listener
        findViewById(R.id.button_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(Constants.TAG, "button_scan onClick (scan for 10s)");

                // bluetooth actions on click
                GenericBleScanner genericBleScanner = GlobalHandler.getInstance(getApplicationContext()).genericBleScanner;
                genericBleScanner.enableBluetooth(ScanActivity.this);
                genericBleScanner.scanLeDevice(true, mLeScanCallback);
            }
        });

        ListView listView = (ListView)findViewById(R.id.list_view);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        listView.setAdapter(mLeDeviceListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(Constants.TAG, "MainActivity.onResume");
        GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());
        if (globalHandler.serialBleHandler.getDeviceConnection() != null && globalHandler.serialBleHandler.getDeviceConnection().isConnected()) {
            Log.i(Constants.TAG, "closing non-null BluetoothGatt");
            globalHandler.serialBleHandler.getDeviceConnection().disconnect();
        }
    }
}
