package catglo.com.deliverydroid.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import catglo.com.deliveryDatabase.Order;


public class Leg extends Object implements Serializable {
	private static final long serialVersionUID = 1L;
	public String distance;
	public int meters;
	public String duration;
	public int seconds;
	public String endAddress;
	public String startAddress;
	public Order order;
	int orderId=-1;
	public ArrayList<Step> steps = new ArrayList<Step>();
	public MyGeoPoint endPoint;
	
	private void writeObject(ObjectOutputStream oos) throws IOException { 
		oos.writeUTF(distance); 
		oos.writeInt(meters); 
		oos.writeUTF(duration); 
		oos.writeInt(seconds); 
		oos.writeUTF(endAddress); 
		oos.writeUTF(startAddress); 
		oos.writeObject(steps);
		oos.writeObject(endPoint); 
		if (order==null){
			oos.writeInt(-1);
		}else{
			oos.writeInt(order.primaryKey);
		}
	} 

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException { 
		distance = ois.readUTF(); 
		meters = ois.readInt(); 
		duration = ois.readUTF(); 
		seconds = ois.readInt(); 
		endAddress = ois.readUTF(); 
		startAddress = ois.readUTF(); 
		steps = (ArrayList<Step>) ois.readObject();
		endPoint= (MyGeoPoint) ois.readObject(); 
		//oos.writeInt(order.primaryKey);
		orderId = ois.readInt(); 
	} 
		
		
}
