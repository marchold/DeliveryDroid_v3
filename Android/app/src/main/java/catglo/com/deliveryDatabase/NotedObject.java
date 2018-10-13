package catglo.com.deliveryDatabase;

import java.util.Date;

abstract public class NotedObject {
	public String notes;
	public abstract double getLat();
	public abstract double getLng();
	public abstract Date getTime();
	public double distanceInLatLng;
}
