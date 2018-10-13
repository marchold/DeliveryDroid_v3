package catglo.com.deliverydroid.homeScreen;

import java.util.ArrayList;

import android.content.Context;
import android.preference.PreferenceManager;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.data.Leg;
import catglo.com.deliverydroid.data.Route;


class HomeScreen_Util implements HomeScreen_Utils {
	
	public interface HomeScreenRoutingListener {
		void onRoutingSucceded(Route route, ArrayList<Order> orders);
		void onRoutingFailed(ArrayList<Order> orders);
		void onRoutingComplete();
		void onRoutingStarted();
	}

	@Override
	public void getRoundTripTimeAndGeopoints(final Context context,final ArrayList<Order> orders, final DataBase dataBase,final HomeScreenRoutingListener listener) {
		listener.onRoutingStarted();
		final ArrayList<Order> localOrders = orders;
		new WebServiceDirections(context,
								 PreferenceManager.getDefaultSharedPreferences(context),
    			                 localOrders, 
    			                 false, 
    			                 new DirectionsListener(){public void result(final ArrayList<Route> routes,final ArrayList<Order> waypoints) {
    		
    			//try 
    			{
    				for (Order o : waypoints){
						if (o.hasBeenLookedUp){
							dataBase.saveGeolocation(o);
						}
					}
    				
    				if (routes!=null && routes.size()>0){
	        			Route route = routes.get(0);//TODO check for alternatives and sort from best to worst tipper too
	        			
	        			for (Leg leg : route.legs){
							Order o = leg.order;
							if (o != null){
								o.distance = leg.distance;
								o.travelTime = leg.duration;
								if (o.geoPoint==null || o.geoPoint.geoPoint.getLatitudeE6()==0){
									o.geoPoint.geoPoint = leg.endPoint.geoPoint;
								}
								o.legOfRoute = leg;
								o.hasBeenLookedUp=true;	
							}
						}
	        			
	        			listener.onRoutingSucceded(route, orders);	    			
	        			
    				} else {
        				listener.onRoutingFailed(orders);
        			} 
    			
    			}/* catch (Exception e){
    				listener.onRoutingFailed(orders);
    				if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("generateDevLog", false)==true){
    					FileWriter f; try {Writer writer = new StringWriter();PrintWriter printWriter = new PrintWriter(writer);e.printStackTrace(printWriter);String es = writer.toString();
    			        f = new FileWriter(Environment.getExternalStorageDirectory()+"/dr_log"+".txt",true);f.write("\nMap Activity service Exception  "+e.getLocalizedMessage()+"\n"+es+"\n");
    				    f.flush();f.close();} catch (IOException e2) {}
    				}
    			} finally {*/
    				listener.onRoutingComplete();
    			//}
			
    	}});
	}

	
}
