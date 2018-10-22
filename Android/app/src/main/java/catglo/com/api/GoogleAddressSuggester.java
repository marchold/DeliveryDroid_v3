package catglo.com.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import catglo.com.deliveryDatabase.AddressInfo;
import catglo.com.deliverydroid.BuildConfig;
import catglo.com.deliverydroid.data.MyGeoPoint;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by goblets on 2/27/14.
 */
public class GoogleAddressSuggester {

    private final SharedPreferences sharedPreferences;
    private final String addressFilterComponents;
    private final RequestQueue requestQue;
    protected final Context context;

    public interface AddressListListener {
        void commit(ArrayList<AddressInfo> addressList, String searchString);
    }

    public AddressListListener resultListener;

    JsonObjectRequest request;
    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        //Log.e("REST", error.getLocalizedMessage());
        }
    };

    public GoogleAddressSuggester(Context context, AddressListListener addressListListener) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        addressFilterComponents = sharedPreferences.getString("addressFilterComponents", "");

        this.resultListener = addressListListener;
        requestQue = Volley.newRequestQueue(context);
        this.context = context;
    }

    public void lookup(final String lookupAddress){
        String url;
        String address = null;
        try {
            address = URLEncoder.encode(lookupAddress, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            address = "";
        }

        float range = sharedPreferences.getFloat("dileveryRadius", 1f); //TODO: Make configurable!!

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);

        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {//TODO: MAke a better solution, gps from prefs..
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            String bounds = "&bounds=" + (lat - range) + "," + (lng - range) + URLEncoder.encode("|") + (lat + range) + "," + (lng + range);
            url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + bounds + "&sensor=true&key="+BuildConfig.GoogleMapsApiKey;
        } else {
            url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=true&key="+BuildConfig.GoogleMapsApiKey;
        }

        if (addressFilterComponents.length() > 1) {
            url = url + "&components=" + URLEncoder.encode(addressFilterComponents);
        }

        requestQue.add(new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResponse) {

                try {
                    JSONArray results = jsonResponse.getJSONArray("results");
                    ArrayList<AddressInfo> addresses = new ArrayList<AddressInfo>();

                    for (int index = 0; index < results.length(); index++){
                        JSONObject a1=results.getJSONObject(index);

                        AddressInfo addressInfo = new AddressInfo();
                        addressInfo.address = a1.getString("formatted_address");

                        boolean includedRoute;
                        includedRoute=false;
                        JSONArray address_components = a1.getJSONArray("address_components");
                        for (int i = 0; i < address_components.length(); i++){
                            JSONArray types = address_components.getJSONObject(i).getJSONArray("types");
                            for (int j = 0; j < types.length(); j++){
                                if (types.getString(j).equalsIgnoreCase("route")){
                                    includedRoute=true;
                                }
                            }
                        }
                        if (includedRoute){
                            try {
                                JSONObject geometry = a1.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                float lat = Float.parseFloat(location.getString("lat"));
                                float lng = Float.parseFloat(location.getString("lng"));
                                addressInfo.location = new MyGeoPoint((int)(lat*1e6),(int)(lng*1e6));
                            } catch (NumberFormatException e){};

                            addresses.add(addressInfo);
                        }
                    }

                    resultListener.commit(addresses, lookupAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, errorListener));

    }

}
