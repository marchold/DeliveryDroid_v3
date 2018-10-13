package catglo.com.deliveryDatabase;

import java.util.Iterator;
import java.util.LinkedList;

public class ZipHash extends LinkedList<ZipCode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean contains(String zipCode){
		Iterator<ZipCode> i = iterator();
		while (i.hasNext()){
			ZipCode z = i.next();
			if (z.zipCode.equalsIgnoreCase(zipCode)){
				return true;
			}
		}	
		return false;
	}
	
	public ZipHash(final int order) {
		super();
	}

	float	minDist	= Float.MAX_VALUE;
	ZipCode	center;
 
	public void insert(final ZipCode t) {
		if (t.distance < minDist) {
			minDist = t.distance;
			center = t;
		}
		
		Iterator<ZipCode> i = iterator();
		while (i.hasNext()){
			ZipCode z = i.next();
			if (z.zipCode.equalsIgnoreCase(t.zipCode)){
				z.state = t.state;
				return;
			}
		}
		
		add(t);
	}

	public ZipCode get(String zipCode) {
		Iterator<ZipCode> i = iterator();
		while (i.hasNext()){
			ZipCode z = i.next();
			if (z.zipCode.equalsIgnoreCase(zipCode)){
				return z;
			}
		}
		return null;
	}

	public void remove(String zipCode) {
		Iterator<ZipCode> i = iterator();
		while (i.hasNext()){
			ZipCode z = i.next();
			if (z.zipCode.equalsIgnoreCase(zipCode)){
				i.remove();
			}
		}
	}

}
