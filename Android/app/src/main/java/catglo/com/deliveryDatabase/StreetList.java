package catglo.com.deliveryDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import catglo.com.deliverydroid.data.MyGeoPoint;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
/*
class StreetStore {
	public StreetStore() {
		super();
	}

	public Street update(final String name, final int start, final int stop) {
		Street s = find(name);
		s = null;
		s = find(name);
		if (s == null) {
			s = new Street(name, start, stop);
			insert(s);
		}
		s.update(start, stop);
		return s;
	}

}


interface DoForEachLine {
	void parseAndStore(String line);
}

class HttpDataFetcher {
	private final HttpClient	client;
	private final DoForEachLine	doForEachLine;
	private final String		requestString;

	public HttpDataFetcher(final HttpClient client, final String requestString, final DoForEachLine doForEachLine) {
		this.client = client;
		this.doForEachLine = doForEachLine;
		this.requestString = requestString;
	}

	void fetchAndParse() {
		final HttpGet request = new HttpGet(requestString);
		HttpResponse response;
		try {
			response = client.execute(request);
			InputStream in;

			in = response.getEntity().getContent();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;

			while ((line = reader.readLine()) != null) {
				doForEachLine.parseAndStore(line);
			}
			in.close();
		} catch (final Exception ex) {
			//Log.i("HttpDataFetcher", "failed " + requestString);
		}
	}
}
*/
public class StreetList extends Thread {
	public final static String					URL_QUE_FILE_NAME	= "/streetUrlQue.dat....";
	public final static String					STREET_NAMES_FILE	= "/streetNames.dat....";

	private final String						UrlFileName;									// = new
	// String("/data/data/com.example.DeliveryDriver/databases/streetsUrlQue.dataFile");
	private final String						StreetsFileName;								// = new
	// String("/data/data/com.example.DeliveryDriver/databases/streetNames.dataFile");

	//static private StreetStore					streets=null;										// Store the street's with no
																								// duplicates
	public static LinkedList<StreetNameInformation>	parentList=null;									// Store the initial list
	// of street names
	static public ZipHash								zipCodes=null;										// Store zip codes we use
	public boolean								exit				= false;
	private boolean								keepSearching		= true;

	public boolean								ready				= false;

	public boolean isReady() {
		return ready;
	}

	// private DatabaseHelper databaseHelper;
	// private SQLiteDatabase dataBase;
	Context						context;
	//public DefaultHttpClient	client;
	private final Pattern		rangeFinder;

	// private static final float ZIP_AREA_DEFAULT = 6;

	MyGeoPoint center	= null;
	LocationManager				locationManager;
	Location					l		= null;
	Geocoder					geocoder;
	private final Pattern		parseZipCodefromXML;
	private final Pattern		parseLatititudeFromXML;
	private final Pattern		parseLongititudeFromXML;
	private final Pattern		parseDistanceFromGeocodeXML;
	private final Pattern		parseStateAdminCode1FromXML;
	double						longitude;
	double						latitude;
	boolean needsData;
	private Pattern	parseCordinatesLonLatFromXML;
	
	public StreetList(final Context activity) {
	//	if (streets==null){
	//		streets = new StreetStore();
			parentList = new LinkedList<StreetNameInformation>();
			zipCodes = new ZipHash(5);
			needsData=true;
	//	} else {
	//		needsData=false;
	//	}
		this.context = activity.getApplicationContext();
		final String path = activity.getFilesDir().toString();
		UrlFileName = path + URL_QUE_FILE_NAME;
		StreetsFileName = path + STREET_NAMES_FILE;

		//client = new DefaultHttpClient();
		rangeFinder = Pattern.compile("\\;\\'\\>([0-9]+)\\sto\\s([0-9]+)");

		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		parseZipCodefromXML = Pattern.compile("\\<postalcode\\>([0-9]{5})\\<\\/postalcode\\>");// Parse
		// zipCode
		parseLatititudeFromXML = Pattern.compile("\\<lat\\>([0-9\\.\\-]+)\\<\\/lat\\>"); // Parse
		// latitude
		parseLongititudeFromXML = Pattern.compile("\\<lng\\>([0-9\\.\\-]+)\\<\\/lng\\>"); // Parse
		// longitude
		// <distance>6.295178930282145</distance>
		parseDistanceFromGeocodeXML = Pattern.compile("\\<distance\\>([0-9\\.]+)");
		// <address>Snohomish, WA 98290, USA</address>
		parseStateAdminCode1FromXML = Pattern.compile("\\<adminCode1\\>(\\w{2})");// <adminCode1>WA</adminCode1>

		// <Point><coordinates>-122.0058344,47.9577350,0</coordinates></Point>
		parseCordinatesLonLatFromXML = Pattern.compile("\\<Point\\>\\<coordinates\\>([0-9\\.]+),([0-9\\.]+)");

		geocoder = new Geocoder(activity);
	}

	public MyGeoPoint getCurrentLocation() throws NoClassDefFoundError {
		final List<String> providers = locationManager.getProviders(true);

		if (providers.size() == 0) return null;

		/*
		 * Loop over the array backwards, and if you get an accurate location, then break out the loop
		 */
		for (int i = providers.size() - 1; i >= 0; i--) {
			l = locationManager.getLastKnownLocation(providers.get(i));
			if (l != null) {
				break;
			}
		}

		if (l == null) return null;
		center = new MyGeoPoint(l.getLatitude(), l.getLongitude());
		longitude = l.getLongitude();
		latitude = l.getLatitude();
		return center;
	}

	public void findCloseByZipCodes() {
		/*final String requestString = new String("http://ws.geonames.org/findNearbyPostalCodes?lat=" + latitude
				+ "&lng=" + longitude);
		final HttpGet request = new HttpGet(requestString);
		HttpResponse response;
		try {
			response = client.execute(request);
			InputStream in;

			in = response.getEntity().getContent();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			float lat = 0;
			float lng = 0;
			String zip = "";
			Float distance = 0f;
			String provence = null;

			while ((line = reader.readLine()) != null) {
				
				
				final Matcher m = parseZipCodefromXML.matcher(line);
				while (m.find()) {
					final String b = m.group(1);
					zip = b;
				}

				final Matcher m2 = parseLatititudeFromXML.matcher(line);
				while (m2.find()) {
					final String b = m2.group(1);
					final Float f = new Float(b);
					lat = (int) (f * (float) 1E6);
				}

				final Matcher m3 = parseLongititudeFromXML.matcher(line);
				while (m3.find()) {
					final String b = m3.group(1);
					lng = new Float(b);
				}

				final Matcher m5 = parseDistanceFromGeocodeXML.matcher(line);
				if (m5.find()) {
					distance = new Float(m5.group(1));
				}

				final Matcher m6 = parseStateAdminCode1FromXML.matcher(line);
				if (m6.find()) {
					provence = new String(m6.group(1));
				}

				if (lat != 0 && lng != 0 && zip.length()!=0 && distance != 0 && provence != null) {

					
					
					final int state = ZipCode.STATE_NOT_IN_DELIVERY_AREA;
					// if (distance < ZIP_AREA_DEFAULT)
					// state = ZipCode.STATE_NEEDS_LOOKUP;

					final ZipCode z = new ZipCode(zip, state, lat, lng, distance, provence);
					zipCodes.insert(z);

					lat = 0;
					zip = "";
					lng = 0;
					distance = 0f;
					provence = null;
				}
			}
			in.close();
		} catch (final Exception ex) {
			Log.e("STREER", "Lookup failed ");
			ex.printStackTrace();
		}
		
		Log.e("STREET","Loaded "+zipCodes.size()+"zip codes in findCloseByZipCodes");

		*/
	}

	public static StreetList LoadState(final Context context) {
		StreetList list = new StreetList(context);

		/*if (list.needsData==false) {
			return list;
		}
		
		File theFile;
		FileInputStream oStream;
		BufferedInputStream outStream;
		DataInputStream objStream;

		final String path = context.getFilesDir().toString();
		final String UrlFileName = path + URL_QUE_FILE_NAME;

		theFile = new File(UrlFileName);

		try {
			// setup a stream to a physical file on the filesystem
			oStream = new FileInputStream(theFile);
			outStream = new BufferedInputStream(oStream, 8192);

			// attach a stream capable of writing objects to the stream that is
			// connected to the file
			objStream = new DataInputStream(outStream);

			
			// Load Zip Codes
			final int size = objStream.readInt();
			
			for (int i = 0; i < size; i++) {

				int integerZipCodeOrEscape = objStream.readInt();
				String theRealZip;
				if (integerZipCodeOrEscape==-1)
				{
					
					theRealZip = objStream.readUTF();
				}
				else
				{
					theRealZip = ""+integerZipCodeOrEscape;
				}
				final String zipCode = theRealZip;
	
				final int state = objStream.readByte();
				final float longitude = objStream.readFloat();
				final float latitide = objStream.readFloat();
				final float distance = objStream.readFloat();
				final char[] c = new char[2];
				c[0] = (char) objStream.readByte();
				c[1] = (char) objStream.readByte();
				final String provence = new String(c);
				final ZipCode z = new ZipCode(zipCode, state, longitude, latitide, distance, provence);
				list.zipCodes.insert(z);
				//Log.i("DRIVER",""+z.zipCode);
			}

			// Load parent list of streets
			// list.parentList =
			// (LinkedList<StreetNameInformation>)objStream.readObject();
			String last = "";
			String dupCheck="";
			int len = 0;
			final int count = objStream.readInt();
			for (int i = 0; i < count; i++) {
				len = objStream.readByte();
				int l = objStream.readChar();
				char[] c = new char[l];
				for (int j = 0; j < l; j++) {
					c[j] = (char) objStream.readByte();
				}
				final String url_end = new String(c);
				final String url = new String(last.substring(0, len) + url_end);
				final int state = objStream.readByte();

				l = objStream.readByte();
				c = new char[l];
				for (int j = 0; j < l; j++) {
					c[j] = (char) objStream.readByte();
				}
				final String name = new String(c);

				final StreetNameInformation p = new StreetNameInformation(url, name);
				if (name.compareTo(dupCheck)!=0)
					list.parentList.add(p);
				//list.streets.insert(new Street(name.replace('+', ' ')));
				len = url.length();
				last = url;
				dupCheck = name;
			}

			// close down the streams
			objStream.close();
			outStream.close();
		} catch (final IOException e) {
			System.err.println("IOExcetiopn in LoadState. " + e.getMessage());
			e.printStackTrace();
			// theFile.delete();
			list = new StreetList(context);
		} catch (final Exception e) {
			System.err.println("Excetiopn in (que)LoadState. " + e.getMessage());
		} // catch

		
		//Log.i("time LoadState", "" + (t2 - t1));
*/
		// TODO: if we dont have zip codes find them
		return list;

	}

	public void saveURLState() {
		//Log.i("driver", "Saving URL que State");
		File theFile;
		FileOutputStream oStream;
		BufferedOutputStream outStream;
		DataOutputStream objStream;
		theFile = new File(UrlFileName);

		try {
			// setup a stream to a physical file on the filesystem
			oStream = new FileOutputStream(theFile);
			outStream = new BufferedOutputStream(oStream, 8192);

			// attach a stream capable of writing objects to the stream that is
			// connected to the file
			objStream = new DataOutputStream(outStream);

			// Save Zip Codes
			objStream.writeInt(zipCodes.size());
			final Iterator<ZipCode> zips = zipCodes.iterator();//  .keys();
			while (zips.hasNext()) {

				/*
				 * int zipCode = objStream.readInt(); int state = objStream.readByte(); float longitude = objStream.readFloat();
				 * float latitide = objStream.readFloat(); float distance = objStream.readFloat(); char[] c = new char[2]; c[0] =
				 * (char)objStream.readByte(); c[1] = (char)objStream.readByte(); String provence = new String(c); ZipCode z = new
				 * ZipCode(zipCode,state,longitude,latitide,distance,provence); list.zipCodes.insert(z);
				 */

				final ZipCode zip = zips.next();
				
				if (zip==null) Log.e("STREET","if (zip==null) in saveURLState");
				
				/*
				//So I had to fix this zip code bug without breaking peoples files so I pack the length in the high bits and 
				//use them if they are there otherwize ignore.
				int l = zip.zipCode.length();
				int z=0;
				try { 
					z=Integer.parseInt(zip.zipCode); 
				} catch (NumberFormatException e){
					
					
				};
				
				int packedZipCodeData = (l << 24) | z;*/
				
				int escape = -1;
				objStream.writeInt(escape);
				objStream.writeUTF(zip.zipCode);
				objStream.writeByte((byte) zip.state);
				objStream.writeFloat(zip.longitude);
				objStream.writeFloat(zip.latitude);
				objStream.writeFloat(zip.distance);
			    char c[];
				try {
					c = zip.provence.toCharArray();
				} catch (Exception e){
					Log.w("STREET","Exception converting zip.provence.toCharArray in saveURLState - recovering with empty provence");
					c = new char[2];
					c[0]='a';
					c[1]='a';
				}
				objStream.writeByte(c[0]);
				objStream.writeByte(c[1]);
				
				Log.e("STREET","Wrote zip code "+zip.zipCode) ;

			}

			// Save parent list of streets
			// objStream.writeObject(parentList);
			String last = "";
			int len = 0;
			objStream.writeInt(parentList.size());
			for (int i = 0; i < parentList.size(); i++) {
				final StreetNameInformation street = parentList.get(i);
				while (len > 1) {
					if (street.suffixUrl.startsWith(last.substring(0, len))) {
						break;
					}
					len--;
				}
				if (len > 255) {
					len = 255;
				}
				objStream.writeByte(len);
				String s = street.suffixUrl.substring(len, street.suffixUrl.length());

				int l = s.length();
				objStream.writeChar(l);
				char[] c = s.toCharArray();
				for (int j = 0; j < l; j++) {
					objStream.writeByte(c[j]);
				}
				objStream.writeByte(0);
				// objStream.writeObject(street.name);
				s = street.name;
				l = s.length();
				objStream.writeByte(l);
				c = s.toCharArray();
				for (int j = 0; j < l; j++) {
					objStream.writeByte(c[j]);
				}
				len = street.suffixUrl.length();
				last = street.suffixUrl;
			}

			// close down the streams
			objStream.flush();
			objStream.close();
			
		} catch (final IOException e) {
			System.err.println("Things not going as planned.");
			e.printStackTrace();
		} catch (final Exception e) {
			System.err.println("Exception in saveState." + e.getMessage());
			e.printStackTrace();
			// catch
		}

		//Log.i("time url's saveState", "" + (t2 - t1));


	}

	
	String[]				addressList	= null;
	
	

	private long				lastWaitTimestamp	= System.currentTimeMillis();
	static private final int	TIMEOUT_DELAY		= 60000;
	private int					lastTimeout			= Integer.MIN_VALUE;

	private void networkFailWait() {
		final long time = System.currentTimeMillis();
		final long timeSinceLast = time - lastWaitTimestamp;

		 // if it has not been 10seconds + timeout since the last network failure
		if (timeSinceLast > 0 && time < lastWaitTimestamp + lastTimeout + 10000) {
			
			lastTimeout = TIMEOUT_DELAY * 60; // then wait for 1 hour instead of
			// 1 minute
			//Log.i("SLEEP", "wait for network =" + lastTimeout + "   " + time + " < " + lastWaitTimestamp + "   +   "
			//		+ lastTimeout);
			try {
				sleep(lastTimeout);
			} catch (final InterruptedException e) {
			}
		} else {
			lastTimeout = TIMEOUT_DELAY; // Otherwise just wait a short while
		//	Log.i("SLEEP", "wait for network =" + lastTimeout);
			try {
				sleep(60000);
			} catch (final InterruptedException e) {
			}
		}
		lastWaitTimestamp = time;
	}

	private void waitForNetwork() {
		final ConnectivityManager network = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean validConnectedNetwork = false;
		while (validConnectedNetwork == false && !exit) {
			final NetworkInfo[] networkInfo = network.getAllNetworkInfo();
			for (int i = 0; i < networkInfo.length; i++) {
				final NetworkInfo.State networkState = networkInfo[i].getState();
				if (networkState == NetworkInfo.State.CONNECTED) {
					validConnectedNetwork = true;
					return;
				}
			}
			networkFailWait();
		}
	}

	void blockOnSettings() {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		while (sharedPreferences.getBoolean("EnableBackgroundData", true) == false) {
			//Log.i("SLEEP", "wait for settings 2");
			try {
				sleep(60000);
			} catch (final Exception e) {
			}
			;
		}
	}

	@Override
	public void run() {
	//	while (keepSearching && !exit) {
			keepSearching = true;
			int numKeys = 0;
			while (numKeys != zipCodes.size() && !exit) {
				numKeys = zipCodes.size();
				int count = 0;
				final Iterator<ZipCode> keys = zipCodes.iterator();
				while (keys.hasNext()) { // while there are zip codes to
					// process
					final String zipCode = keys.next().zipCode;
					// if the zip code has not been processed
					Log.i("DRIVER","Trying Zip Code "+zipCode);
					
					if (zipCodes.get(zipCode)       == null){
						Log.e("STREET","NULL Zip Code when loading street list");
					}
					
					if (    zipCodes.get(zipCode)       != null 
						&& (zipCodes.get(zipCode).state == ZipCode.STATE_NEEDS_LOOKUP
						||  zipCodes.get(zipCode).state == ZipCode.STATE_LOOKUP_FAILED)) 
					{	
						Log.i("STREETS", "startBuildingZip " + zipCode);
						waitForNetwork();
						blockOnSettings();
						// then get the web page and parse it
						if (startBuildingStreetList(zipCode) == false) {
							networkFailWait();
						}
						++count;
					}
					if (exit) return;
				}
				if (count > 0) {
					saveURLState();
				}
			}
			//try {
			//	sleep(1200000);
			//} //catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
		//}
		//Log.i("STREETS", "exiting thread");
		
	}

	synchronized public void addZipCode(final String zipCode) {
		if (zipCode == null) return;
		if (zipCode.length() < 5) return;
		if (!zipCodes.contains(zipCode)) {
			zipCodes.insert(new ZipCode(zipCode));
		}
	}


	synchronized private boolean startBuildingStreetList(final String zipCode) {
	/*	int i = 0;
		{
			final String requestString = new String("http://www.melissadata.com/lookups/zipstreet.asp?InData="
					+ zipCode);
			//Log.i("STREETS", "HTTP get " + requestString);
			final HttpGet request = new HttpGet(requestString);
			HttpResponse response;
			try {
				response = client.execute(request);
				InputStream in;

				in = response.getEntity().getContent();

				final BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
				String line = null;
				while ((line = reader.readLine()) != null) {

//				      <td><a href="zipstreet.asp?Step5=98290&Name=100TH"> 100th</a></td></tr>        

					
					final Pattern p = Pattern.compile("(zipstreet\\.asp\\?Step5\\=" + zipCode + "\\&Name\\=)(.*)\\x22");
					// Pattern p =
					// Pattern.compile("(zipstreet\\.asp\\?Step4\\="+"90210"+"\\&Name\\=)(.*)\\x22");
					// Log.i("STREETS","HTML "+line);
					final Matcher m = p.matcher(line);
					while (m.find()) {
						final String b = new String(m.group(1).toCharArray());
						final String c = new String(m.group(2).toCharArray());
						final StreetNameInformation s = new StreetNameInformation(b + c, c);
						synchronized (this) {
							int j =0;
							for (; j < parentList.size(); j++){
								StreetNameInformation ppp = parentList.get(j);
								if (ppp.name.compareTo(s.name) >0)
									break;
							}	
							parentList.add(j,s);
							//streets.insert(new Street(s.name.replace('+', ' '), 0, 0));
						}
						// Log.i("STREETS","Added "+c);
						i++;
					}
				}
				in.close();
			} catch (final Exception ex) {
				//Log.i("STREET", "Lookup failed " + zipCode + " " + ex.getMessage());
				return false;
			}
			if (i == 0) {
				//Log.i("STREETS", "Zip code lookup for " + zipCode + " failed due to bad page");
				zipCodes.get(zipCode).state = ZipCode.STATE_LOOKUP_FAILED;
				keepSearching = true;
				return false;
			} else {
				synchronized (this) {
					zipCodes.get(zipCode).state = ZipCode.STATE_LOOKUP_SUCCESS;
				}
			}
		}

		*/
		return true;
	}
	
	synchronized StreetNameInformation getStreetName(String streetText){
		for (int i = 0; i < parentList.size(); i++){
			if (parentList.get(i).name.compareTo(streetText) == 0){
				return parentList.get(i);
			}
		}
		return null;
	}
	
	synchronized public void deleteStreet(StreetNameInformation street) {
		//parentList.remove(street);
		int j =0;
		for (; j < parentList.size(); j++){
			StreetNameInformation ppp = parentList.get(j);
			if (ppp.name.compareTo(street.name)==0){
				parentList.remove(j);
				return;
			}
		}	
	}
	
	synchronized public StreetNameInformation getStreet(StreetNameInformation street) {
		if (street==null)
			return null;
		int j =0;
		for (; j < parentList.size(); j++){
			StreetNameInformation ppp = parentList.get(j);
			if (ppp.name.compareTo(street.name)==0){
				return parentList.get(j);
			}
		}	
		return null;
	}

	synchronized public void addStreet(StreetNameInformation street) {
		int j =0;
		for (; j < parentList.size(); j++){
			StreetNameInformation ppp = parentList.get(j);
			if (ppp.name.compareTo(street.name)==0)//dont add duplicates
				return;
			if (ppp.name.compareTo(street.name) >0)//add before next biggest item
				break;
		}	
		parentList.add(j,street);
	}
	
	
	// This one is called after a zip code has been ok'ed for download. Its for
	Pattern	postalcodePattern	= Pattern.compile("\\<postalcode\\>([0-9]+)\\<\\/");	// <postalcode>98272</postalcode>
	
	Pattern	latParse = Pattern.compile("\\<lat\\>([\\-0-9\\.]+)\\<\\/");	
	Pattern lonParse = Pattern.compile("\\<lng\\>([\\-0-9\\.]+)\\<\\/");
	Pattern distParse = Pattern.compile("\\<distance\\>([0-9\\.]+)\\<\\/");
	Pattern stateParse = Pattern.compile("\\<adminCode1\\>(\\w+)\\<\\/");
	Pattern endCode = Pattern.compile("\\<\\/code\\>");
	
	private String zipCodeValue;
	private float latValue;
	private float lngValue;
	private float distValue;
	private String provence;
	
	public void addZipCodeNear(final String curZip) {
		// 98272&country=US&radius=30
	/*	final HttpDataFetcher net = new HttpDataFetcher(client,
				"http://ws.geonames.org/findNearbyPostalCodes?postalcode=" + curZip + "&country=US&radius=60",
				new DoForEachLine() {
					public void parseAndStore(final String line) {
						final Matcher m = postalcodePattern.matcher(line);
						final Matcher mLat = latParse.matcher(line);
						final Matcher mLng = lonParse.matcher(line);
						final Matcher mDist = distParse.matcher(line);
						final Matcher mState = stateParse.matcher(line);
						final Matcher done = endCode.matcher(line);
						if (m.find()) {
							zipCodeValue=m.group(1);
						} 
						if (mLat.find()){
							latValue=new Float(mLat.group(1));
						}
						if (mLng.find()){
							lngValue=new Float(mLng.group(1));
						}
						if (mDist.find()){
							distValue=new Float(mDist.group(1));
						}  
						if (mState.find()){
							provence = mState.group(1);
						}
						if (done.find()){
							zipCodes.insert(new ZipCode(zipCodeValue,ZipCode.STATE_NOT_IN_DELIVERY_AREA,
							                            latValue,lngValue,distValue,provence));
						}
					}
				});
		net.fetchAndParse();*/

	}

	

	
}