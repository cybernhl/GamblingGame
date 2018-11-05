package com.aidanas.russianroulette.game;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Aidanas
 * Created on: 21/04/2016.
 *
 * Class to contain the Russian Roulette game logic. This class should be instantiated and run only
 * on a device running as a client (NOT as the host of the game!)
 */
public class ArbitratorDummy implements BluetoothSocketReceiver{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    // Flag to be set for the device acting as a server of the game.
    private final boolean mIsServer;

    // Holds a list of connected sockets. Only server would contains more than one item in it.
    private final List<BluetoothSocket> mConnectedSockets =
            Collections.synchronizedList(new ArrayList<BluetoothSocket>());

    /**
     * Constructor.
     * @param isServer - Is the device running as the server of the game?
     */
    public ArbitratorDummy(boolean isServer){
        mIsServer = isServer;
    }

    /***********************************************************************************************
     *                                  Interface Implementations
     **********************************************************************************************/

    /**
     * Callback method which is called when connection is established providing a connected
     * Bluetooth socket as an argument.
     * @param bluetoothSocket - Connected Bluetooth socket.
     */
    @Override
    public synchronized void receiveSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In receiveSocket(), Adding connected socket to:" +
                bluetoothSocket.getRemoteDevice().getAddress() + ", Thread = " +
                Thread.currentThread().getName());

        mConnectedSockets.add(bluetoothSocket);
    }
}
