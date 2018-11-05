package com.aidanas.russianroulette.communication;

import java.io.Serializable;

/**
 * Created by: Aidanas Tamasauskas
 * Created on: 21/04/2016.
 *
 * Class to model a single inter device Bluetooth message.
 */
public class BtMsg implements Serializable{

    public static final int MASTER_NOT_LISTENING = 20;
    public static final int CONNECTED_MASTER_SOCKET = 22;

    public static final int SLAVE_ACK = 10;
    public static final int SLAVE_CONNECTION_FAIL = 11;
    public static final int CONNECTED_SLAVE_SOCKET = 12;

    // Server To Client message types.
    public static final int STC_NEW_PLAYER   = 501;
    public static final int STC_PLAYERS_LIST = 502;
    public static final int STC_SERVER_READY = 503;
    public static final int STC_PLAYER_READY = 504;
    public static final int STC_SERVER_ALIVE = 510;
    public static final int STC_PLAYER_ALIVE = 511;
    public static final int STC_SERVER_RESET = 520;
    public static final int STC_PLAYER_RESET = 521;

    // Client To Server message types.
    public static final int CTS_CLIENT_READY = 603;
    public static final int CTS_CLIENT_ALIVE = 610;
    public static final int CTS_CLIENT_RESET = 620;

    // Contents of a message passed between players.
    public int type;
    public Object payload; // Must be cast to the expected object type upon reception.

    // This filed gets added at the receiving end of the transmission.
    public String srcMAC;

    /**
     * No param constructor.
     */
    public BtMsg(){
        // Fields might be assigned directly at a later time if needed be.
    }

    /**
     * Courtesy constructor (fields are public) to initialise fields.
     * @param type - Type of the message. Must be one of static fields of this class. Used in
     *             switching block upon reception.
     * @param payload - Arbitrary serializable object. Must be cast back upon reception.
     */
    public BtMsg(int type, Object payload){
        this.type = type;
        this.payload = payload;
    }

}
