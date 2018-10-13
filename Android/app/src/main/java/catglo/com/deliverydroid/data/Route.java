package catglo.com.deliverydroid.data;

import java.io.Serializable;
import java.util.ArrayList;


public class Route extends Object implements Serializable {
	private static final long serialVersionUID = 1L;
	public MyGeoPoint boundsNorteast;
	public MyGeoPoint boundsSouthwest;
	public int meters;
	public int seconds;
	public String getDistance(){
		return new String(""+((int)((float)meters*0.000621371))+" miles");
	}
	public String getDuration(){
		int hours = seconds/(60*60);
		int remainder = seconds-(hours*60*60);
		int minutes = remainder/60;
		
		if (hours > 0){
			return String.format("%d:%02d", hours,minutes);
		} else if (minutes > 0) {
			return String.format("%2d Min", minutes);
		} else {
			return "0";
		}
	}
	
	public ArrayList<Leg> legs = new ArrayList<Leg>();
}
