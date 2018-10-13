package catglo.com.deliverydroid.homeScreen;

import java.util.ArrayList;

import android.content.Context;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;


interface HomeScreen_Utils {
	void getRoundTripTimeAndGeopoints(Context contect, ArrayList<Order> orders, DataBase dataBase, HomeScreen_Util.HomeScreenRoutingListener listener);
}
