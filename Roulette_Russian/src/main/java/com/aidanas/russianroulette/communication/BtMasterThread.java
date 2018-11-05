package com.aidanas.russianroulette.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
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
 * Thread to open a Bluetooth connection socket which will listen for any incoming connection
 * attempts by other devices.
 */
public class BtMasterThread extends Thread {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    private final BluetoothServerSocket mServerSocket;

    // Connected sockets will be passed to this object.
    private final BluetoothSocketReceiver mSocketReceiver;


    /**
     * Constructor.
     * @param name - Name of of the program to be used in the SDP entry.
     * @param uuid - Unique ID of the program to be used in the SDP entry.
     * @param socketReceiver - Connected Bluetooth sockets will be passed to this object.
     */
    public BtMasterThread(String name, UUID uuid, BluetoothSocketReceiver socketReceiver)
            throws IOException {

        // Open a Bluetooth server socket to listen for incoming connections.
        mServerSocket =
                BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(name, uuid);
        mSocketReceiver = socketReceiver;
    }

    /**
     * New thread starts here.
     */
    public void run() {
        if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " + Thread.currentThread().getName());

        BluetoothSocket bluetoothSocket;

        /*
         * Accept as many connections as we can. Passing them to the arbitrator.
         */
        while (true) {
            try {
                bluetoothSocket = mServerSocket.accept();
            } catch (IOException e) {
                // Caused as well by method call to cancel().
                break;
            }
            // If a connection was accepted pass it to the handler.
            if (bluetoothSocket != null) {
                mSocketReceiver.receiveSocket(bluetoothSocket);
            }
        }
    }

    /**
     * Closes the server listening socket causing the thread to finish.
     */
    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            // Ignore close exception.
        }
    }

}
