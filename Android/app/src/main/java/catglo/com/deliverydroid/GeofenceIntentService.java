package catglo.com.deliverydroid;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Wage;
import com.google.android.gms.location.*;
import com.google.android.gms.location.Geofence.Builder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class GeofenceIntentService extends IntentService implements LocationListener, OnSuccessListener, OnFailureListener {
    float latitude;
    float longitude;
    private Timer timer;
    public static boolean isRunning = false;
    LocationRequest locationrequest;
    ArrayList<Geofence> listOfGeofences;
    DataBase dataBase = null;
    private GeofencingClient locationClient = LocationServices.getGeofencingClient(this);

    public GeofenceIntentService() {
        super("GeofenceIntentService");
        timer = new Timer("GeofenceIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = new DataBase(getApplicationContext());
        dataBase.open();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dataBase != null) dataBase.close();
        dataBase = null;
    }

    @Override
    protected synchronized void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            //     String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //           geofencingEvent.getErrorCode());
            Log.e("DB", "errorMessage");
            return;
        }

        // Get the transition type.
        int transitionType = geofencingEvent.getGeofenceTransition();

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPreferences.getBoolean("pay_rate_location_aware", false)) {

                Wage wage = dataBase.currentWage();
                float inStoreRate = Float.parseFloat(sharedPreferences.getString("hourly_rate", "0"));
                DecimalFormat df = new DecimalFormat("#.##");
                if (df.format(wage.wage).equalsIgnoreCase(df.format(inStoreRate)) == false) {
                    dataBase.setWage(inStoreRate, dataBase.getShift(DataBase.TodaysShiftCount), DateTime.now());
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{0, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600}, -1);
                }
            }
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {

            Log.e("GEOFENCE", "transitionType == Geofence.GEOFENCE_TRANSITION_EXIT");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPreferences.getBoolean("pay_rate_location_aware", false)) {

                Wage wage = dataBase.currentWage();
                float onRoadRate = Float.parseFloat(sharedPreferences.getString("hourly_rate_on_road", "0"));
                DecimalFormat df = new DecimalFormat("#.##");
                if (df.format(wage.wage).equalsIgnoreCase(df.format(onRoadRate)) == false) {
                    dataBase.setWage(onRoadRate, dataBase.getShift(DataBase.TodaysShiftCount), DateTime.now());
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{0, 300, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 100, 300, 100}, -1);
                }
            }


        }
      /*
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            //Log.i(TAG, geofenceTransitionDetails);

        } else {
            // Log the error.
            //  Log.e("BS", getString(R.string.geofence_transition_invalid_type,
            //        geofenceTransition));
        }
*/
      /*  Log.i("GEOFENCE","onHandleIntent");

        if (intent.getBooleanExtra("init_store_geofence", false)){
            initiateLocationMonitoring();
        }

        else
        if (LocationClient.hasError(intent)) {

            int errorCode = LocationClient.getErrorCode(intent);
            Log.e("GEOFENCE", "Location Services error: " + Integer.toString(errorCode));

        }
        else {
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);

            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){

                Log.e("GEOFENCE",  "transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)" );

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.getBoolean("pay_rate_location_aware",false)){

                    Wage wage = dataBase.currentWage();
                    float inStoreRate = Float.parseFloat(sharedPreferences.getString("hourly_rate", "0"));
                    DecimalFormat df = new DecimalFormat("#.##");
                    if (df.format(wage.wage).equalsIgnoreCase(df.format(inStoreRate))==false){
                        dataBase.setWage(inStoreRate, dataBase.getShift(DataBase.TodaysShiftCount), DateTime.now());
                        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[] {0, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600, 100, 100, 600},-1);
                    }
                }
            }
            else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {

                Log.e("GEOFENCE","transitionType == Geofence.GEOFENCE_TRANSITION_EXIT" );

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.getBoolean("pay_rate_location_aware",false)){

                    Wage wage = dataBase.currentWage();
                    float onRoadRate = Float.parseFloat(sharedPreferences.getString("hourly_rate_on_road", "0"));
                    DecimalFormat df = new DecimalFormat("#.##");
                    if (df.format(wage.wage).equalsIgnoreCase(df.format(onRoadRate))==false){
                        dataBase.setWage(onRoadRate, dataBase.getShift(DataBase.TodaysShiftCount), DateTime.now());
                        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{0, 300, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 300, 100, 100, 300, 100},-1);
                    }
                }



            } else {



            }
            // An invalid transition was reported
            */
        //   }
    }

    PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(getApplicationContext(), GeofenceIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        GeofencingRequest.Builder geofenceRequestBuilder = new GeofencingRequest.Builder();
        geofenceRequestBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        Builder builder = new Geofence.Builder();
        builder.setRequestId("near_store_geofence");
        builder.setCircularRegion(latitude, longitude, 50);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        geofenceRequestBuilder.addGeofence(builder.build());

        builder = new Builder();
        builder.setRequestId("leave_store_geofence");
        builder.setCircularRegion(latitude, longitude, 50);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        geofenceRequestBuilder.addGeofence(builder.build());

        //   locationClient.addGeofences(listOfGeofences,pendingIntent, GeofenceIntentService.this);


        locationClient.addGeofences(geofenceRequestBuilder.build(), pendingIntent);

        return pendingIntent;


/*        locationrequest = LocationRequest.create();
        locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationrequest.setInterval(5000);
        locationrequest.setExpirationDuration(stayOnMillis);
        locationClient.requestLocationUpdates(locationrequest, GeofenceIntentService.this);*/


    }

    private synchronized void stopLocationMonitoring() {
//        locationClient.removeGeofences()
       /* Log.i("GEOFENCE","stopLocationMonitoring ");
        isRunning = false;
        locationrequest.setExpirationDuration(0);
        locationClient.removeLocationUpdates(this);
        ArrayList<String> geofenceRequstIds = new ArrayList<String>();
        for (Geofence g : listOfGeofences){
            geofenceRequstIds.add(g.getRequestId());
        }
        locationClient.removeGeofences(geofenceRequstIds,this);*/

    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }

    @Override
    public void onSuccess(Object o) {

    }

    private synchronized void initiateLocationMonitoring() {
        if (isRunning) return;
        isRunning = true;


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        latitude = sharedPreferences.getFloat("storeAddress_lat", -1);
        longitude = sharedPreferences.getFloat("storeAddress_lng", -1);
        String storeAddress = sharedPreferences.getString("storeAddress", null);
        String stayOnHoursString = sharedPreferences.getString("location_aware_stay_on_hours", "2");
        float stayOnHours;
        try {
            stayOnHours = Float.parseFloat(stayOnHoursString);
        } catch (NumberFormatException e) {
            stayOnHours = 2;
        }
        final int stayOnMillis = (int) (1000 * 60 * 60 * stayOnHours);


        final Runnable next = new Runnable() {
            public void run() {
                listOfGeofences = new ArrayList<Geofence>();

                Builder builder = new Builder();
                builder.setRequestId("near_store_geofence");
                builder.setCircularRegion(latitude, longitude, 50);
                builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
                builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
                listOfGeofences.add(builder.build());

                builder = new Builder();
                builder.setRequestId("leave_store_geofence");
                builder.setCircularRegion(latitude, longitude, 50);
                builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);
                builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
                listOfGeofences.add(builder.build());
            }
        };
    }


}

         /*   final GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks(){

                @Override
                public void onConnected(Bundle connectionHint) {
                    Log.i("GEOFENCE","onConnected");

                    Intent intent = new Intent(getApplicationContext(),  GeofenceIntentService.class);
                    PendingIntent pendingIntent =  PendingIntent.getService( getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    locationClient.addGeofences(listOfGeofences,pendingIntent, GeofenceIntentService.this);

                    locationrequest = LocationRequest.create();
                    locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationrequest.setInterval(5000);
                    locationrequest.setExpirationDuration(stayOnMillis);
                    locationClient.requestLocationUpdates(locationrequest, GeofenceIntentService.this);

                    timer.schedule(new TimerTask(){public void run() {
                        stopLocationMonitoring();
                    }}, stayOnMillis);


                }

                @Override
                public void onDisconnected() {
                    Log.i("GEOFENCE","onDisconnected");


                }
            };


            GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener(){

                @Override
                public void onConnectionFailed(ConnectionResult result) {
                    Log.i("GEOFENCE","onConnectionFailed");

                }
            };
            locationClient = new LocationClient(getApplicationContext(), connectionCallbacks, onConnectionFailedListener);

            locationClient.connect();


        }};
                if (latitude == -1 || longitude == -1) {
            new WebServiceGoogleGeocode(getApplicationContext(),storeAddress,new WebServiceGoogleGeocode.AddressListListener(){
                public void commit(ArrayList<AddressInfo> addressList,String searchString) {
                    if (addressList.size()>0){
                        AddressInfo addressInfo = addressList.get(0);
                        longitude = (float) addressInfo.location.getLng();
                        latitude  = (float) addressInfo.location.getLat();
                        next.run();
                    }
                }
            });
                } else {
                    next.run();
                }

            }
        }
*/