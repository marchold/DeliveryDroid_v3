package catglo.com.deliverydroid.homeScreen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.*;
import catglo.com.deliverydroid.data.Leg;
import catglo.com.deliverydroid.data.Route;
import catglo.com.deliverydroid.settings.SettingsActivity;
import org.jetbrains.annotations.NotNull;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.reader.MapFile;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeScreen_MapFragmentActivity extends DeliveryDroidBaseActivity {
	protected DataBase dataBase;
	protected SharedPreferences sharedPreferences;
	protected ArrayList<Order> orders;
	private MapView mapView;
	//private ArrayList<OverlayItem> mOverlays;
	private TextView driverEarnings;
	private TextView roundTripTime;

	protected Editor prefEditor;
	private ProgressBar pleaseWaitForDriverEarnings;
	private ImageView errorIcon;
	private ViewGroup optimizeClickable;
	private ImageView optimizeIcon;
	private TextView optimizeText;
	private ViewGroup roundTripTimeArea;
	
	HomeScreen_Utils util = new HomeScreen_Util();
    HomeScreen_Util.HomeScreenRoutingListener routeTimeEstimateListener;
	HomeScreen_Util.HomeScreenRoutingListener routeOptimizationListener;
	
/*void setOptimizeOn(boolean isOn){
		boolean oldState = sharedPreferences.getBoolean("optimizeCheckbox", false);
		
		if (HomeScreenActivity.alreadyAskedStoreAddress==false){
 			HomeScreenActivity.alreadyAskedStoreAddress=true;
 			if (sharedPreferences.getString("storeAddress", "").length()==0) {
 				isOn=false;
 				final Context theContext = getActivity().getApplicationContext();
     			AlertDialog.Builder delBuilder = new AlertDialog.Builder(getActivity());
					delBuilder.setIcon(R.drawable.icon);
					delBuilder.setIcon(R.drawable.icon).setTitle(R.string.you_need_store);
					delBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int whichButton) {
							startActivityForResult(new Intent(theContext, SettingsActivity.class), 0);
						}
					}).setNegativeButton("No", null);
					delBuilder.create().show();
     		}
     	} 
		
		if (isOn){
			optimizeIcon.setImageResource(R.drawable.icon_opt);
			optimizeText.setText(R.string.Optimize);
		} else {
			optimizeIcon.setImageResource(R.drawable.icon_non_opt);
			optimizeText.setText(R.string.Manual_Route);
		}
		if (oldState!=isOn){
			prefEditor.putBoolean("optimizeCheckbox", isOn);
			prefEditor.commit();
			if (isOn){
				doRouteOptimization();
				Toast.makeText(getActivity().getApplicationContext(), R.string.Optimize_Route_Toast, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getApplicationContext(), R.string.Manual_Route_Toast, Toast.LENGTH_LONG).show();
			}
		}
	}*/
	void setOptimizeOn(boolean isOn){
		boolean oldState = sharedPreferences.getBoolean("optimizeCheckbox", true);
		
		if (HomeScreenActivity.alreadyAskedStoreAddress==false){
 			HomeScreenActivity.alreadyAskedStoreAddress=true;
 			
 			
 			
 			if (sharedPreferences.getString("storeAddress", "").length()==0 ) {
 				
 					
	 				isOn=false;
	 				final Context theContext = getApplicationContext();
	     			AlertDialog.Builder delBuilder = new AlertDialog.Builder(this);
						delBuilder.setIcon(R.drawable.icon);
						delBuilder.setIcon(R.drawable.icon).setTitle(R.string.you_need_store);
						delBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int whichButton) {
								startActivityForResult(new Intent(theContext, SettingsActivity.class), 0);
							}
						}).setNegativeButton("No", null);
						
					
     		}
     	}  
		
		if (isOn){
			optimizeIcon.setImageResource(R.drawable.icon_opt);
			optimizeText.setText(R.string.Optimize);
		} else {
			optimizeIcon.setImageResource(R.drawable.icon_non_opt);
			optimizeText.setText(R.string.Manual_Route);
		}
		if (oldState!=isOn){
			prefEditor.putBoolean("optimizeCheckbox", isOn);
			prefEditor.commit();
			if (isOn){
				util.getRoundTripTimeAndGeopoints(getApplicationContext(), orders, dataBase, routeOptimizationListener);
				Toast.makeText(getApplicationContext(), R.string.Optimize_Route_Toast, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.Manual_Route_Toast, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/*private class TouchOverlay extends com.google.android.maps.Overlay {
        int lastZoomLevel = -1;

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapview) {
            if (event.getAction() == 1) {
                if (lastZoomLevel == -1)
                    lastZoomLevel = mapView.getZoomLevel();

                if (mapView.getZoomLevel() != lastZoomLevel) {
                    onZoom(mapView.getZoomLevel());
                    lastZoomLevel = mapView.getZoomLevel();
                }
            }
            return false;
        }
    }*/

    public void onZoom(int level) {
    	Editor editor = sharedPreferences.edit();
    	editor.putInt("mapZoomLevel", level);
    	editor.commit();
    }

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.my_map_activity);
        if (dataBase == null) {
        	dataBase = new DataBase(getApplicationContext());
        	dataBase.open();
        }
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	mapView = (MapView) findViewById(R.id.mapview);
        roundTripTime = (TextView)findViewById(R.id.roundTripTime);
    	driverEarnings = (TextView)findViewById(R.id.driverEarnings);
        //prefEditor = sharedPreferences.edit();
        optimizeClickable = (ViewGroup)findViewById(R.id.optimizeClickable);
        optimizeIcon = (ImageView)findViewById(R.id.optimizeIcon);
        optimizeText = (TextView)findViewById(R.id.optimizeRouteText);
       
        pleaseWaitForDriverEarnings = (ProgressBar)findViewById(R.id.progressBarRoundTrip);
        errorIcon = (ImageView)findViewById(R.id.errorIcon);
        
        roundTripTimeArea = (ViewGroup)findViewById(R.id.roundTripTimeArea);
        
        routeTimeEstimateListener = new HomeScreen_Util.HomeScreenRoutingListener(){
			public void onRoutingStarted() {runOnUiThread(new Runnable(){public void run(){
				pleaseWaitForDriverEarnings.setVisibility(View.VISIBLE);
			}});}
			public void onRoutingSucceded(final Route route, ArrayList<Order> orders) {runOnUiThread(new Runnable(){public void run(){
				roundTripTime.setText(route.getDuration());
				errorIcon.setVisibility(View.GONE);
			}});}
			public void onRoutingFailed(ArrayList<Order> orders) {runOnUiThread(new Runnable(){public void run(){
				errorIcon.setVisibility(View.VISIBLE);
			}});}
			public void onRoutingComplete() {runOnUiThread(new Runnable(){public void run(){
				pleaseWaitForDriverEarnings.setVisibility(View.GONE);
				buildMapMarkerOverlay();
			}});}
		};
		routeOptimizationListener = new HomeScreen_Util.HomeScreenRoutingListener(){
			public void onRoutingStarted() {runOnUiThread(new Runnable(){public void run(){
				pleaseWaitForDriverEarnings.setVisibility(View.VISIBLE);
			}});}
			public void onRoutingSucceded(final Route route, final ArrayList<Order> orders) {runOnUiThread(new Runnable(){public void run(){
				if (dataBase==null) return;
				if (route.legs.size() == orders.size()+1) {
					roundTripTime.setText(route.getDuration());
	    			errorIcon.setVisibility(View.GONE);
	    			
					int orderIndex=1;
					ArrayList<Order> neworders = new ArrayList<Order>();
					for (Leg leg : route.legs){
						Order o = leg.order;
						if (o != null){
							o.deliveryOrder=orderIndex++;
							neworders.add(o);
							dataBase.changeOrder(o.primaryKey, o.deliveryOrder);
						}
					}
					HomeScreen_MapFragmentActivity.this.orders = neworders;
				} else {
					onRoutingFailed(orders);
				}
			}});}
			public void onRoutingFailed(ArrayList<Order> orders) {runOnUiThread(new Runnable(){public void run(){
				errorIcon.setVisibility(View.VISIBLE);
			}});}
			public void onRoutingComplete() {runOnUiThread(new Runnable(){public void run(){
				pleaseWaitForDriverEarnings.setVisibility(View.GONE);
				buildMapMarkerOverlay();
			}});};
		};
		
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataBase.close();
        dataBase = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        TipTotalData tip = dataBase.getTipTotal(this,DataBase.Shift+"="+dataBase.getCurShift()+" AND "+DataBase.Payed+" >= 0",
        		"WHERE shifts.ID="+DataBase.TodaysShiftCount);
		final float totalTipsMade = tip.payed-tip.cost;
		driverEarnings.setText(Tools.getFormattedCurrency(totalTipsMade + tip.mileageEarned));
		
        updateUI();
        
        if (   sharedPreferences.getBoolean("optimizeCheckbox", false)
       		|| sharedPreferences.getBoolean("calculateRouteTimes", false))
        {
   			roundTripTimeArea.setVisibility(View.VISIBLE);
   		} else {
   			roundTripTimeArea.setVisibility(View.GONE);
   		}
        
        if (sharedPreferences.getString("storeAddress", "").length()>0 && orders.size()!=0){
			
			if (sharedPreferences.getBoolean("optimizeCheckbox", false) ){
				util.getRoundTripTimeAndGeopoints(getApplicationContext(), orders, dataBase, routeOptimizationListener);
			} else if (sharedPreferences.getBoolean("calculateRouteTimes", false) ){
				util.getRoundTripTimeAndGeopoints(getApplicationContext(), orders, dataBase, routeTimeEstimateListener);
			} else {
				pleaseWaitForDriverEarnings.setVisibility(View.GONE);
				errorIcon.setVisibility(View.GONE);
			}
		} else {
			//false :sharedPreferences.getString("storeAddress", "").length()>0 && orders.size()!=0){
		}
        
        
    }
    
    OnTouchListener bluingToucher = new OnTouchListener(){public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_DOWN){
			v.setBackgroundColor(0xFF33b5e5);
			Log.i("driveer","going bluee");
		}
		if (event.getAction()==MotionEvent.ACTION_UP){
			Log.i("driveer","going clear");
			v.setBackgroundColor(Color.TRANSPARENT);
		}
		//if (gestureListener!=null) {
		//	return gestureListener.onTouch(v, event);
		//} else {
			return false;
		//}
	}};


    @SuppressLint("MissingPermission")
	void updateUI(){
    	//First get all the orders in to an array
    	if (sharedPreferences.getString("storeAddress", "").length()==0){
			roundTripTime.setText("need store address");
			roundTripTime.setTextSize(11);
		} else {
			roundTripTime.setTextSize(16);
			roundTripTime.setText("...");
		}
    	optimizeClickable.setOnTouchListener(bluingToucher);
        optimizeClickable.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
         	setOptimizeOn(!sharedPreferences.getBoolean("optimizeCheckbox", false));
 		}});
    	
		orders = new ArrayList<Order>();
		final Cursor c = dataBase.getUndeliveredOrders();
    	if (c != null) {
			if (c.moveToFirst()) {
				do {
					//Look up record for this order in data base
					final Order order = new Order(c);
					
					//Look up average tip this address
					order.tipTotalsForThisAddress = dataBase.getTipTotal(getApplicationContext(), 
							" `"+DataBase.Address +"` LIKE "+DatabaseUtils.sqlEscapeString(order.address)
							+" AND `"+DataBase.AptNumber+"` LIKE "+DatabaseUtils.sqlEscapeString(order.apartmentNumber)
							+" AND Payed != -1",null);

					orders.add(order);

				} while (c.moveToNext());
				if (sharedPreferences.getString("storeAddress", "").length()>0){
					util.getRoundTripTimeAndGeopoints(getApplicationContext(), orders, dataBase, routeTimeEstimateListener);
				}
			} else {
				pleaseWaitForDriverEarnings.setVisibility(View.GONE);
				errorIcon.setVisibility(View.GONE);
			}
		}
		c.close();

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;

        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        float lat=0;
        float lng=0;
        if (l != null) {
            lat = (float) l.getLatitude();
            lng = (float) l.getLongitude();
        }
		lat = Float.parseFloat(sharedPreferences.getString("centrPoint_lat_s", ""+lat));
		lng = Float.parseFloat(sharedPreferences.getString("centrPoint_lng_s", ""+lng));
        Log.i("MAP","Centring map to "+lng+","+lat);


        List<TileCache> tileCaches = new ArrayList<TileCache>();

		float finalLng = lng;
		float finalLat = lat;
		DownloadedMap.Companion.getMapForCurrentLocation(this, new MapReadyListener() {
			@Override
			public void onMapReady(@NotNull DownloadedMap map) {
                MapFile mapFile = new MapFile(map.getMapFile());

                int zoom = sharedPreferences.getInt("mapZoomLevel",15);

                //	MapController mc = mapView.getController();
                //	mc.setCenter(new GeoPoint((int)(lat*1e6),(int)(lng*1e6)));
                //	mc.setZoom(zoom);
				AndroidPreferences preferencesFacade = new AndroidPreferences(getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE));
				mapView.getModel().init(preferencesFacade);
				mapView.setClickable(true);
				mapView.getMapScaleBar().setVisible(true);
				mapView.setBuiltInZoomControls(true);


                tileCaches.add(AndroidUtil.createTileCache(HomeScreen_MapFragmentActivity.this, "AA",
                        mapView.getModel().displayModel.getTileSize(), 1.0f,
                        mapView.getModel().frameBufferModel.getOverdrawFactor()));

				TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(tileCaches.get(0),
						mapView.getModel().mapViewPosition, mapFile, DeliveryDroidMapRenderTheme.OSMARENDER, false, true, false);
				mapView.getLayerManager().getLayers().add(tileRendererLayer);


                IMapViewPosition mvp = mapView.getModel().mapViewPosition;

                LatLong center = mvp.getCenter();

                //if (center.equals(new LatLong(0, 0))) {
                    mvp.setMapPosition(new MapPosition(mapFile.startPosition(), (byte)15));
                //}
               mvp.setZoomLevelMax((byte)18);
               mvp.setZoomLevelMin((byte)8);



			}
		});


		
		if (sharedPreferences.getString("storeAddress", "").length()==0 || orders.size() < 2 || orders.size() > 8){
			optimizeClickable.setVisibility(View.GONE);
		} else {
			optimizeClickable.setVisibility(View.VISIBLE);
		}
		
		buildMapMarkerOverlay();
		
    }
    
	

	void buildMapMarkerOverlay(){
		//Build the overlays for the map markers
	/*
		List<Overlay> mapOverlays = mapView.getOverlays();

		
		//mapOverlays.clear();
		//mapOverlays.add(new TouchOverlay());
		int counter=1;
		int minLat=Integer.MAX_VALUE;
		int maxLat=Integer.MIN_VALUE;
		int minLng=Integer.MAX_VALUE;
		int maxLng=Integer.MIN_VALUE;


        Tools.appendLog("\nBuilding Map Overlays");

		try {
			String storeAddress = sharedPreferences.getString("storeAddress", "");

			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String bestProvider = locationManager.getBestProvider(criteria, false);
			Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
			
			int storeAddressLat = 0;
            int storeAddressLng = 0;
            if (lastKnownLocation!=null) {
                Tools.appendLog("    lastKnownLocation was != null");
                storeAddressLat = sharedPreferences.getInt("storeAddressLat", (int) (lastKnownLocation.getLatitude() / 1e6));
                storeAddressLng = sharedPreferences.getInt("storeAddressLng", (int) (lastKnownLocation.getLongitude() / 1e6));
            } else {
                Tools.appendLog("    lastKnownLocation was == null");
            }
            if (storeAddressLat==0 || storeAddressLng==0){
                Tools.appendLog("    storeAddressLat==0 || storeAddressLng==0");
                if (lastKnownLocation != null) {
                    Tools.appendLog("    setting store address to last known location");
                    storeAddressLat = (int) (lastKnownLocation.getLatitude()*1E6);
                    storeAddressLng = (int) (lastKnownLocation.getLongitude()*1E6);
                }
            }

            Tools.appendLog("    Using gps coordinates "+storeAddressLat+","+storeAddressLng);

			GeoPoint geoPoint = new GeoPoint(storeAddressLat,storeAddressLng);
			
			MapOverlay overlay = new MapOverlay(getResources().getDrawable(R.drawable.map_flag),getApplicationContext());
	    	OverlayItem overlayitem = new OverlayItem(geoPoint, storeAddress, "snippet");
	    	overlay.addOverlay(overlayitem);
	    	mapOverlays.add(overlay);
	
	    	if (geoPoint.getLatitudeE6()  < minLat) minLat = geoPoint.getLatitudeE6();
	    	if (geoPoint.getLongitudeE6() < minLng) minLng = geoPoint.getLongitudeE6();
	    	if (geoPoint.getLatitudeE6()  > maxLat) maxLat = geoPoint.getLatitudeE6();
	    	if (geoPoint.getLongitudeE6() > maxLng) maxLng = geoPoint.getLongitudeE6();
	
			for (Order order : orders){
				if (order.isValidated){
					
					try {
						int imageResource = getResources().getIdentifier("drawable/map"+counter, null, getPackageName());
						overlay = new MapOverlay(getResources().getDrawable(imageResource),getApplicationContext());
				    	overlayitem = new OverlayItem(order.geoPoint.geoPoint, order.address, "snippet");
				    	overlay.addOverlay(overlayitem);
				    	mapOverlays.add(overlay);
					} catch (Resources.NotFoundException e){
						Toast.makeText(getApplicationContext(), R.string.error_building_map_markers, Toast.LENGTH_SHORT).show();
					}
				    	
			    	if (order.geoPoint.geoPoint.getLatitudeE6()  < minLat) minLat = order.geoPoint.geoPoint.getLatitudeE6();
			    	if (order.geoPoint.geoPoint.getLongitudeE6() < minLng) minLng = order.geoPoint.geoPoint.getLongitudeE6();
			    	if (order.geoPoint.geoPoint.getLatitudeE6()  > maxLat) maxLat = order.geoPoint.geoPoint.getLatitudeE6();
			    	if (order.geoPoint.geoPoint.getLongitudeE6() > maxLng) maxLng = order.geoPoint.geoPoint.getLongitudeE6();
			    	
			    	counter++;
				} else {
					//TODO: We need an error icon or something, maybe the ! is enough
				}
			}
		}catch (NullPointerException e){
			e.printStackTrace();
			//Sometimes we will exceed the available map marker images
		}
		int latDif = maxLat-minLat;
		int lngDif = maxLng-minLng;
			
		
		MapController mc = mapView.getController();
		mc.zoomToSpan(latDif,lngDif);

        mc.animateTo(new GeoPoint((maxLat + minLat)/2
        		                 ,(maxLng + minLng)/2));
        
    */
	}
	
	/*@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}*/
}
