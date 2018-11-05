package com.aidanas.russianroulette.interfaces;

import android.bluetooth.BluetoothSocket;

/**
 * Created by: Aidanas
 * Created on: 21/04/2016.
 *
 * Callback interface for classes which can accept a Bluetooth socket.
 */
public interface BluetoothSocketReceiver {
    void receiveSocket(BluetoothSocket bluetoothSocket);
}
