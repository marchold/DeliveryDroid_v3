package catglo.com


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.*
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliveryDatabase.DataBase
import com.google.android.gms.location.places.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
import java.util.*


/**
 * Created by goblets on 2/27/14.
 */
open class GoogleAddressSuggester(protected val context: Context, var resultListener: AddressListListener?) {

    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    interface AddressListListener {
        fun commit(addressList: ArrayList<AddressInfo>, searchString: String)
    }

    val dataBase = DataBase(context)
    private var geocoder: Geocoder = Geocoder(context)
    private var geoDataClient: GeoDataClient = Places.getGeoDataClient(context)
    private var placesDetectionClient : PlaceDetectionClient = Places.getPlaceDetectionClient(context)
//    GeoDataClient.getAutocompletePredictions()

    private inner class AutocompletePredictor(var query: String) {

        fun getPredictions(
            bounds: LatLngBounds,
            typeFilter: AutocompleteFilter
        ): Task<AutocompletePredictionBufferResponse> {
            return geoDataClient.getAutocompletePredictions(query, bounds, typeFilter)
        }
    }


    private var lastQuery: String = ""

    fun completeLookup(searchAddress: String, locationManager : LocationManager, location: Location) {

        //Try this to see if we can get the address from local history and return that if we need
//        dataBase.getAddressInfoForString()

        val range = 0.2f //sharedPreferences.getFloat("dileveryRadius", 1f) //TODO: Make configurable!!
        val addressInfoList = ArrayList<AddressInfo>()

        var bounds = LatLngBounds(LatLng((location.latitude - range),(location.longitude - range)),
            LatLng((location.latitude + range),(location.longitude + range)))

        lastQuery = searchAddress

        // Submit the query to the autocomplete API and retrieve a PendingResult that will contain the results when the query completes.
        val typeFilter = AutocompleteFilter.Builder().setTypeFilter( AutocompleteFilter.TYPE_FILTER_ADDRESS or AutocompleteFilter.TYPE_FILTER_GEOCODE).build()
        val predictor = AutocompletePredictor(searchAddress)
        val results = predictor.getPredictions(bounds, typeFilter)

        results.addOnSuccessListener { autocompletePredictions ->
            resultListener?.let {
                autocompletePredictions.forEach {prediction ->
                    val addressString = prediction.getFullText(null)
                    addressInfoList.add(AddressInfo(addressString.toString()))
                }
                it.commit(addressInfoList, searchAddress)
            }
            autocompletePredictions.release()
        }
    }

    @SuppressLint("MissingPermission")
    open fun lookup(searchAddress: String) {

        (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).run {
            val criteria = Criteria()
            val bestProvider = getBestProvider(criteria, false)
            val location = getLastKnownLocation(bestProvider)


            if (location != null) {
                completeLookup(searchAddress,this,location)
            } else {
                val lm = this
                requestSingleUpdate(bestProvider, object:LocationListener{
                    override fun onLocationChanged(location: Location?) {
                        if (location!=null){
                            completeLookup(searchAddress,lm,location)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String?) {
                    }

                    override fun onProviderDisabled(provider: String?) {
                    }
                }, Looper.getMainLooper())
            }
        }
    }

}
