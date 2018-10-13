package catglo.com.deliverydroid.outTheDoor;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.NotedObject;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
* Created by goblets on 6/14/14.
*/
class GpsNotesListAdapter implements ListAdapter {
    private final Context context;
    private double latitude;
    private double longitude;


    public GpsNotesListAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    public int getCount() {
        return notes.size();
    }

    public Object getItem(int position) {
        return  notes.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public int getItemViewType(int position) {
        return 0;
    }


    ArrayList<NotedObject> notes = new ArrayList<NotedObject>();
    public boolean refresh(DataBase dataBase,double latitude, double longitude, boolean loadOrders){
        notes = dataBase.getNotes(latitude, longitude, loadOrders);
        this.latitude = latitude;
        this.longitude = longitude;
        return (notes.size()>0);
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            view = View.inflate(context, R.layout.gps_notes_list_item, null);
        }
        TextView note     = (TextView)view.findViewById(R.id.note);
        TextView time     = (TextView)view.findViewById(R.id.dateTime);
        TextView distance = (TextView)view.findViewById(R.id.distance);
        ImageView icon    = (ImageView)view.findViewById(R.id.imageView1);

        NotedObject item = notes.get(position);
        note.setText(item.notes);

        java.text.DateFormat df = DateFormat.getDateFormat(context);
        java.text.DateFormat tf = DateFormat.getTimeFormat(context);

        time.setText(df.format(item.getTime()) +" "+tf.format(item.getTime()));
        if (item instanceof Order){
            Order o = (Order)item;
            distance.setText(o.address);
            icon.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_mapmode));
        } else {
            double x1 = item.getLat();
            double x2 = latitude;
            double y1 = item.getLng();
            double y2 = longitude;
            double dist = Math.sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2)));

            double killometers = dist * 110.54;
            double meters = killometers * 1000;
            DecimalFormat formatter = new DecimalFormat("#");
            distance.setText(""+formatter.format(meters)+"m");
            icon.setImageDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_compass));
        }
        return view;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isEmpty() {
        return notes.size()==0;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }
}
