package catglo.com.deliverydroid.settings

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import catglo.com.GoogleAddressSuggester
import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliverydroid.DeliveryDroidMapRenderTheme
import catglo.com.deliverydroid.DownloadedMap
import catglo.com.deliverydroid.DownloadedMap.Companion.getMapForCurrentLocation
import catglo.com.deliverydroid.MapReadyListener
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.data.MyGeoPoint
import catglo.com.deliverydroid.widgets.AddressHistoryAutocomplete
import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.MapPosition
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidPreferences
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.reader.MapFile
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import catglo.com.deliverydroid.Utils;

class SettingsStoreAddressActivity : Activity(), TextWatcher, LocationListener,
    GoogleAddressSuggester.AddressListListener {

    internal var LOOKUPABLE_ADDRESS_STRING = Pattern.compile("\\w+\\s+\\w{3,100}")
    internal var GPS_COORDINATES_STRING = Pattern.compile("[0-9]+\\.[0-9]+\\,[0-9]+\\.[0-9]+")

    private var progressDialog: ProgressDialog? = null
    private var autocomplete: AddressHistoryAutocomplete? = null
    private var cancel: Button? = null
    private var save: Button? = null
    private var lookUpGps: Button? = null
    internal var selectedAddress: AddressInfo? = null
    private var mapView: MapView? = null
    private var sharedPreferences: SharedPreferences? = null
    private var locationAccuracyInMeters: Float = 0.toFloat()

    val key: String
        get() = "storeAddress"

    internal var storeAddress: String? = null
    internal var storeAddressLat: Double = 0.toDouble()
    internal var storeAddressLng: Double = 0.toDouble()
    private var locationManager: LocationManager? = null
    private var currentLatitude: Double = 0.toDouble()
    private var currentLongitude: Double = 0.toDouble()

    internal var tileCaches: MutableList<TileCache> = ArrayList()

    private lateinit var addressSuggester: GoogleAddressSuggester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_store_address_activity)

        addressSuggester = GoogleAddressSuggester(this, this);

        autocomplete = findViewById<View>(R.id.autocomplete) as AddressHistoryAutocomplete
        save = findViewById<View>(R.id.save) as Button
        cancel = findViewById<View>(R.id.cancel) as Button
        lookUpGps = findViewById<View>(R.id.look_up_store_address) as Button
        mapView = findViewById<View>(R.id.mapview) as MapView

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        storeAddress = sharedPreferences!!.getString(key, "")
        storeAddressLat = sharedPreferences!!.getInt(key + "Lat", 0).toDouble() / 1e6
        storeAddressLng = sharedPreferences!!.getInt(key + "Lng", 0).toDouble() / 1e6

        autocomplete!!.setSelectAllOnFocus(true)
        autocomplete!!.setText(storeAddress)
        save!!.setOnClickListener {
            Utils.appendLog("\nSaving Store Address $storeAddressLat,$storeAddressLng")


            if (storeAddressLat == 0.0 || storeAddressLng == 0.0 || storeAddressLat > 360 || storeAddressLng > 360) {

                AlertDialog.Builder(this@SettingsStoreAddressActivity)
                    .setTitle(R.string.something_went_wrong)
                    .setMessage(R.string.failed_to_get_good_gps)
                    .setPositiveButton("Try Again") { dialog, which ->
                        //Do nothing
                        Utils.appendLog(" Selected Dialog Try Again ")
                    }
                    .setNegativeButton("Ignore") { dialog, which ->
                        Utils.appendLog(" Selected Dialog Ignore ")

                        saveAndExit()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            } else {
                saveAndExit()
            }
        }
        cancel!!.setOnClickListener { finish() }
        lookUpGps!!.setOnClickListener {
            Utils.appendLog("\nLooking up GPS coordinates")

            if (locationAccuracyInMeters == 0f) {
                synchronized(this@SettingsStoreAddressActivity) {
                    progressDialog = ProgressDialog(this@SettingsStoreAddressActivity)
                    progressDialog!!.setMessage("Please wait...")
                    progressDialog!!.setCancelable(true)
                    progressDialog!!.setOnCancelListener {
                        synchronized(this@SettingsStoreAddressActivity) {
                            Utils.appendLog("    Dialog cancled")
                            progressDialog = null
                        }
                    }
                    progressDialog!!.show()
                }

            } else {
                useCurrentCoordinates()
            }
        }
        autocomplete!!.onItemClickListener =
                OnItemClickListener { arg0, arg1, arg2, arg3 -> centerMapToAddress(autocomplete!!.selectedAddress) }
        autocomplete!!.addTextChangedListener(this)
        autocomplete!!.startSuggestor()
    }

    protected fun useCurrentCoordinates() {
        storeAddressLat = currentLatitude.toFloat().toDouble()
        storeAddressLng = currentLongitude.toFloat().toDouble()
        storeAddress = "$storeAddressLat,$storeAddressLng"
        autocomplete!!.setText(storeAddress)
        Toast.makeText(
            applicationContext,
            getString(R.string.Accutate_to) + " " + locationAccuracyInMeters + " " + getString(R.string.meters),
            Toast.LENGTH_LONG
        ).show()
        centerMapAndSetStoreAddressOverlay()
    }

    internal fun saveAndExit() {
        //TODO: save preference but check for good lookup
        val prefEditor = sharedPreferences!!.edit()
        val addressString = autocomplete!!.text.toString()
        prefEditor.putString(key, addressString)
        prefEditor.putInt(key + "Lat", (storeAddressLat * 1e6).toInt())
        prefEditor.putInt(key + "Lng", (storeAddressLng * 1e6).toInt())
        prefEditor.putString("centrPoint_lat_s", "" + storeAddressLat)
        prefEditor.putString("centrPoint_lng_s", "" + storeAddressLng)
        prefEditor.apply()
        finish()
        Utils.appendLog("Saved Store Address $storeAddressLat,$storeAddressLng")


    }


    @SuppressLint("MissingPermission")
    public override fun onResume() {
        super.onResume()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)
        val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            currentLatitude = lastKnownLocation.latitude
            currentLongitude = lastKnownLocation.longitude
        } else {
            currentLatitude = 0.0
            currentLongitude = 0.0
        }
        centerMapAndSetStoreAddressOverlay()
    }

    public override fun onPause() {
        super.onPause()
        locationManager!!.removeUpdates(this)
    }

    override fun afterTextChanged(editable: Editable) {
        val enteredAddress = editable.toString()
        addressSuggester.lookup(enteredAddress)

    }

    override fun commit(addressList: ArrayList<AddressInfo>, searchString: String) {
        if (addressList.size >0){
//            Utils.appendLog("Got geocoded "+searchString);
            val geocoder = Geocoder(this)
            try {
                val lm = getSystemService(Activity.LOCATION_SERVICE) as LocationManager
                if (lm != null) {
                    @SuppressLint("MissingPermission") val location =
                        lm.getLastKnownLocation(lm.getBestProvider(Criteria(), false))


                    val geocoded = geocoder.getFromLocationName(
                        addressList[0].address,
                        1,
                        location.latitude - 0.1,
                        location.longitude - 0.1,
                        location.latitude + 0.1,
                        location.longitude + 0.1
                    )
                    if (geocoded.size > 0) {
                        val result = geocoded[0]
                        addressList[0].location = MyGeoPoint(result.latitude,result.longitude)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            centerMapToAddress(addressList[0]);
        } else {
            Utils.appendLog("Empty address list for geocode attempt");
        }
    }



    internal fun centerMapToAddress(addressInfo: AddressInfo?) {
        if (addressInfo?.location != null) {
            storeAddress = addressInfo.address
            storeAddressLat = addressInfo.location!!.lat
            storeAddressLng = addressInfo.location!!.lng
            centerMapAndSetStoreAddressOverlay()
        } else {
            Utils.appendLog("NULL ADDRESS INFO")
        }
    }

    private fun viewToBitmap(c:Context, view:View ) : Bitmap {
        view.measure( View.MeasureSpec.getSize(view.measuredWidth),View.MeasureSpec.getSize(view.measuredHeight));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        var drawable = BitmapDrawable(c.resources,android.graphics.Bitmap.createBitmap(view.drawingCache))
        view.isDrawingCacheEnabled = false
        return AndroidGraphicFactory.convertToBitmap(drawable);
    }

    private fun setBackground(view:View , background: Drawable ) {
        view.background = background
    }

    private fun centerMapAndSetStoreAddressOverlay() {

        getMapForCurrentLocation(this, object : MapReadyListener {
            override fun onMapReady(map: DownloadedMap) {
                mapView?.let {
                    val mapFile = MapFile(map.mapFile!!)

                    val zoom = sharedPreferences!!.getInt("mapZoomLevel", 16)

                    val preferencesFacade =
                        AndroidPreferences(getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE))
                    it.model.init(preferencesFacade)
                    it.isClickable = true
                    it.mapScaleBar.isVisible = true
                    it.setBuiltInZoomControls(true)


                    tileCaches.add(
                        AndroidUtil.createTileCache(
                            this@SettingsStoreAddressActivity, "AA",
                            it.model.displayModel.tileSize, 1.0f,
                            it.model.frameBufferModel.overdrawFactor
                        )
                    )

                    val tileRendererLayer = AndroidUtil.createTileRendererLayer(
                        tileCaches[0],
                        mapView!!.model.mapViewPosition, mapFile, DeliveryDroidMapRenderTheme.OSMARENDER, false, true, false
                    )
                    it.layerManager.layers.add(tileRendererLayer)


                    val counter = 1
                    val minLat = java.lang.Float.MAX_VALUE
                    val maxLat = java.lang.Float.MIN_VALUE
                    val minLng = java.lang.Float.MAX_VALUE
                    val maxLng = java.lang.Float.MIN_VALUE

                    if (storeAddressLat!=0.0 && storeAddressLng!=0.0)
                    {
                        val bubbleView = TextView(this@SettingsStoreAddressActivity);
                        setBackground(bubbleView, getDrawable(R.drawable.bubble))
                        bubbleView.gravity = Gravity.CENTER;
                        bubbleView.maxEms = 20;
                        bubbleView.setTextSize(15f);
                        bubbleView.text = storeAddress ?: "";
                        var bubble = viewToBitmap(this@SettingsStoreAddressActivity, bubbleView);
                        bubble.incrementRefCount();
                        it.layerManager.layers.add(Marker(
                            LatLong(storeAddressLat, storeAddressLng),
                            bubble,
                            0,
                            -bubble.height / 2))
/*
                        val bitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.map_home_icon))
                        bitmap.incrementRefCount();
                        val marker = Marker( LatLong(storeAddressLat, storeAddressLng), bitmap, 0, -bitmap.getHeight() / 2)
                        mapView?.layerManager?.layers?.add(marker)*/
                        val mvp = mapView!!.model.mapViewPosition
                        mvp.mapPosition = MapPosition(
                            LatLong(storeAddressLat , storeAddressLng),
                            zoom.toByte()
                        )

                    } else {

                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                        if (locationManager != null) {
                            val criteria = Criteria()
                            val bestProvider = locationManager.getBestProvider(criteria, false)
                            @SuppressLint("MissingPermission") val location =
                                locationManager.getLastKnownLocation(bestProvider)
                            val mvp = mapView!!.model.mapViewPosition
                            mvp.mapPosition = MapPosition(LatLong(location.latitude, location.longitude), zoom.toByte())
                        }
                    }

                    mapView!!.repaint()
                }

            }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }


    override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

    @Synchronized
    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        locationAccuracyInMeters = location.accuracy
        if (progressDialog != null && locationAccuracyInMeters < 100) {
            useCurrentCoordinates()
            progressDialog!!.dismiss()
            progressDialog = null
            Utils.appendLog("   dismiss dialog because we got the data we needed")
        }
        Utils.appendLog("   onLocationChanged $currentLatitude,$currentLongitude")
        Log.i("MAP", "Location changed accurate to meters $locationAccuracyInMeters")
    }

    override fun onProviderDisabled(arg0: String) {
        lookUpGps!!.isEnabled = false
    }

    override fun onProviderEnabled(arg0: String) {
        lookUpGps!!.isEnabled = true
    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {

    }
}
