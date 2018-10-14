package catglo.com.deliverydroid.outTheDoor;



import android.support.v4.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.homeScreen.HomeScreenActivity;

import java.util.LinkedList;
import java.util.Locale;

//import com.catglo.deliverydroid.neworder.NewOrderTimeFragment;

public class OutTheDoorActivity extends DeliveryDroidBaseActionBarActivity
        implements ActionBar.TabListener,LocationListener {

    private final LinkedList<Order> orders = new LinkedList<Order>();
    private int orderCount = 0;
    int orderCounter = 0;
    boolean startNewRun = false;
    private Cursor data;
    public View doneButton;

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    public Order order = new Order();
    private View tabletPane;
    private LocationManager locationManager;
    public double latitude;
    public double longitude;


    public void updateDoneButtonState(Order updatedOrder){

        if (isFinishing()) return;
        boolean isUnpaid=false;
        for (Order order : orders){
            if (updatedOrder!=null && order.id==updatedOrder.id){
                order.payed = updatedOrder.payed;
                order.paymentType = updatedOrder.paymentType;
                order.paymentType2 = updatedOrder.paymentType2;
                order.payed2 = updatedOrder.payed2;
            }
            if (order.payed == Order.NOT_PAID) isUnpaid=true;
        }
        if (isUnpaid){
            doneButton.setVisibility(View.GONE);
        }else {
            doneButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_new_order_acticity);



        Intent intent = getIntent();
        startNewRun = intent.getBooleanExtra("startNewRun", false);

        data = dataBase.getUndeliveredOrders();
        if (data != null) {
            if (data.moveToFirst()) {
                orderCount = 1;
                orders.add(new Order(data));
                while (data.moveToNext() && orderCount < 20) {
                    orders.add(new Order(data));
                    orderCount++;
                }
            }
            data.close();
        }
        if (orders.size() == 0) {
            // This happens upon restart from icon
            Intent i = new Intent(getApplicationContext(), HomeScreenActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(i);
            finish();
            return;
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);



        View v = View.inflate(getApplicationContext(),R.layout.done_button,null);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT;
        v.setLayoutParams(lp);
        actionBar.setCustomView(v);
        doneButton = v.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(300);
                startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        updateDoneButtonState(null);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }



        //gpsNotesButton = view.findViewById(R.id.gpsNotesButton);
        //gpsNotesButton.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        startActivity(new Intent(getActivity(), GpsNotes.class));
        //    }
        //});

        //callThemButton =  view.findViewById(R.id.callClickable);
        //smsThemButton = view.findViewById(R.id.textMessageClickable);


//        callStore.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//                if (activity==null || activity.isFinishing()) return;
//
//                String phoneNumber = activity.sharedPreferences.getString("storePhoneNumber", "");
//                if (phoneNumber.length() < 1) {
//                    Toast.makeText(activity, R.string.missing_phone_number, Toast.LENGTH_LONG).show();
//                } else {
//                    String uri = "tel:" + phoneNumber;
//                    Intent intent = new Intent(Intent.ACTION_DIAL);
//                    intent.setData(Uri.parse(uri));
//                    startActivity(intent);
//                }
//            }
//        });


//        if (sharedPreferences.getBoolean("disableGPS", false)) {
//            navigate.setVisibility(View.GONE);
//            mapIt.setVisibility(View.GONE);
//        } else {
//            navigate.setOnClickListener(new View.OnClickListener() {
//                public void onClick(final View v) {
//                    OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//                    if (activity==null || activity.isFinishing()) return;
//
//                    String addressToNavTo = order.address;
//                    activity.tools.navigateTo(addressToNavTo, activity);
//                }
//            });
//            if (mapIt != null) {
//                mapIt.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(final View v) {
//                        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//                        if (activity==null || activity.isFinishing()) return;
//
//                        String addressToNavTo = order.address;
//                        activity.tools.mapTo(addressToNavTo,activity);
//                    }
//                });
//            }
//        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.out_the_door, menu);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.gpsNotesButton:
                startActivity(new Intent(getApplicationContext(), GpsNotes.class));
            return true;

            case R.id.callStore:
                String phoneNumber = sharedPreferences.getString("storePhoneNumber", "");
                if (phoneNumber.length() < 1) {
                    //TODO: launch settings to get the store phone number
                    Toast.makeText(getApplicationContext(), R.string.missing_phone_number, Toast.LENGTH_LONG).show();
                } else {
                    String uri = "tel:" + phoneNumber;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }




    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/viewPagerPages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            Order order = OutTheDoorActivity.this.orders.get(position);
            return OutTheDoorFragment.create(order);
        }

        @Override
        public int getCount() {
            return OutTheDoorActivity.this.orders.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            Order order = OutTheDoorActivity.this.orders.get(position);
            return order.address.toUpperCase(l);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(HomeScreenActivity.DELIVERY_NOTIFICATION);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation!=null){
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
        } else {
            latitude=0;
            longitude=0;
        }
    }

    @Override
    protected void onPause() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (orderCounter < orderCount) {
            Notification.Builder mBuilder = new Notification.Builder(this).setSmallIcon(R.drawable.icon).setContentTitle("Pending Deliveries").setContentText(orders.get(orderCounter).address);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(OutTheDoorActivity.class);
            stackBuilder.addNextIntent(new Intent(this, OutTheDoorActivity.class));
            mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
            mNotificationManager.notify(HomeScreenActivity.DELIVERY_NOTIFICATION, mBuilder.build());
        } else {
            mNotificationManager.cancel(HomeScreenActivity.DELIVERY_NOTIFICATION);
        }
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
