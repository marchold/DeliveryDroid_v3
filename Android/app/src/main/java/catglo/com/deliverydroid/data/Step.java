package catglo.com.deliverydroid.data;

import java.io.Serializable;


//For directions each STEP of this leg of the trip
public class Step extends Object implements Serializable { 
	private static final long serialVersionUID = 1L;
    String   distanceString;
	String   durationString;
	int distance;
	int duration;
	MyGeoPoint endLocation;
	MyGeoPoint startLocation;
    String   htmlInstructions;	
}