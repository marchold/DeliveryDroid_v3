package catglo.com.deliverydroid.data;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyGeoPoint implements Serializable {
    public double lat;
    public double lng;
    public MyGeoPoint(float lat, float lng)
    {
        this.lng = (double)lng;
        this.lat = (double)lat;
    }
    public MyGeoPoint(double lat, double lng)
    {
        this.lng = lng;
        this.lat = lat;
    }
}

