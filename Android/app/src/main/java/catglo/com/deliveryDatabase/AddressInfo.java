package catglo.com.deliveryDatabase;

import catglo.com.deliverydroid.data.MyGeoPoint;

import java.io.Serializable;



public class AddressInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public String address;
	public MyGeoPoint location=null;
	public String phoneNumber;
	public Order associatedOrder;
	
	@Override
	public String toString(){
		if (address!=null) return address;
		return phoneNumber;
	}
}