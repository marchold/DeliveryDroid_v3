package catglo.com.deliverydroid.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;
import catglo.com.deliveryDatabase.AddressInfo;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import catglo.com.widgets.AddressHistoryAutocomplete;
import org.mapsforge.map.android.view.MapView;


public class SettingsStoreAddressActivity extends
		Activity implements TextWatcher, LocationListener {
	Pattern LOOKUPABLE_ADDRESS_STRING = Pattern.compile("\\w+\\s+\\w{3,100}");
	Pattern GPS_COORDINATES_STRING = Pattern.compile("[0-9]+\\.[0-9]+\\,[0-9]+\\.[0-9]+");

	private ProgressDialog progressDialog;
	private AddressHistoryAutocomplete autocomplete;
	private Button cancel;
	private Button save;
	private Button lookUpGps;
	AddressInfo selectedAddress;
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	//private ArrayList<OverlayItem> mOverlays;
	private float locationAccuracyInMeters;

	public String getKey(){
		return "storeAddress";
	}

	String storeAddress;
	double storeAddressLat;
	double storeAddressLng;
	private LocationManager locationManager;
	private double currentLatitude;
	private double currentLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_store_address_activity);

		autocomplete = (AddressHistoryAutocomplete)findViewById(R.id.autocomplete);
		save         = (Button)                    findViewById(R.id.save);
		cancel       = (Button)                    findViewById(R.id.cancel);
		lookUpGps    = (Button)                    findViewById(R.id.look_up_store_address);
		mapView      = (MapView)                   findViewById(R.id.mapview);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		storeAddress    = sharedPreferences.getString(getKey(), "");
		storeAddressLat = ((double)sharedPreferences.getInt(getKey()+"Lat", 0)/1e6);
		storeAddressLng = ((double)sharedPreferences.getInt(getKey()+"Lng", 0)/1e6);

		autocomplete.setSelectAllOnFocus(true);
		autocomplete.setText(storeAddress);
		save.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {

            Tools.appendLog("\nSaving Store Address "+storeAddressLat+","+storeAddressLng);


            if (storeAddressLat == 0 || storeAddressLng == 0 || storeAddressLat > 360 || storeAddressLng > 360)
            {

                new DialogFragment(){
                    public Dialog onCreateDialog(Bundle b){
                        Dialog dialog = new AlertDialog.Builder(SettingsStoreAddressActivity.this)
                                .setTitle("Something went wrong")
                                .setMessage("We failed to get good GPS coordinates for the address. Some features won't work without store GPS coordinates.")
                                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Do nothing
                                        Tools.appendLog(" Selected Dialog Try Again ");
                                    }
                                })
                                .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Tools.appendLog(" Selected Dialog Ignore ");

                                        saveAndExit();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .create();
                        return dialog;
                    }

                }.show(getFragmentManager(),"Confirm bad address");
            }
            else{
                saveAndExit();
            }

		}});
		cancel.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			finish();
		}});
		lookUpGps.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {

            Tools.appendLog("\nLooking up GPS coordinates");

            if (locationAccuracyInMeters==0){
				synchronized(SettingsStoreAddressActivity.this){
					progressDialog = new ProgressDialog(SettingsStoreAddressActivity.this);
					progressDialog.setMessage("Please wait...");
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new OnCancelListener(){public void onCancel(DialogInterface arg0) {
						synchronized(SettingsStoreAddressActivity.this){
                            Tools.appendLog("    Dialog cancled");
                            progressDialog=null;
						}
					}});
					progressDialog.show();
				}

			} else {
				useCurrentCoordinates();
			}
		}});
		autocomplete.setOnItemClickListener(new OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			centerMapToAddress(autocomplete.getSelectedAddress());
		}});
		autocomplete.addTextChangedListener(this);
		autocomplete.startSuggestor();
	}

	protected void useCurrentCoordinates() {
		storeAddressLat = ((float) currentLatitude);
		storeAddressLng = ((float) currentLongitude);
		storeAddress = ""+storeAddressLat+","+storeAddressLng;
		autocomplete.setText(storeAddress);
		Toast.makeText(getApplicationContext(), getString(R.string.Accutate_to)+" "+locationAccuracyInMeters+" "+getString(R.string.meters), Toast.LENGTH_LONG).show();
		centerMapAndSetStoreAddressOverlay();
	}

    void saveAndExit() {
        //TODO: save preference but check for good lookup
        final Editor prefEditor = sharedPreferences.edit();
        String addressString = autocomplete.getText().toString();
        prefEditor.putString(getKey(), addressString);
        prefEditor.putInt(getKey() + "Lat", (int) (storeAddressLat * 1e6));
        prefEditor.putInt(getKey() + "Lng", (int) (storeAddressLng * 1e6));
        prefEditor.putString("centrPoint_lat_s", "" + storeAddressLat);
        prefEditor.putString("centrPoint_lng_s", "" + storeAddressLng);
        prefEditor.commit();
        finish();
        Tools.appendLog("Saved Store Address "+storeAddressLat+","+storeAddressLng);


    }


	@Override
	public void onResume(){
		super.onResume();
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation!=null){
			currentLatitude = lastKnownLocation.getLatitude();
			currentLongitude = lastKnownLocation.getLongitude();
		} else {
			currentLatitude=0;
			currentLongitude=0;
		}
		centerMapAndSetStoreAddressOverlay();
	}

	@Override
	public void onPause(){
		super.onPause();
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void afterTextChanged(Editable editable) {
		/*String enteredAddress = editable.toString();
		Matcher matcher = LOOKUPABLE_ADDRESS_STRING.matcher(enteredAddress);
		if (matcher.find()){
			Log.i("MAP","Looking up info for "+enteredAddress);
			//look up the address and center the map
            new WebServiceGoogleGeocode(getApplicationContext(),enteredAddress,new WebServiceGoogleGeocode.AddressListListener(){
				public void commit(ArrayList<AddressInfo> addressList,String searchString) {
					if (addressList.size()>0){
                        Tools.appendLog("Got geocoded "+searchString);
						centerMapToAddress(addressList.get(0));	
					} else {
                        Tools.appendLog("Empty address list for geocode attempt");
					}
				}
			});
		} else {
            Tools.appendLog("Not enough string for address");
		}*/
	}
	
	void centerMapToAddress(AddressInfo addressInfo){
		if (addressInfo != null){
			storeAddress = addressInfo.getAddress();
			storeAddressLat = addressInfo.getLocation().lat;
			storeAddressLng = addressInfo.getLocation().lng;
			centerMapAndSetStoreAddressOverlay();
		} else {
            Tools.appendLog("NULL ADDRESS INFO");
		}
	}

	void centerMapAndSetStoreAddressOverlay(){

        Tools.appendLog("centerMapAndSetStoreAddressOverlay to "+storeAddressLat+" , "+storeAddressLng);
/*

        //Build the overlays for the map markers
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
	
		GeoPoint geoPoint = new GeoPoint((int)(storeAddressLat*1e6),(int)(storeAddressLng*1e6));
		
		MapOverlay overlay = new MapOverlay(getResources().getDrawable(R.drawable.map_flag),getApplicationContext());
    	OverlayItem overlayitem = new OverlayItem(geoPoint, storeAddress, "snippet");
    	overlay.addOverlay(overlayitem);
    	mapOverlays.add(overlay);
	
    	//Matcher m1 = LOOKUPABLE_ADDRESS_STRING.matcher(storeAddress);
    	//Matcher m2 = GPS_COORDINATES_STRING.matcher(storeAddress);
    	MapController mc = mapView.getController();
		if ((storeAddressLat+storeAddressLng)!=0) {
			mc.setZoom(14);
			Log.i("MAP","Zoom to 14");
			
		} else 
    	{
			Log.i("MAP","Zoom to 1");
			
			mc.setZoom(1);
		}
		mc.animateTo(geoPoint);    */
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	//@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/*class MapOverlay extends ItemizedOverlay<OverlayItem> {
		public MapOverlay(Drawable defaultMarker,Context context) {
			super(boundCenterBottom(defaultMarker));
			mOverlays = new ArrayList<OverlayItem>();
			populate();
		}
		
	    public void removeItem(int i){
	        mOverlays.remove(i);
	        populate();
	    }

		protected OverlayItem createItem(int i) {
			 return mOverlays.get(i);
		}

		public int size() {
			return mOverlays.size();
		}
		
		public void addOverlay(OverlayItem overlay) {
		    mOverlays.add(overlay);
		    setLastFocusedIndex(-1);
		    populate();
		}
		
		protected boolean onTap(int index) {
		    return false;
		}
		
		public void clear() {
		    mOverlays.clear();
		    mapView.removeAllViews();
		    setLastFocusedIndex(-1);
		    populate();
		}
	}
*/
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	synchronized public void onLocationChanged(Location location) {
		currentLatitude =location.getLatitude();
		currentLongitude=location.getLongitude();
		locationAccuracyInMeters = location.getAccuracy();
		if (progressDialog!=null && locationAccuracyInMeters<100){
			useCurrentCoordinates();
			progressDialog.dismiss();
			progressDialog=null;
            Tools.appendLog("   dismiss dialog because we got the data we needed");
		}
        Tools.appendLog("   onLocationChanged "+currentLatitude+","+currentLongitude);
		Log.i("MAP","Location changed accurate to meters "+locationAccuracyInMeters);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		lookUpGps.setEnabled(false);		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		lookUpGps.setEnabled(true);
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}
}
