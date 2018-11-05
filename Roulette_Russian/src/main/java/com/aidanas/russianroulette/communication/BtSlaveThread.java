package com.aidanas.russianroulette.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 21/04/2016.
 *
 * Class to initiate Bluetooth connection to a remote device.
 */
public class BtSlaveThread extends Thread{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtSlaveThread.class.getSimpleName();

    // Connected sockets will be passed to this object.
    private final BluetoothSocketReceiver mSocketReceiver;

    // Holds the connected socket to the master device.
    private final BluetoothSocket mSocket;

    /**
     * Constructor.
     * @param device - Remote Bluetooth device to connect to (must specify MAC address).
     * @param uuid - Unique ID of the program to be used when querying the remote device for SDP
     *             entry with matching uuid.
     * @param socketReceiver - Connected Bluetooth sockets will be passed to this object.
     */
    public BtSlaveThread(BluetoothDevice device, UUID uuid,
                         BluetoothSocketReceiver socketReceiver) throws IOException {

        mSocket = device.createRfcommSocketToServiceRecord(uuid);
        mSocketReceiver = socketReceiver;
    }

    /**
     * New thread starts here.
     */
    public void run() {
        if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " + Thread.currentThread().getName());

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        /*
         * Try to connect to the remote Bluetooth device. This is blocking a call! If in the
         * meanwhile cancel() is called from some other thread this will throw and catch an
         * exception which will cause the method to return and the thread to finish.
         */
        try {
            mSocket.connect();
        } catch (IOException connectException) {
            try {
                mSocket.close();
            } catch (IOException closeException) {
                // Ignore close exception.
            }
            return;
        }

        // Bluetooth connection successfully established, pass the connected socket.
        mSocketReceiver.receiveSocket(mSocket);
    }

    /**
     * Call from other thread to terminate the connection attempt.
     */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            // Ignore close exception.
        }
    }

}
