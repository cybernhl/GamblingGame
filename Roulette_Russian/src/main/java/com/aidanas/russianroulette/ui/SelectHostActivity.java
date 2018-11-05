package com.aidanas.russianroulette.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.adapters.BtDeviceArrayAdapter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 02/05/2016.
 *
 * Activity containing the screen which lists bluetooth devices so that user can select the one
 * which is currently hosting the game.
 */
public class SelectHostActivity extends Activity {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = SelectHostActivity.class.getSimpleName();

    // Key to access Intent extra.
    public static final String HOST_MAC_ADDR = "mac of the master device";

    // Views
    private ListView mBtDevicesLv;

    // Adapter handling population of the list.
    private BtDeviceArrayAdapter mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Const.DEBUG) Log.v(TAG, "In onCreate()");

        setContentView(R.layout.activity_select_host);

        setupListView();

    }

    /***********************************************************************************************
     *                            Only Android live cycle methods above this point!
     **********************************************************************************************/

    /**
     * Utility method to setup the list of Bluetooth Devices.
     */
    private void setupListView() {

        // Setup the list view and its adapter.
        mBtDevicesLv = (ListView) findViewById(R.id.ac_select_host_lw);
        mArrayAdapter = new BtDeviceArrayAdapter(this, R.layout.host_list_item,
                new ArrayList<BluetoothDevice>());
        mBtDevicesLv.setAdapter(mArrayAdapter);

        // Click listener will attempt to connect to the chosen device.
        mBtDevicesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Const.DEBUG) Log.v(TAG + "[ANON]", "In onItemClick() , position = " + position);

                /*
                 * Launch PlayingActivityClient for the specified host.
                 */
                Intent intent = new Intent(SelectHostActivity.this, PlayingActivityClient.class);
                intent.putExtra(HOST_MAC_ADDR, mArrayAdapter.getItem(position).getAddress());
                startActivity(intent);
            }
        });

        // Finally, add all currently paired devices as found on the device to the list.
        addPairedDevicesToList(mArrayAdapter);
    }

    /**
     * Method to add currently paired device list to the ListView.
     * @param arrayAdapter - Adapter of the list to which paired devices will be added.
     */
    private void addPairedDevicesToList(ArrayAdapter<BluetoothDevice> arrayAdapter) {

        // Get all currently paired devices.
        Set<BluetoothDevice> pairedDevices =
                BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                arrayAdapter.add(device);
            }
        }
    }

}
