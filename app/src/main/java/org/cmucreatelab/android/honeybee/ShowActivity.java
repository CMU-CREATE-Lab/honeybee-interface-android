package org.cmucreatelab.android.honeybee;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.cmucreatelab.android.genericblemodule.serial.SerialBleHandler;

import java.util.UUID;

public class ShowActivity extends AppCompatActivity {

    private final SerialBleHandler.NotificationListener notificationListener = new SerialBleHandler.NotificationListener() {
        @Override
        public void onNotificationReceived(final String messageSent, final String response) {
            Log.v(Constants.TAG, messageSent + " => " + response);
            //DialogHelper.displayDialog(ShowActivity.this, "Title", "Hello Clicky");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogHelper.displayDialog(ShowActivity.this, messageSent, response);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.TAG, "ShowActivity.onCreate");
        setContentView(R.layout.activity_honeybee_show);

        final TextView textView = (TextView)findViewById(R.id.textInputSecurityKey);
        findViewById(R.id.buttonJoinNetwork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(Constants.TAG, "button_send onClick");
                String text = textView.getText().toString();
                if (text.length() == 0) {
                    text = "ping";
                }

                GlobalHandler globalHandler = GlobalHandler.getInstance(getApplicationContext());
                UUID service = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
                UUID serviceChar = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
                // ASSERT globalHandler.deviceConnection not null
                BluetoothGattService bleService = globalHandler.serialBleHandler.getDeviceConnection().getService(service);
                BluetoothGattCharacteristic bleCharacteristic = bleService.getCharacteristic(serviceChar);
                BluetoothGattCharacteristic bleNotifyCharacteristic = bleService.getCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
                byte[] message = text.getBytes();

                globalHandler.serialBleHandler.sendMessageForResult(bleCharacteristic, bleNotifyCharacteristic, new String(message), notificationListener);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(Constants.TAG, "ShowActivity.onDestroy");


    }

}
