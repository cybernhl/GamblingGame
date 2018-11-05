package com.aidanas.russianroulette.game;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.communication.BtConnectedThread;
import com.aidanas.russianroulette.communication.BtMasterThread;
import com.aidanas.russianroulette.communication.BtMsg;
import com.aidanas.russianroulette.interfaces.BluetoothSocketReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Aidanas
 * Created on: 21/04/2016.
 *
 * Class to contain the Russian Roulette game logic.
 */
public class Arbitrator implements BluetoothSocketReceiver{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = BtMasterThread.class.getSimpleName();

    // Message types passed to the main thread to update UI elements.
    public static final int MSG_UI_UPDATE_PLAYER_LIST = 40;
    public static final int MSG_UI_ALL_READY = 41;
    public static final int MSG_UI_ALIVE = 42;
    public static final int MSG_UI_DEAD = 43;

    // How many bullets a gun CAN have?
    private static final int GUN_CAPACITY = 6;

    // Bullets in the cylinder. Usually 1 :)
    private static final int BULLETS = 1;
    // Milliseconds to wait before the trigger is pulled.
    private static final long THRILL_DELAY = 1000;

    // Flag to be set for the device acting as a server of the game.
    private final boolean mIsServer;

    /*
     * Flag indicating that the current device is ready.
     * Volatile as written and read from distinct threads.
     */
    private volatile boolean mIsReady = false;

    // Master player and is socket (remains null if this device is the master).
    private Player mMasterPlayer;

    // To communicate with activity.
    private final Messenger mMessenger;

    // Holds a list of connected sockets. Only server would contains more than one item in it.
    private final List<BluetoothSocket> mConnectedSockets =
            Collections.synchronizedList(new ArrayList<BluetoothSocket>());

    // Incoming messages will  be processed by thread kept in this map.
    private final Map<String, BtConnectedThread> mConnectedThreadMap = new HashMap<>();

    // Players currently in the game.
    private final List<Player> mPlayersList = new ArrayList<>();

    /*
     * Looper thread and its handler. This will provide a separate thread which the communication
     * threads can use to post received messages.
     */
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    /**
     * Constructor.
     * @param isServer - Is the device running as the server of the game?
     */
    public Arbitrator(boolean isServer, Messenger messenger){
        mIsServer = isServer;
        mMessenger = messenger;

        // Setup the looper for JobCoordinator. It is going to be the main thread coordinating work.
        mHandlerThread = new HandlerThread("Arbitrators' Handler Thread");
        mHandlerThread.start();
        mHandler = new ArbitratorHandler(mHandlerThread.getLooper()); // TODO: Disable on termination
    }

    /**
     * Method to mark current device as ready. It makes sure the processing get done on a separate
     * from UI thread.
     */
    public void readyUp() {
        if (Const.DEBUG) Log.v(TAG, "In readyUp(), Thread = " + Thread.currentThread().getName());

        // Mrk this player as 'ready'.
        mIsReady = true;

        // Release the calling thread by delegating further processing to the handlers' thread.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Const.DEBUG) Log.v(TAG+"[ANON]", "In run(), Thread = " +
                        Thread.currentThread().getName());

                // Send the ready bluetooth message to all devices in the game.
                if (mIsServer){
                    sendToClients(BtMsg.STC_SERVER_READY, null);
                } else {
                    sendToMaster(BtMsg.CTS_CLIENT_READY, null);
                }

                // Is it time to spin the gun yet?
                if (allReady()){
                    playGame();
                }
            }
        });
    }

    /**
     * Method to mark current device as reset. It makes sure the processing get done on a separate
     * from UI thread.
     */
    public void reset() {
        if (Const.DEBUG) Log.v(TAG, "In reset(), Thread = " + Thread.currentThread().getName());

        // Mrk this player as 'ready'.
        mIsReady = false;

        // Release the calling thread by delegating further processing to the handlers' thread.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Const.DEBUG) Log.v(TAG+"[ANON]", "In run(), Thread = " +
                        Thread.currentThread().getName());

                // Send the player reset bluetooth message to all devices in the game.
                if (mIsServer){
                    sendToClients(BtMsg.STC_SERVER_RESET, null);
                } else {
                    sendToMaster(BtMsg.CTS_CLIENT_RESET, null);
                }
            }
        });
    }

    /**
     * Utility method to check if all players are in the 'ready' state.
     * @return - True if all players are ready, false otherwise.
     */
    private boolean allReady() {
        if (Const.DEBUG) Log.v(TAG, "In allReady(), Thread = " +
                Thread.currentThread().getName());

        // Return false if any of the players are not ready yet.
        for (Player p: mPlayersList) {
            if (!p.isReady()){
                return false;
            }
        }

        // True if all and this player are ready.
        return mIsReady;
    }

    /**
     * This method contains the logic of a russian roulette game.
     * All devices must be in 'ready' mode before calling this method!
     */
    private void playGame() {
        if (Const.DEBUG) Log.v(TAG, "In playGame(), Thread = " + Thread.currentThread().getName());

        // Change title of the activity to "Playing...".
        postToUiHandler(MSG_UI_ALL_READY, null);

        Gun gun = new Gun(GUN_CAPACITY);
        gun.loadBullets(BULLETS);
        gun.spinCylinder();

        try {
            Thread.sleep(THRILL_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // The moment of truth.
        if (gun.pullTheTrigger()){
            dead();
        } else {
            alive();
        }
    }

    /**
     * Method gets called if the player lost the game.
     */
    private void dead() {
        if (Const.DEBUG) Log.v(TAG, "In dead(), Thread = " + Thread.currentThread().getName());

    }

    /**
     * Method gets called if this player remained alive after a round of the game.
     */
    private void alive() {
        if (Const.DEBUG) Log.v(TAG, "In alive(), Thread = " + Thread.currentThread().getName());

        postToUiHandler(MSG_UI_ALIVE, null);

        // Inform others about the outcome of your game.
        if (mIsServer){
            notifyClientsServerAlive();
        } else {
            notifyServerClientAlive();
        }
    }

    /**
     * Method to process a newly come device.
     * @param bluetoothSocket - Connected socket to the new device.
     */
    private void newPlayer(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In newPlayer(), bluetoothSocket" + bluetoothSocket);

        // If running as the host then update UI and inform other clients.
        if (mIsServer){
            mConnectedSockets.add(bluetoothSocket);
            Player p = makePlayerFromSocket(bluetoothSocket);
            mPlayersList.add(p);
            updateUiPlayerList();
            notifyClientsNewPlayer(p);
            notifyNewPlayerAboutCurrentPlayers(p);
        }
    }

    /**
     * Utility method to instantiate and initialise a player object from a given bluetooth socket.
     * @param bluetoothSocket - Conected Bluetooth socket.
     * @return - Player object.
     */
    private Player makePlayerFromSocket(BluetoothSocket bluetoothSocket) {
        if (Const.DEBUG) Log.v(TAG, "In makePlayerFromSocket(), socket = "+
                bluetoothSocket.getRemoteDevice().getName());

        return new Player(bluetoothSocket.getRemoteDevice().getName(),
                bluetoothSocket.getRemoteDevice().getAddress());
    }

    /**
     * Method to send a list of current players to the newly arrived player.
     * @param p - Newly arrived player which should be provides with a list of current players.
     */
    private void notifyNewPlayerAboutCurrentPlayers(Player p) {
        if (Const.DEBUG) Log.v(TAG, "In notifyNewPlayerAboutCurrentPlayers(), player = " +
                p.getName());

        // List should contain all but the receiving players.
        List<Player> players = new ArrayList<>(mPlayersList);
        if (!players.remove(p)){
            throw new RuntimeException("Player list could not be modified!");
        }

        // Construct and send the message.
        BtMsg btMsg = new BtMsg(BtMsg.STC_PLAYERS_LIST, players);
        mConnectedThreadMap.get(p.getAddress()).write(btMsg);
    }

    /**
     * Method to inform all clients that a new player has arrived.
     * @param player - New player.
     */
    private void notifyClientsNewPlayer(Player player) {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsNewPlayer(), player = " + player.getName());

        for (BtConnectedThread t: mConnectedThreadMap.values()) {
            String addr = t.getBluetoothSocket().getRemoteDevice().getAddress();
            if (!addr.equals(player.getAddress())){
                BtMsg btmsg = new BtMsg(BtMsg.STC_NEW_PLAYER, player);
                t.write(btmsg);
            }
        }
    }

    /**
     * Method to notify all clients that one of the players changed their status to 'ready'. The
     * method uses the MAC address of the player to identify it.
     * @param mac - Mac address of the Player who has changed their status to 'Ready'
     */
    private void notifyClientsPlayerReadyByMAC(String  mac) {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsPlayerReadyByMAC(), mac = " + mac +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null){
            throw new IllegalStateException("Player to be marked as READY not found! MAC:" + mac);
        } else {
            sendToClientsButOne(BtMsg.STC_PLAYER_READY, p, p);
        }
    }

    /**
     * Method sends messages to all clients indicating that the host has played a round of
     * the game and is yet alive.
     */
    private void notifyClientsServerAlive() {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsServerAlive(), Thread = " +
                Thread.currentThread().getName());

        sendToClients(BtMsg.STC_SERVER_ALIVE, null);
    }

    /**
     * Method sends messages to all clients indicating that the host has played a round of
     * the game and is yet alive.
     * @param mac - Adress of the player who has survived the round.
     */
    private void notifyClientsPlayerAliveByMAC(String mac) {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsPlayerAliveByMAC(), mac = " + mac +
                "Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null){
            throw new IllegalStateException("Player to be marked as ALIVE not found! MAC:" + mac);
        } else {
            sendToClientsButOne(BtMsg.STC_PLAYER_ALIVE, p, p);
        }
    }

    /**
     * Method sends a message to the server device indicating that this player has played a round of
     * the game and is yet alive.
     */
    private void notifyServerClientAlive() {
        if (Const.DEBUG) Log.v(TAG, "In notifyServerClientAlive(), Thread = " +
                Thread.currentThread().getName());

        sendToMaster(BtMsg.CTS_CLIENT_ALIVE, null);
    }

    /**
     * Method sends messages to all clients indicating that a player has transitioned to reset
     * state.
     * @param mac - Address of the player who wants to play another round.
     */
    private void notifyClientsPlayerResetByMAC(String mac) {
        if (Const.DEBUG) Log.v(TAG, "In notifyClientsPlayerResetByMAC(), mac = " + mac +
                "Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null){
            throw new IllegalStateException("Player to be marked as RESET not found! MAC:" + mac);
        } else {
            sendToClientsButOne(BtMsg.STC_PLAYER_RESET, p, p);
        }
    }

    /**
     * Utility method to construct a BtMsg with given arguments as fields and send it to all clients
     * of this master. Obviously, this should only be called from the master devices.
     * @param type - One of the BtMsg constants indicating type. Used in a switch upon reception.
     * @param payload - Arbitrary serializable object.
     */
    private void sendToClients(int type, Object payload) {
        if (Const.DEBUG) Log.v(TAG, "In sendToClients(), type = " + type + ", payload = " +
                payload + ", Thread = " +Thread.currentThread().getName());

        BtMsg btMsg = new BtMsg(type, payload);

        for (BtConnectedThread t : mConnectedThreadMap.values()) {
            t.write(btMsg);
        }
    }

    /**
     * Utility method to construct a BtMsg with given arguments as fields and send it to all but one
     * clients of this master. Obviously, this should only be called from the master devices.
     * @param type - One of the BtMsg constants indicating type. Used in a switch upon reception.
     * @param payload - Arbitrary serializable object.
     * @param player - The player which will be excluded from recipient list of this message.
     */
    private void sendToClientsButOne(int type, Object payload, Player player) {
        if (Const.DEBUG) Log.v(TAG, "In sendToClientsButOne(), type = " + type + ", payload = " +
                payload + ", Thread = " +Thread.currentThread().getName());

        BtMsg btMsg = new BtMsg(type, payload);

        for (BtConnectedThread t: mConnectedThreadMap.values()) {
            String addr = t.getBluetoothSocket().getRemoteDevice().getAddress();
            if (!addr.equals(player.getAddress())){
                t.write(btMsg);
            }
        }
    }

    /**
     * Utility method to construct a BtMsg with given arguments as fields and send it to the master
     * device.
     * @param type - One of the BtMsg constants indicating type. Used in a switch upon reception.
     * @param payload - Arbitrary serializable object.
     */
    private void sendToMaster(int type, Object payload){
        if (Const.DEBUG) Log.v(TAG, "In sendToMaster(), type = " + type + ", payload = " + payload +
                ", Thread = " +Thread.currentThread().getName());

        BtMsg btMsg = new BtMsg(type, payload);
        mConnectedThreadMap.get(mMasterPlayer.getAddress()).write(btMsg);
    }

    /**
     * Method to mark a player as being 'ready'.
     * @param p - The player to be marked as ready.
     */
    private void markPlayerReady(Player p) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerReady(), player = " + p.getName() +
                ", Thread = " + Thread.currentThread().getName());

        Player player = getMatchingPlayer(p);
        if (player != null){
            player.setReady();
            updateUiPlayerList();
        }
    }

    /**
     * Method to mark a player with a given mac address as being 'ready'.
     * @param mac - The player to be marked as ready.
     */
    private void markPlayerReadyByMAC(String mac) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerReadyByMAC(), mac = " + mac +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null){
            throw new IllegalStateException("Player to be marked as READY not found! MAC:" + mac);
        } else {
            markPlayerReady(p);
        }
    }

    /**
     * Method marks a player as being in an 'alive' state.
     * @param player - Player indicating that it is still alive after a round was played.
     */
    private void markPlayerAlive(Player player) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerAlive(), player = " + player.getName() +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getMatchingPlayer(player);
        if (p == null) {
            throw new IllegalStateException("Player to be marked as ALIVE not found! Player:" +
                    player.getName());
        } else {
            p.setAlive();
            updateUiPlayerList();
        }
    }

    /**
     * Method marks a player with specified mac address as being in an 'alive' state.
     * @param mac - Mac address of the player indicating that it is still alive after a round was
     *            played.
     */
    private void markPlayerAliveByMAC(String mac) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerAliveByMAC(), mac = " + mac +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null) {
            throw new IllegalStateException("Player to be marked as ALIVE not found! MAC:" + mac);
        } else {
            markPlayerAlive(p);
        }
    }

    /**
     * Method marks a player as in 'reset' state. Meaning that it is wishing to play another round
     * of the game.
     * @param player - Player which desires to play another round.
     */
    private void markPlayerReset(Player player) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerReset(), player = " + player.getName() +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getMatchingPlayer(player);
        if (p == null) {
            throw new IllegalStateException("Player to be marked as RESET not found! Player:" +
                    player.getName());
        } else {
            p.setReset();
            updateUiPlayerList();
        }
    }

    /**
     * Method marks a player with specified mac as in 'reset' state. Delegates to markPlayerReset()
     * after the player has been acquired.
     * @param mac - Address of the Player which desires to play another round.
     */
    private void markPlayerResetByMAC(String mac) {
        if (Const.DEBUG) Log.v(TAG, "In markPlayerReset(), mac = " + mac +
                ", Thread = " + Thread.currentThread().getName());

        Player p = getPlayerByMac(mac);
        if (p == null) {
            throw new IllegalStateException("Player to be marked as RESET not found! MAC:" + mac);
        } else {
            markPlayerReset(p);
        }
     }

    /**
     * Method to obtain the player with a given mac address.
     * @param srcMAC - Mac address of the player to be found.
     * @return - Player from the mPlayersList with a matching MAC address OR 'null' if not found.
     */
    private Player getPlayerByMac(String srcMAC) {
        if (Const.DEBUG) Log.v(TAG, "In getPlayerByMac(), srcMAC = " + srcMAC +
                ", Thread = " + Thread.currentThread().getName());

        for (Player p : mPlayersList) {
            if (p.getAddress().equals(srcMAC)){
                return p;
            }
        }
        return null;
    }

    /**
     * Method to gert a Player object from the mPlayersList with the matching details (name).
     * @param player - Player reference to be found.
     * @return - Player object corresponding to the one provided as the argument OR 'null' if not
     * found.
     */
    private Player getMatchingPlayer(Player player){
        if (Const.DEBUG) Log.v(TAG, "In getMatchingPlayer(), player = " + player.getName() +
                ", Thread = " + Thread.currentThread().getName());

        for (Player p : mPlayersList) {
            if (p.getName().equals((player.getName()))){
                return p;
            }
        }
        return null;
    }

    /**
     * Method to update player list on the UI.
     */
    private void updateUiPlayerList() {
        if (Const.DEBUG) Log.v(TAG, "In updateUiPlayerList()");

        postToUiHandler(MSG_UI_UPDATE_PLAYER_LIST, mPlayersList);
    }

    /**
     * Utility method to enqueue a message onto the main thread's looper.
     * @param what - Type of the message.
     * @param obj - Arbitrary object to be delivered to the main thread.
     */
    private void postToUiHandler(int what, Object obj){
        if (Const.DEBUG) Log.v(TAG, "In postToUiHandler(), what = " + what + ", obj = " + obj +
                ", Thread = " + Thread.currentThread().getName());

        Message m = Message.obtain();
        m.what    = what;
        m.obj     = obj;
        try {
            mMessenger.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to update the current list of players with the new one. Method takes care to preserve
     * the master and eliminate duplicates. This gets called when a client joins the server only
     * once.
     * @param players - List of currently playing users.
     */
    private void updatePlayersList(List<Player> players) {
        if (Const.DEBUG) Log.v(TAG, "In updatePlayersList(), players.size() = " + players.size());

        mPlayersList.addAll(players);
        updateUiPlayerList();
    }

    /**
     * Method to return bytes of en object passed as a parameter to it.
     * @param obj - Object to returned as an array of bytes.
     * @return - Byte array.
     */
    private byte[] objTobyteArr(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    /**
     * MMethod to construct an object out of bytes provided in the array supplied as the argument
     * @param arr - Array of bytes from which the object will be read.
     * @return - Object read from the byte array.
     */
    private Object byteArrToObj(byte[] arr) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        ObjectInput in = new ObjectInputStream(bis);
        Object obj = null;
        try {
            obj = in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
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

        // For every connected socket spawn a new thread for reading data.
        try {
            BtConnectedThread t = new BtConnectedThread(bluetoothSocket, mHandler);
            t.start();
            mConnectedThreadMap.put(bluetoothSocket.getRemoteDevice().getAddress(), t);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Server should inform others about the new player. Client just adds the socket as a
         * player saving a reference to the player as 'MasterPlayer'.
         */
        if (mIsServer){
            newPlayer(bluetoothSocket);
        } else {
            mPlayersList.add(mMasterPlayer = makePlayerFromSocket(bluetoothSocket));
            updateUiPlayerList();
        }
    }

    /***********************************************************************************************
     *                                  Inner Classes
     **********************************************************************************************/

    /**
     * Handler class. The purpose of this class is so that other Threads could pass messages and
     * runnable objects to the arbitrator's tread. it allows the UI thread to return quickly and
     * communication to be handled by blocking calls.
     */
    private class ArbitratorHandler extends Handler {

        // Tag, mostly used for logging output.
        public final String TAG = ArbitratorHandler.class.getSimpleName();

        /**
         * Constructor simply delegates the looper to its superclass.
         * @param looper - Message queue.
         */
        public ArbitratorHandler(Looper looper){
            super(looper);
        }
        /**
         * Method processes the messages posted onto this handler.
         * @param inputMessage - Message received. Fields of interest : 'what' and 'obj'.
         */
        @Override
        public void handleMessage(Message inputMessage){
            if (Const.DEBUG) Log.v(TAG, "In handleMessage(), msg.what = " + inputMessage.what +
                    ", msg.obj = " + inputMessage.obj + ", Thread = " +
                    Thread.currentThread().getName());

            BtMsg btMsg = (BtMsg) inputMessage.obj;

            // Switch on the type of the BtMsg.
            switch (inputMessage.what){

                case BtMsg.STC_NEW_PLAYER:
                    mPlayersList.add((Player)btMsg.payload);
                    updateUiPlayerList();
                    break;

                case BtMsg.STC_PLAYERS_LIST:
                    List<Player> players = (List<Player>) btMsg.payload;
                    updatePlayersList(players);
                    break;

                case BtMsg.STC_SERVER_READY:
                    markPlayerReady(mMasterPlayer);
                    // Is it time to spin the gun yet?
                    if (allReady()){
                        playGame();
                    }
                    break;

                case BtMsg.STC_PLAYER_READY:
                    markPlayerReady((Player)btMsg.payload);
                    // Is it time to spin the gun yet?
                    if (allReady()){
                        playGame();
                    }
                    break;

                case BtMsg.STC_SERVER_ALIVE:
                    markPlayerAlive(mMasterPlayer);
                    break;

                case BtMsg.STC_PLAYER_ALIVE:
                    markPlayerAlive((Player)btMsg.payload);
                    break;

                case BtMsg.STC_SERVER_RESET:
                    markPlayerReset(mMasterPlayer);
                    break;

                case BtMsg.STC_PLAYER_RESET:
                    markPlayerReset((Player)btMsg.payload);
                    break;

                case BtMsg.CTS_CLIENT_READY:
                    markPlayerReadyByMAC(btMsg.srcMAC);
                    notifyClientsPlayerReadyByMAC(btMsg.srcMAC);
                    // Is it time to spin the gun yet?
                    if (allReady()){
                        playGame();
                    }
                    break;

                case BtMsg.CTS_CLIENT_ALIVE:
                    markPlayerAliveByMAC(btMsg.srcMAC);
                    notifyClientsPlayerAliveByMAC(btMsg.srcMAC);
                    break;

                case BtMsg.CTS_CLIENT_RESET:
                    markPlayerResetByMAC(btMsg.srcMAC);
                    notifyClientsPlayerResetByMAC(btMsg.srcMAC);
                    break;

                default:
                    super.handleMessage(inputMessage);
            }
        }
    }


}
