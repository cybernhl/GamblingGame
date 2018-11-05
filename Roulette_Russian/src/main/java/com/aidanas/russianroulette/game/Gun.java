package com.aidanas.russianroulette.game;

import android.util.Log;

import com.aidanas.russianroulette.Const;

import java.security.SecureRandom;

/**
 * Created by: Aidanas
 * Created on: 22/04/2016.
 *
 * Class to model the behavior of a gun in a Russian Roulette game.
 */
public class Gun {

    // Tag, mostly used for logging and debug output.
    public static final String TAG = Gun.class.getSimpleName();

    // How many bullets can this gun hold?
    private final int mCapacity;

    // Number of bullets loaded.
    private int mBulletsLoaded;

    // Random number generator.
    private final SecureRandom srnd = new SecureRandom();

    // Hold the number of the cylinder slot at which the hammer is pointing.
    private int mHammer = 1;

    /**
     * Constructor.
     * @param capacity - Bullet capacity of this gun. Default = 6.
     */
    public Gun(int capacity){
        if (capacity < 1){
            throw new IllegalArgumentException("Capacity must be greater then 0");
        }

        mCapacity = capacity;
    }

    /**
     * Method to load bullets into the gun.
     * @param bullets - number of bullets to load. Must be equal or less than the capacity.
     */
    public void loadBullets(int bullets){
        mBulletsLoaded = bullets;
    }

    /**
     * Method to model the cylinder spin action.
     */
    public void spinCylinder(){
        mHammer = srnd.nextInt(mCapacity-1)+1; //-1+1 to cater for 0 index.
    }

    /**
     * Method to model the trigger pull of the gun.
     * @return - Returns true if the shot was live or false if it was empty.
     */
    public boolean pullTheTrigger() {
        if (Const.DEBUG) Log.v(TAG, "In pullTheTrigger(), hammer@ = " + mHammer +
                ", bullets = " + mBulletsLoaded);

        return mHammer <= mBulletsLoaded;
    }
}
