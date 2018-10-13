package catglo.com.deliveryDatabase;

import catglo.com.deliverydroid.data.MyGeoPoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ZipCode {

	public static final int	STATE_NEEDS_LOOKUP			= 1;
	public static final int	STATE_LOOKUP_SUCCESS		= 2;
	public static final int	STATE_LOOKUP_FAILED			= 3;
	public static final int	STATE_NOT_IN_DELIVERY_AREA	= 4;

	public String			zipCode;
	MyGeoPoint geoPoint;
	public float			distance;
	public int				state;
	float					longitude;
	float					latitude;
	String					provence;
	// <AdministrativeAreaName>WA</AdministrativeAreaName>
	static private Pattern	findStateFromGeocode		= Pattern.compile("\\<AdministrativeAreaName\\>(\\w+)\\<");
	// <Point><coordinates>-121.9931680,47.8785090,0</coordinates></Point>
	static private Pattern	parsePointCordinates		= Pattern
																.compile("\\<Point\\>\\<coordinates\\>([0-9\\.\\-]+),([0-9\\.\\-]+)");
	// <address>Snohomish, WA 98290, USA</address>
	static private Pattern	findAddress					= Pattern.compile("\\<address\\>.*\\<\\/address");

	ZipCode(final String key) {
		super();
		zipCode = key;
	}

	ZipCode(final String zipcode, final int state, final float lat, final float lon, final float dist, final String st) {
		super();
		zipCode = zipcode;
		this.state = state;
		geoPoint = new MyGeoPoint(lat, lon );
		longitude = lon;
		latitude = lat;
		distance = dist;
		provence = st;
	}
/*
	public ZipCode(final String zipcode, final int state, final HttpClient client) {
		super();
		zipCode = zipcode;
		this.state = state;
		getZipCodeGeopoint(this, client);
	}


	static public ZipCode getZipCodeGeopoint(final ZipCode cur, final HttpClient client) {
		// String provence

		String requestString = new String("http://maps.google.com/maps/geo?q=" + cur.zipCode
				+ "&output=xml&key=061vUOvKcZ-cIdJqdiHt1Yix5A_Zi5CoquFaL_g");
		final HttpGet request = new HttpGet(requestString);
		HttpResponse response;
		try {
			response = client.execute(request);
			InputStream in;

			in = response.getEntity().getContent();

			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;

			while ((line = reader.readLine()) != null) {
				final Matcher m2 = findStateFromGeocode.matcher(line);
				if (m2.find()) {
					cur.provence = m2.group(1);
				}

				final Matcher m = parsePointCordinates.matcher(line);
				while (m.find()) {
					final String lng = m.group(1);
					final String lat = m.group(2);
					try {
						cur.latitude = new Float(lng);
						cur.latitude = new Float(lat);
						cur.geoPoint = new GeoPoint((int) (cur.latitude * 1E6), (int) (cur.longitude * 1E6));
						return cur;
					} catch (final Exception e) {
						return null;
					}
				}
				// m = findAddress.matcher(line);
				// if (m.find()){
				// nearByAddress = m.group(1);
				// }
			}
			in.close();
		} catch (final Exception ex) {
			//Log.i("ZIP", "reverse geocode Lookup failed ");
		}

		return null;
	}
*/
}
