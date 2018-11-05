package com.aidanas.russianroulette.communication;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.aidanas.russianroulette.Const;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to holds the logic for reading and writing into connected bluetooth sockets.
 */
public class BtConnectedThread extends Thread{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtConnectedThread.class.getSimpleName();

    // Size of the input buffer in bytes.
    private static final int BUFFER_SIZE = 1024;

    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    private final BluetoothSocket mBluetoothSocket;

    // Messages received will be passed to this handler for processing.
    private final Handler mHandler;

    /**
     * Constructor.
     * @param bluetoothSocket - A connected Bluetooth socket through which the communication will be
     *                        going.
     * @param handler - Handler to which received data will be passed.
     */
    public BtConnectedThread(BluetoothSocket bluetoothSocket, Handler handler)
            throws IOException {
        mInputStream = bluetoothSocket.getInputStream();
        mOutputStream = bluetoothSocket.getOutputStream();
        mBluetoothSocket = bluetoothSocket;
        mHandler = handler;
    }

    /**
     * Thread's starting point.
     */
    public void run() {
        if (Const.DEBUG) Log.v(TAG, "In run(), Thread = " + Thread.currentThread().getName());

        while (true) {
            try {
                if (Const.DEBUG) Log.v(TAG, "In run(), reading " +
                        mBluetoothSocket.getRemoteDevice().getAddress());

                // We expecting to read objects from the stream.
                ObjectInputStream ois = new ObjectInputStream(mInputStream);
                BtMsg btMsg = (BtMsg) ois.readObject();
                passToHandler(btMsg);

            } catch (IOException e) {
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to send date to the remote device.
     * @param btmsg - Message to be written to the socket.
     */
    public void write(BtMsg btmsg) {
        if (Const.DEBUG) Log.v(TAG, "In write(), writing to: " +
                mBluetoothSocket.getRemoteDevice().getAddress() +
                ", bytes = " + btmsg.type);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(mOutputStream);
            oos.writeObject(btmsg);
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to close the connection and terminate the thread.
     */
    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) { }
    }

    /**
     * Method to construct a message form the passed parameters and post them to the Arbitrators'
     * handler.
     * @param btMsg  - Object to be passed to the main thread.
     */
    private void passToHandler(BtMsg btMsg) {
        if (Const.DEBUG) Log.v(TAG, "In passToHandler(), btMsg.type = " + btMsg.type +
                ", Thread = " + Thread.currentThread().getName());

        // Include senders address in the message.
        btMsg.srcMAC = mBluetoothSocket.getRemoteDevice().getAddress();

        Message msg = Message.obtain();
        msg.what = btMsg.type;
        msg.obj  = btMsg;
        mHandler.sendMessage(msg);
    }

    /**
     * Method ot obtain the socket to which this thread is assisted with.
     * @return - Socket used by this thread.
     */
    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }
}
