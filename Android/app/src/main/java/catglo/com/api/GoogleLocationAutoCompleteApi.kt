package catglo.com.api


import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager

import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliverydroid.BuildConfig
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng


import org.json.JSONException
import org.json.JSONObject

import java.net.URLEncoder
import java.util.ArrayList

interface AddressListListener {
    fun commit(addressList: ArrayList<AddressInfo>, searchString: String)
}


/**
 * Used for looking up addressed in an auto-complete
 */
class GoogleLocationAutoCompleteApi(context: Context)  {

    private val context: Context
    private val networkQue: RequestQueue

    init {
        this.context = context.applicationContext
        networkQue = Volley.newRequestQueue(context)
    }


    interface StringListReceiver {
        fun onResult(results: ArrayList<String>)
    }

    fun getAddressForLocation(latLng: LatLng, resultReciever: StringListReceiver) {
        val url: String
        url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latLng.latitude + "," + latLng.longitude
        doLookup(url, resultReciever)
    }

    fun lookup(searchString: String, resultReciever: StringListReceiver) {
        val url: String
        //The idea here is to get the last known location and use that as a center point for a roughly 8 mile square we
        //feed to google as +- 0.1 lng/lat
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val bestProvider = locationManager.getBestProvider(criteria, false)
        @SuppressLint("MissingPermission") val location = locationManager.getLastKnownLocation(bestProvider)
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            val bounds =
                "&bounds=" + (lat - 0.1) + "," + (lng - 0.1) + URLEncoder.encode("|") + (lat + 0.1) + "," + (lng + 0.1)
            url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(searchString) +
                    bounds + "&sensor=true&"
        } else {
            url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(searchString) +
                    "&sensor=true"
        }
        doLookup(url, resultReciever)
    }

    private fun doLookup(url: String, resultReciever: StringListReceiver) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, object : Response.Listener<JSONObject> {
            override fun onResponse(jsonObject: JSONObject) {
                val resultList = ArrayList<String>()
                try {
                    val results = jsonObject.getJSONArray("results")
                    for (i in 0 until results.length()) {
                        val result = results.get(i) as JSONObject
                        resultList.add(result.getString("formatted_address"))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                resultReciever.onResult(resultList)
            }
        }, Response.ErrorListener { })
        networkQue.add(jsonObjectRequest)
    }


    fun lookup(searchString: String, resultReciever: AddressListListener) {
        val url: String

        //The idea here is to get the last known location and use that as a center point for a roughly 8 mile square we
        //feed to google as +- 0.1 lng/lat
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val bestProvider = locationManager.getBestProvider(criteria, false)
        @SuppressLint("MissingPermission") val location = locationManager.getLastKnownLocation(bestProvider)
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            val bounds =
                "&bounds=" + (lat - 0.1) + "," + (lng - 0.1) + URLEncoder.encode("|") + (lat + 0.1) + "," + (lng + 0.1)
            url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(searchString) +
                    bounds + "&sensor=true&key="+ BuildConfig.GoogleMapsApiKey;
        } else {
            url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(searchString) +
                    "&sensor=true&key="+BuildConfig.GoogleMapsApiKey;
        }

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, object : Response.Listener<JSONObject> {
            override fun onResponse(jsonObject: JSONObject) {
                val resultList = ArrayList<AddressInfo>()
                try {
                    val results = jsonObject.getJSONArray("results")
                    for (i in 0 until results.length()) {
                        val result = results.get(i) as JSONObject
                        val item = AddressInfo()
                        // item.name = result.getString("formatted_address");
                        val geometry = result.getJSONObject("geometry")
                        if (geometry != null) {
                            val location = geometry.getJSONObject("location")
                            if (location != null) {
                                //  item.location = new LatLng(NumberTool.parseFloat(location.getString("lat")),
                                //        NumberTool.parseFloat(location.getString("lng")));
                            }
                        }
                        resultList.add(item)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                resultReciever.commit(resultList,searchString)
            }
        }, Response.ErrorListener { })
        networkQue.add(jsonObjectRequest)
    }
}
