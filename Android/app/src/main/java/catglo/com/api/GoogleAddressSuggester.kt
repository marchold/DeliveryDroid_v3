package catglo.com.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.*
import android.preference.PreferenceManager

import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliverydroid.BuildConfig
import catglo.com.deliverydroid.data.MyGeoPoint
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley


import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by goblets on 2/27/14.
 */
open class GoogleAddressSuggester(protected val context: Context, var resultListener: AddressListListener) {

    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    interface AddressListListener {
        fun commit(addressList: ArrayList<AddressInfo>, searchString: String)
    }

    private var geocoder: Geocoder = Geocoder(context)

    @SuppressLint("MissingPermission")
    open fun lookup(searchAddress: String) {

        val range = 1f //sharedPreferences.getFloat("dileveryRadius", 1f) //TODO: Make configurable!!

        val addressInfoList = ArrayList<AddressInfo>()
        (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).run {
            val criteria = Criteria()
            val bestProvider = getBestProvider(criteria, false)
            val location = getLastKnownLocation(bestProvider)
            val results : List<Address>
            if (location != null) {
                results = geocoder.getFromLocationName(searchAddress,40,
                    (location.latitude - range),
                    (location.longitude - range),
                    (location.latitude + range),
                    (location.longitude + range))

            } else {
                results = geocoder.getFromLocationName(searchAddress,40)
            }

            results.forEach {
                val addressInfo = AddressInfo()
                addressInfo.address = it.getAddressLine(1)
                addressInfo.location = MyGeoPoint(it.latitude,it.longitude)
                addressInfoList.add(AddressInfo())
                addressInfoList.add(addressInfo)
            }

        }

        resultListener.commit(addressInfoList, searchAddress);


        //if (addressFilterComponents!!.length > 1) {
        //    url = url + "&components=" + URLEncoder.encode(addressFilterComponents)
        //}

        /*


        JsonObjectRequest request = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
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
        }, errorListener);

        requestQue.add(request);*/

    }

}
