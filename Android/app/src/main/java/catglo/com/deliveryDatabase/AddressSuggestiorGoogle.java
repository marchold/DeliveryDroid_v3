package catglo.com.deliveryDatabase;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import catglo.com.deliverydroid.data.MyGeoPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.preference.PreferenceManager;


public class AddressSuggestiorGoogle {

	/*private String bounds;
	float range;
	private LocationManager locationManager;
	private String bestProvider;
	private Location location;
	private double lat;
	private double lng;
		
	public interface AddressSuggestionCommitor {
		void commit(ArrayList<AddressInfo> addressList, String searchString);
	}
	
	public AddressSuggestionCommitor commitor;

	public AddressSuggestiorGoogle(Context context,AddressSuggestionCommitor commitor){
		super();
		this.context = context;
		
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		range = sharedPreferences.getFloat("dileveryRadius", 1);
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);			
		this.commitor = commitor;
	}

	@Override
	protected void handleJsonResponce(JSONObject jsonResponse, String searchString) throws JSONException {
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
		commitor.commit(addresses,searchString);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected String getURL(String addressSoFar) throws UnsupportedEncodingException {
		String address = URLEncoder. encode(addressSoFar,"UTF-8");
		String retVal=null;
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String addressFilterComponents = sharedPreferences.getString("addressFilterComponents", "");
		
		try {
			location = locationManager.getLastKnownLocation(bestProvider);
			lat = location.getLatitude();
			lng = location.getLongitude(); 
			bounds = "&bounds="+(lat-range)+","+(lng-range)+URLEncoder.encode("|")+(lat+range)+","+(lng+range);
			retVal = "http://maps.googleapis.com/maps/api/geocode/json?address="+address+bounds+"&sensor=true";
		} catch (NullPointerException e){
			e.printStackTrace();
			retVal = "http://maps.googleapis.com/maps/api/geocode/json?address="+address+"&sensor=false";
		}
		
		
		if (addressFilterComponents.length()>1){
			retVal = retVal+"&components="+URLEncoder.encode(addressFilterComponents);
		} 
	
		if (sharedPreferences.getBoolean("generateDevLog", false)==true){
			FileWriter f;
	        try {
				f = new FileWriter(Environment.getExternalStorageDirectory()+"/dr_log"+".txt",true);
				f.write("Google Lookup URL \n"+retVal);
		        f.flush();
		        f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		return retVal;
	}*/
}
