package com.aidanas.russianroulette.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.communication.BtSlaveThread;
import com.aidanas.russianroulette.game.Arbitrator;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;
import com.aidanas.russianroulette.ui.PlayingActivityServer;
import com.aidanas.russianroulette.ui.SelectHostActivity;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 22/04/2016.
 * <p>
 * Service will handle communication and Bluetooth sockets away from the lifecycle of activity and
 * in a separate thread.
 */
public class GameService extends Service  implements BluetoothSocketReceiver {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = GameService.class.getSimpleName();

    // Activity->Service communication lifeline.
    private final IBinder mBinder = new ServiceBinder();

    // Service->Activity communication line.
    private Messenger mMessenger;

    // Top level job coordinator. Responsible for global 'view' of job processing.
    private Arbitrator mArbitrator;

    // Server device flag.
    private Boolean mIsServer;

    private boolean isStarted = false;

    // Bluetooth adapter which will be used for communication.
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // Thread for initiating Bluetooth connections.
    private BtMasterThread mBtMasterThread;
    private BtSlaveThread mBtSlaveThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (Const.DEBUG) Log.v(TAG, "In onBind(), Thread = " + Thread.currentThread().getName());

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Const.DEBUG) Log.v(TAG, "In onCreate(), Thread = " + Thread.currentThread().getName());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Const.DEBUG) Log.v(TAG, "In onStartCommand(), Thread = " +
                Thread.currentThread().getName());

        Bundle bundle = intent.getExtras();
        mMessenger = (Messenger) bundle.get(PlayingActivityServer.MESSENGER);

        // Game arbitrator and the communication should only be initiated once.
        if (!isStarted) {
            mIsServer = bundle.getBoolean(PlayingActivityServer.IS_SERVER);
            isStarted = true;
            initArbitrator(mIsServer);
            initCommunication(mIsServer, mIsServer ? null :
                    bundle.getString(SelectHostActivity.HOST_MAC_ADDR));
        }

        // Die with the process.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Const.DEBUG) Log.v(TAG, "In onDestroy(),Thread = " + Thread.currentThread().getName());

    }

    /***********************************************************************************************
     *                            Only Android live cycle methods above this point!
     **********************************************************************************************/

    /**
     * This gets called by an activity when user chicks 'I'm Ready' button. Method simply delegates
     * the notification to the Arbitrator.
     */
    public void readyUp() {
        if (Const.DEBUG) Log.v(TAG, "In readyUp(), Thread = " + Thread.currentThread().getName());

        mArbitrator.readyUp();
    }

    /**
     * Method called by activity after the user indicates that their want to play another round of
     * the game. Delegates the intent to the Arbitrator object.
     */
    public void reset() {
        if (Const.DEBUG) Log.v(TAG, "In reset(), Thread = " + Thread.currentThread().getName());

        mArbitrator.reset();
    }

    /**
     * Method to create and initialise the arbitrator of the game.
     * @param isServer - Tue if the device is hosting the game.
     */
    private void initArbitrator(Boolean isServer) {
        if (Const.DEBUG) Log.v(TAG, "In initArbitrator(), isServer = " + isServer);

        mArbitrator = new Arbitrator(isServer, mMessenger);
    }

    /**
     * Method to initialise Bluetooth communication.
     * @param mIsServer - Create listening socket or attempt to connect to a remote one?
     * @param mastersMacAddress - Mac address of a remote bluetooth device to be connected to.
     *                          null if it is starting as a server device.
     */
    private void initCommunication(Boolean mIsServer, String mastersMacAddress) {
        if (Const.DEBUG) Log.v(TAG, "In initCommunication(), mastersMac = " + mastersMacAddress +
                " Thread = " + Thread.currentThread().getName());

        if (mIsServer){
            startBtServer();
        } else {
            startBtClient(mBluetoothAdapter.getRemoteDevice(mastersMacAddress));
        }
    }

    /**
     * Method to initialise and start a Master Bluetooth thread to listen for incoming connections.
     */
    private void startBtServer() {
        if (Const.DEBUG) Log.v(TAG, "In startBtServer(), Thread = " +
                Thread.currentThread().getName());

        try {
            mBtMasterThread = new BtMasterThread(getString(R.string.app_name),
                    UUID.fromString(getString(R.string.UUID)), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBtMasterThread.start();
    }

    /**
     * Method to initialise and start a Slave Bluetooth thread to connect to a remote device as
     * specified by the argument.
     * @param bluetoothDevice - Remote device to be connected to.
     */
    private void startBtClient(BluetoothDevice bluetoothDevice) {
        if (Const.DEBUG) Log.v(TAG, "In startBtClient(), connect to = " +
                bluetoothDevice.getAddress() + ", Thread = " + Thread.currentThread().getName());

        try {
            mBtSlaveThread = new BtSlaveThread(bluetoothDevice,
                    UUID.fromString(getString(R.string.UUID)), this);
            mBtSlaveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to construct a message form the passed parameters and post them to the messenger.
     * @param what - Message type.
     * @param arg1 - Argument for message arg1 field.
     * @param arg2 - Argument for message arg2 field.
     * @param obj  - Object to be passed to the main thread.
     */
    private void passToMessenger(int what, int arg1, int arg2, Object obj) {
        if (Const.DEBUG) Log.v(TAG, "In passToMessenger(), dispatching msg.what = " + what);

        Message msg = Message.obtain();
        msg.what    = what;
        msg.arg1    = arg1;
        msg.arg2    = arg2;
        msg.obj     = obj;

        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /***********************************************************************************************
     *                                  Interface Implementations
     **********************************************************************************************/

    /**
     * Callback method implementation which gets called each time a new connected socket is made.
     * It spawns a new thread sor each socket received in order to process incoming communication.
     * @param bluetoothSocket - Newly connected Bluetooth socket.
     */
    @Override
    public void receiveSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In receiveSocket(), bluetoothSocket = " +
                bluetoothSocket.getRemoteDevice().getAddress());

        // Pass the socket to the arbitrator.
        mArbitrator.receiveSocket(bluetoothSocket);
    }

    /***********************************************************************************************
     *                                  Inner Classes
     **********************************************************************************************/

    /**
     * This Binder subclass will be provided to the activity after it binds to the service and
     * becomes a client of this service. This happens after activity calls onBind() passing
     * ServerConnection as a parameter. At some point later (call is asynchronous) ServerConnection
     * object's method onServiceConnected() receiving an instance of this class for its argument
     * (as returned from onBind() method). Therefore activity can use this binder to obtain a
     * reference to this service and communicate with it as necessary by calling its public methods.
     */
    public class ServiceBinder extends Binder {

        // Tag, mostly used for logging and debug output.
        public final String TAG = ServiceBinder.class.getSimpleName();

        public GameService getGameService() {
            if (Const.DEBUG) Log.v(TAG, "In getGameService(), Thread = " +
                    Thread.currentThread().getName());

            return GameService.this;
        }
    }
}




