package com.aidanas.russianroulette.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aidanas.russianroulette.Const;
import com.aidanas.russianroulette.R;
import com.aidanas.russianroulette.game.Player;

import java.util.List;

/**
 * Created by: Aidanas
 * Created on: 02/05/2016.
 * <p>
 * Custom array adapter to populate a list with Bluetooth device names.
 */
public class PlayersListArrayAdapter extends ArrayAdapter<Player>{

    // Tag, mostly used for logging output.
    public static final String TAG = PlayersListArrayAdapter.class.getSimpleName();

    /**
     * Constructor.
     * @param context - Context the adapter is running in.
     * @param rowLayout - Layout of the a single row of the ListView.
     * @param devices - List of Bluetooth connected sockets objects. To be populated in the list rows.
     */
    public PlayersListArrayAdapter(Context context, int rowLayout, List<Player> devices){
        super(context, rowLayout, devices);
    }

    /**
     * Holds references of views for quick access.
     */
    static class ViewHolder {
        public TextView  nameTw;
        public ImageView readyIv;
        public ImageView aliveIv;
    }

    /**
     * This is called whenever a new list item needs to be created.
     * @param position - Position of the item in the list.
     * @param convertView - View which should be used to create the item.
     * @param parent - Parent of the view.
     * @return - View of newly created list item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (Const.DEBUG) Log.v(TAG, "In getView(), position = " + position);

        View rowView = convertView;

        // If no recycled view is available save view in the ViewHolder.
        if (rowView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.player_list_item, parent, false);

            // Save the references.
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.nameTw  = (TextView) rowView.findViewById(R.id.bt_dev_name);
            viewHolder.readyIv = (ImageView) rowView.findViewById(R.id.list_item_ready_iv);
            viewHolder.aliveIv = (ImageView) rowView.findViewById(R.id.list_item_alive_iv);
            rowView.setTag(viewHolder);
        }

        // Populate the values into the row.
        Player p = getItem(position);
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.nameTw.setText(p.getName());
        viewHolder.readyIv.setVisibility(p.isReady() ? View.VISIBLE : View.GONE);
        viewHolder.aliveIv.setVisibility(p.isAlive() ? View.VISIBLE : View.GONE);

        return rowView;
    }


}
