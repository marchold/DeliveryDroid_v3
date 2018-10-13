package catglo.com.deliveryDatabase;

import java.util.Calendar;
import java.util.Date;

import android.database.Cursor;

public class GpsNote extends NotedObject {
	public double lng = 0;
	public double lat = 0;
	public long ID = 0;

	public Calendar time;
	public boolean notification;
	public boolean alarm;

	public GpsNote(){
		
	};
	
	public GpsNote(Cursor c) {
		notes = c.getString(c.getColumnIndex("note"));
		lat = c.getDouble(c.getColumnIndex("GPSLat"));
		lng = c.getDouble(c.getColumnIndex("GPSLng"));
		ID = c.getLong(c.getColumnIndex("ID"));
		alarm = c.getInt(c.getColumnIndex("alarm"))==1;
		notification = c.getInt(c.getColumnIndex("notification"))==1;
		String timeString = c.getString(c.getColumnIndex("time"));	
		time = Calendar.getInstance();
		time.setTimeInMillis(Order.GetTimeFromString(timeString));
		distanceInLatLng = c.getDouble(c.getColumnIndex("dist"));
	}
	
	@Override
	public double getLat() {
		return lat;
	}
	@Override
	public double getLng() {
		return lng;
	}
	
	@Override
	public Date getTime() {
		return time.getTime();
	}
}
