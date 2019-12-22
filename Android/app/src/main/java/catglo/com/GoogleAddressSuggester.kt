package catglo.com


//import com.google.android.libraries.places.compat.*

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliveryDatabase.DataBase
import catglo.com.deliverydroid.data.MyGeoPoint
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*


/**
 * Created by goblets on 2/27/14.
 */
open class GoogleAddressSuggester(protected val context: Context, var resultListener: AddressResultListener?) {

    private var placesClient: PlacesClient

    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    interface AddressResultListener {
        fun commit(addressList: ArrayList<AddressInfo>, searchString: String)
    }

    init {
        // Initialize the SDK
        Places.initialize(context, "AIzaSyCCob6zBTLAuKg1uUF8CfEAdo24lmSkZRw");

        // Create a new Places client instance
        placesClient = Places.createClient(context);
    }



    val dataBase = DataBase(context)
    //private var geocoder: Geocoder = Geocoder(context)
  //  private var geoDataClient: GeoDataClient = Places.getGeoDataClient(context)
  //  private var placesDetectionClient : PlaceDetectionClient = Places.getPlaceDetectionClient(context)
//    GeoDataClient.getAutocompletePredictions()


//    private inner class AutocompletePredictor(var query: String) {
//
//        fun getPredictions(
//            bounds: LatLngBounds,
//            typeFilter: AutocompleteFilter
//        ): Task<AutocompletePredictionBufferResponse> {
//            return geoDataClient.getAutocompletePredictions(query, bounds, typeFilter)
//        }
//    }


    private var lastQuery: String = ""

    fun completeLookup(searchAddress: String, locationManager : LocationManager, location: Location) {

        //Try this to see if we can get the address from local history and return that if we need
//        dataBase.getAddressInfoForString()

        val range = 0.2f //sharedPreferences.getFloat("dileveryRadius", 1f) //TODO: Make configurable!!
        val addressInfoList = ArrayList<AddressInfo>()

   //     var bounds = LatLngBounds(LatLng((location.latitude - range),(location.longitude - range)),
   //         LatLng((location.latitude + range),(location.longitude + range)))

        lastQuery = searchAddress



        var autocompteteRequest = FindAutocompletePredictionsRequest.builder()
            .setLocationRestriction(RectangularBounds.newInstance(
                LatLng((location.latitude - range) , (location.longitude - range)),
                LatLng((location.latitude + range) , (location.longitude + range))))
            .setQuery(searchAddress)
            .build()

        // Submit the query to the autocomplete API and retrieve a PendingResult that will contain the results when the query completes.
//        val typeFilter = AutocompleteFilter.Builder().setTypeFilter( AutocompleteFilter.TYPE_FILTER_ADDRESS or AutocompleteFilter.TYPE_FILTER_GEOCODE).build()
//        val predictor = AutocompletePredictor(searchAddress)
//        val results = predictor.getPredictions(bounds, typeFilter)
//
//        results.addOnSuccessListener { autocompletePredictions ->
//            resultListener?.let {
//                autocompletePredictions.forEach {prediction ->
//                    val addressString = prediction.getFullText(null)
//                    addressInfoList.add(AddressInfo(addressString.toString()))
//                }
//                it.commit(addressInfoList, searchAddress)
//            }
//            autocompletePredictions.release()
//        }
        var completions = 0
        var expected = 0
        placesClient.findAutocompletePredictions(autocompteteRequest)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                resultListener?.let {
                    expected = response.autocompletePredictions.size
                    for (prediction in response.autocompletePredictions) {
                        Log.i("TAG", prediction.placeId)
                        Log.i("TAG", prediction.getPrimaryText(null).toString())

                        val placeId = prediction.placeId

                        // Specify the fields to return.
                        //List<Place.Field>
                        val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG )

                        // Construct a request object, passing the place ID and fields array
                        //FetchPlaceRequest
                        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

                        placesClient.fetchPlace(request).addOnSuccessListener { fetchPlaceResponse ->

                            val addressString = prediction.getFullText(null)
                            addressInfoList.add(
                                AddressInfo(prediction.getFullText(null).toString(),
                                    fetchPlaceResponse.place.latLng?.let{ latLng -> MyGeoPoint(latLng.latitude,latLng.longitude )} ))
                            completions++
                            if (completions >= expected) { it.commit(addressInfoList, searchAddress) }

                        }.addOnFailureListener { ex ->
                            Log.e("places","Place not found")
                            val addressString = prediction.getFullText(null)
                            addressInfoList.add( AddressInfo(prediction.getFullText(null).toString()))
                            completions++
                            if (completions >= expected) { it.commit(addressInfoList, searchAddress) }
                        }
                    }

                }

            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e("TAG", "Place not found: " + exception.statusCode)
                }
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
