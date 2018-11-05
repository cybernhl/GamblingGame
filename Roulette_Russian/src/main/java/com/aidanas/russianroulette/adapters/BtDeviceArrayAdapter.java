package com.aidanas.russianroulette.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;

import java.util.List;

/**
 * Created by Aidanas Tamasauskas
 * Created on 07/02/2016.
 *
 * Custom array adapter to be used with the ListView containing the list of Bluetooth devices.
 */
public class BtDeviceArrayAdapter extends ArrayAdapter <BluetoothDevice>{

    // Tag, mostly used for logging output.
    public static final String TAG = BtDeviceArrayAdapter.class.getSimpleName();

    private final int mRowLayout;

    /**
     * Constructor.
     * @param context - Context its runnint in.
     * @param rowLayout - Layout of the a single row of the ListView.
     * @param devices - List of Bluetooth device objects. To be populated in the list rows.
     */
    public BtDeviceArrayAdapter(Context context, int rowLayout, List<BluetoothDevice> devices){
        super(context, rowLayout, devices);
        mRowLayout = rowLayout;
    }

    /**
     * Holds references of views for quick access.
     */
    static class ViewHolder {
        public TextView nameTw;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (Const.DEBUG) Log.v(TAG, "In getView(), position = " + position);

        View rowView = convertView;

        // If no recycled view is available save view in the ViewHolder.
        if (rowView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(mRowLayout, parent, false);

            // Save the references.
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.nameTw = (TextView) rowView.findViewById(R.id.bt_dev_name_host_sel);
            rowView.setTag(viewHolder);
        }

        // Populate the values into the row.
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.nameTw.setText(getItem(position).getName());

        return rowView;
    }
}
