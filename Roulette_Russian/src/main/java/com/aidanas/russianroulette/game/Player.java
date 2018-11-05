package com.aidanas.russianroulette.game;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to model a single player in the game. It will be used by the Arbitrator. The player is
 * modeled as FSM with the four distinct states specified as enum values.
 */
public class Player implements Serializable{

    // Must be unique among players.
    private final String mName;

    private final String mAddress;

    private State mState = State.RESET;

    /**
     * Constructor
     * @param name - Name of the player.
     */
    public Player(String name, String address){
        mName = name;
        mAddress = address;
    }

    /***********************************************************************************************
     *                          Getters and Setters
     **********************************************************************************************/

    public String getName() {
        return mName;
    }

    public String getAddress(){
        return mAddress;
    }

    public boolean isReady(){
        return mState == State.READY;
    }

    public boolean isAlive() {
        return mState == State.ALIVE;
    }

    public void setReady(){
        if (!(mState == State.RESET)){
            throw new IllegalStateException("Players can transition to READY state only from " +
                    "READY state!");
        }
        mState = State.READY;
    }

    public void setAlive() {
        if (!(mState == State.READY)){
            throw new IllegalStateException("Players can transition to ALIVE state only from " +
                    "READY state!");
        }
        mState = State.ALIVE;
    }

    public void setReset() {
        if (!(mState == State.ALIVE)){
            throw new IllegalStateException("Players can transition to RESET state only from " +
                    "ALIVE state!");
        }
        mState = State.RESET;
    }

    /***********************************************************************************************
     *                          Inner Classes
     **********************************************************************************************/

    /**
     * Custom comparator class to compare two Players in terms of their names.
     */
    public class PlayerComparator implements Comparator<Player>{

        @Override
        public int compare(Player lhs, Player rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    /**
     * Possible states of a player.
     */
    public enum State{
        RESET,
        READY,
        ALIVE,
        DEAD,
    }
}
