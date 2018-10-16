package catglo.com.deliverydroid.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import catglo.com.deliveryDatabase.AddressInfo;
import catglo.com.deliverydroid.R;
import org.mapsforge.map.android.view.MapView;


public class SettingsAddressFilterCriteria extends Activity implements TextWatcher, LocationListener {
	Pattern LOOKUPABLE_ADDRESS_STRING = Pattern.compile("\\w+\\s+\\w{3,100}");
	Pattern GPS_COORDINATES_STRING = Pattern.compile("[0-9]+\\.[0-9]+\\,[0-9]+\\.[0-9]+");
	
	private ProgressDialog progressDialog;

	AddressInfo selectedAddress;
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	//private ArrayList<OverlayItem> mOverlays;
	private float locationAccuracyInMeters;
	
	public String getKey(){
		return "storeAddress";
	}
	
	
	private LocationManager locationManager;
	private double currentLatitude;
	private double currentLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_filter_criteria_activity);
		
		mapView      = (MapView)                   findViewById(R.id.mapview);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		/*
		if (storeAddressLat==0 || storeAddressLng==0 || storeAddress.length()==0){
			Toast.makeText(getApplicationContext(), "Missing store address", Toast.LENGTH_LONG).show();
			Intent i = new Intent(this,SettingsStoreAddressActivity.class);
			startActivity(i);
		}*/
		
		/*save.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			//TODO: save preference but check for good lookup
			final Editor prefEditor = sharedPreferences.edit();
            String addressString = autocomplete.getText().toString(); 
			prefEditor.putString(getKey(), addressString);
			prefEditor.putInt(getKey()+"Lat", (int)(storeAddressLat*1e6) );
	        prefEditor.putInt(getKey()+"Lng", (int)(storeAddressLng*1e6) );
	        prefEditor.commit();
			finish();
		}});
		cancel.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			finish();
		}});
		*/
		
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
	
	/*@Override
	public void afterTextChanged(Editable editable) {
		String enteredAddress = editable.toString();
		Matcher matcher = LOOKUPABLE_ADDRESS_STRING.matcher(enteredAddress);
		if (matcher.find()){
			Log.i("MAP","Looking up info for "+enteredAddress);
			//look up the address and center the map
            new WebServiceGoogleGeocode(getApplicationContext(),enteredAddress,new WebServiceGoogleGeocode.AddressListListener(){
				public void commit(ArrayList<AddressInfo> addressList,String searchString) {
					if (addressList.size()>0){
						Log.i("MAP","Got geocoded "+searchString);
						centerMapToAddress(addressList.get(0));	
					} else {
						Log.i("MAP","Empty address list for geocode attempt");
					}
				}
			});
		} else {
			Log.i("MAP","Not enough string for address");
		}
	}*/
	
	/*void centerMapToAddress(AddressInfo addressInfo){
		if (addressInfo != null){
			storeAddress = addressInfo.address;
			storeAddressLat = addressInfo.location.getLat();
			storeAddressLng = addressInfo.location.getLng();
			centerMapAndSetStoreAddressOverlay();
		} else {
			Log.i("MAP","NULL ADDRESS INFO");
		}
	}*/

	void centerMapAndSetStoreAddressOverlay(){
	/*
		//Build the overlays for the map markers
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
	
		GeoPoint geoPoint = new GeoPoint((int)(currentLatitude*1e6),(int)(currentLongitude*1e6));

		String storeAddress = sharedPreferences.getString(getKey(), null);
		int storeAddressLat = sharedPreferences.getInt(getKey()+"Lat", 0);
		int storeAddressLng = sharedPreferences.getInt(getKey()+"Lng", 0);
		
		if (storeAddress!=null && storeAddressLng!=0 && storeAddressLat!=0){
			GeoPoint storeGeoPoint = new GeoPoint((int)(currentLatitude*1e6),(int)(currentLongitude*1e6));
			MapOverlay overlay = new MapOverlay(getResources().getDrawable(R.drawable.map_flag),getApplicationContext());
	    	OverlayItem overlayitem = new OverlayItem(storeGeoPoint, storeAddress, "snippet");
	    	overlay.addOverlay(overlayitem);
	    	mapOverlays.add(overlay);
		}
	

		MapController mc = mapView.getController();
		mc.animateTo(geoPoint);
		*/
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
	}*/

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	synchronized public void onLocationChanged(Location location) {
		currentLatitude =location.getLatitude();
		currentLongitude=location.getLongitude();
		locationAccuracyInMeters = location.getAccuracy();
		if (progressDialog!=null && locationAccuracyInMeters<100){
			//useCurrentCoordinates();
			progressDialog.dismiss();
			progressDialog=null;
		}
		Log.i("MAP","Location changed accurate to meters "+locationAccuracyInMeters);
	}

	@Override
	public void onProviderDisabled(String arg0) {
		
	}

	@Override
	public void onProviderEnabled(String arg0) {

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}
}
