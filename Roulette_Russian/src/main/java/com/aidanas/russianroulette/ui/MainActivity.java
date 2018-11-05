package com.aidanas.russianroulette.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;

/**
 * Class containing the main activity of the app.
 */
public class MainActivity extends AppCompatActivity{

    // Tag, mostly used for logging and debug output.
    public static final String TAG = MainActivity.class.getSimpleName();

    // Request code for the return result, after 'enable Bluetooth' intent was started.
    private static final int REQUEST_ENABLE_BT = 1;

    // Views of the activity.
    private Button mServerBtn;
    private Button mClientBtn;

    // Bluetooth adapter which will be used for communication.
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mServerBtn = (Button) findViewById(R.id.ac_main_server_btn);
        mClientBtn = (Button) findViewById(R.id.ac_main_client_btn);


        // Check if Bluetooth is supported by the device.
        setupBtAdapter();

        View.OnClickListener clickListener;
        mServerBtn.setOnClickListener(clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Const.DEBUG) Log.v(TAG, "In onClick(), Server Btn? = " + (v == mServerBtn));

                enableBtns(false);
                if (v == mServerBtn){
                    startActivity(new Intent(MainActivity.this, PlayingActivityServer.class));
                } else {
                    startActivity(new Intent(MainActivity.this, SelectHostActivity.class));
                }
            }
        });

        mClientBtn.setOnClickListener(clickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Const.DEBUG) Log.v(TAG, "In onStart()");
    }

    /***********************************************************************************************
     *                        Only Android Lifecycle Methods Above
     **********************************************************************************************/

    /**
     * Utility method to enable/disable UI Bluetooth Server/Client buttons.
     * @param b - True to enable, false otherwise.
     */
    private void enableBtns(boolean b) {
        mServerBtn.setEnabled(b);
        mClientBtn.setEnabled(b);
    }

    /**
     * Utility method to check if device has hardware support for Bluetooth and enable it if it is
     * found to be disabled.
     */
    private void setupBtAdapter() {
        if (Const.DEBUG) Log.v(TAG, "in setupBtAdapter()");

        /*
         * Check if the device has a Bluetooth adapter.
         */
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does NOT support Bluetooth.
            if (Const.DEBUG) Log.v(TAG, "No Bluetooth adapter found!");
            showNoBluetoothDlg();
        } else {
            // Device HAS support for Bluetooth.
            if (Const.DEBUG) Log.v(TAG, "Bluetooth adapter found! adapter = " +
                    bluetoothAdapter.getName());
            /*
             * Check if bluetooth is enabled. If not then prompt the user to enable it.
             */
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    /**
     * Method to construct and display a message informing the user that the device does not support
     * Bluetooth.
     */
    private void showNoBluetoothDlg() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("No Bluetooth!");
        alertDialog.setMessage(getString(R.string.no_bt_dialog_msg));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /***********************************************************************************************
     *                                  Inner Classes
     **********************************************************************************************/
}
