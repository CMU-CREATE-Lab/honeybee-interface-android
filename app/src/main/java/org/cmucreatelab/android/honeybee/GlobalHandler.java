package org.cmucreatelab.android.honeybee;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.cmucreatelab.android.genericblemodule.ble_actions.ActionCharacteristicSetNotification;
import org.cmucreatelab.android.genericblemodule.generic_ble.GenericBleScanner;
import org.cmucreatelab.android.genericblemodule.serial.SerialBleHandler;

import java.util.UUID;

/**
 * Created by mike on 7/5/17.
 */

public class GlobalHandler {

    private static GlobalHandler instance;
    public final SerialBleHandler serialBleHandler;
    public GenericBleScanner genericBleScanner;
    //public GenericBleDeviceConnection deviceConnection;

    private GlobalHandler(Context appContext) {
        this.appContext = appContext;
        this.serialBleHandler = new SerialBleHandler(appContext);
        this.genericBleScanner = serialBleHandler.getScanner();
        this.genericBleScanner = new GenericBleScanner();
    }

    public static GlobalHandler getInstance(Context appContext) {
        if (instance == null) {
            instance = new GlobalHandler(appContext);
        }
        return instance;
    }

    private Context appContext;

    public void connectDevice(BluetoothDevice device, final ScanActivity activity) {
        serialBleHandler.connectDevice(device, new SerialBleHandler.ConnectionListener() {
            @Override
            public void onConnected(BluetoothGatt gatt) {
                Log.i(Constants.TAG, "discovered services");

                // NOTE: readCharacteristic and setCharacteristicNotification are asynchronous; calling them one after another will not work.
                // instead, you have to wait for each one to finish before going on to the next one.
                UUID service,serviceChar;

                // set up notifications
                service = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
                serviceChar = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
                final BluetoothGattCharacteristic characteristic = gatt.getService(service).getCharacteristic(serviceChar);
                final int charaProp = characteristic.getProperties();
                // ASSERT: this is true
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    setCharacteristicNotification(characteristic, true);
                }

                Intent intent = new Intent(activity, ShowActivity.class);
                activity.startActivity(intent);
            }
        });
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        // This is specific to our BLE device.
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        serialBleHandler.getDeviceConnection().send(new ActionCharacteristicSetNotification(characteristic, descriptor, enabled));
    }

}
