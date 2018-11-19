package catglo.com.deliverydroid.homeScreen;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.ListAddressHistoryActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import catglo.com.deliverydroid.bankDrop.BankTillDropActivity;
import catglo.com.deliverydroid.data.Leg;
import catglo.com.deliverydroid.data.Route;
import catglo.com.deliverydroid.settings.SettingsActivity;
import catglo.com.deliverydroid.shift.ShiftStartEndDayActivity;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import catglo.com.deliverydroid.widgets.DragSortController;
import catglo.com.deliverydroid.widgets.DragSortListView;
import catglo.com.deliverydroid.widgets.DragSortListView.DragListener;
import catglo.com.deliverydroid.widgets.DragSortListView.DropListener;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class HomeScreen_ListFragmentDragDrop extends ListFragment implements DropListener, DragListener{
	protected DataBase dataBase;
	protected SharedPreferences sharedPreferences;
	protected ArrayList<Order> orders;
	protected ViewGroup noListAltView;
	protected OnTouchListener gestureListener;
	protected TextView roundTripTime;
	//protected CheckBox optimizeCheckbox;
	protected Editor prefEditor;
	protected TextView driverEarnings;
	protected ProgressBar pleaseWaitForDriverEarnings;
	protected ImageView errorIcon;
	protected Button navToStore;
	protected Button settings;
	protected Button hourlyPayButton;
	protected TextView helpBubble;
	protected ViewGroup optimizeClickable;
	protected ImageView optimizeIcon;
	protected boolean isSwitchWageButton=false;
	protected TextView optimizeText;
	protected ViewGroup roundTripTimeArea;
	protected Button makeADropButton;
	
	HomeScreen_Utils util = new HomeScreen_Util();
	protected HomeScreen_Util.HomeScreenRoutingListener routeTimeEstimateListener;
	protected HomeScreen_Util.HomeScreenRoutingListener routeOptimizationListener;
    private Button menuGpsNotesButton;
    private Button menuShiftButton;
    private Button menuSearchButton;
    private Button orderSummaryButton;
    private View helpIcon;
	private Button downloadMapsButton;

	public HomeScreen_ListFragmentDragDrop() {
		super();
		///this.gestureListener = null;
				routeTimeEstimateListener = new HomeScreen_Util.HomeScreenRoutingListener(){
					public void onRoutingStarted() {getActivity().runOnUiThread(new Runnable(){public void run(){
						pleaseWaitForDriverEarnings.setVisibility(View.VISIBLE);
					}});}
					public void onRoutingSucceded(final Route route, ArrayList<Order> orders) {getActivity().runOnUiThread(new Runnable(){public void run(){
						roundTripTime.setText(route.getDuration());
		    			errorIcon.setVisibility(View.GONE);
					}});}
					public void onRoutingFailed(ArrayList<Order> orders) {getActivity().runOnUiThread(new Runnable(){public void run(){
						errorIcon.setVisibility(View.VISIBLE);
					}});}
					public void onRoutingComplete() {getActivity().runOnUiThread(new Runnable(){public void run(){
						pleaseWaitForDriverEarnings.setVisibility(View.GONE);
						updateUI();
					}});}
				};
				routeOptimizationListener = new HomeScreen_Util.HomeScreenRoutingListener(){
					public void onRoutingStarted() {getActivity().runOnUiThread(new Runnable(){public void run(){
						pleaseWaitForDriverEarnings.setVisibility(View.VISIBLE);
					}});}
					public void onRoutingSucceded(final Route route, final ArrayList<Order> orders) {getActivity().runOnUiThread(new Runnable(){public void run(){
						if (dataBase==null) return;
						if (route.legs.size() == orders.size()+1) {
							roundTripTime.setText(route.getDuration());
			    			errorIcon.setVisibility(View.GONE);
			    			
							roundTripTime.setText(route.getDuration());
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
							HomeScreen_ListFragmentDragDrop.this.orders = neworders;
							getListView().setVisibility(View.VISIBLE);
							postResults(neworders);
							
						} else {
							onRoutingFailed(orders);
						}
					}});}
					public void onRoutingFailed(ArrayList<Order> orders) {getActivity().runOnUiThread(new Runnable(){public void run(){
						errorIcon.setVisibility(View.VISIBLE);
					}});}
					public void onRoutingComplete() {getActivity().runOnUiThread(new Runnable(){public void run(){
						pleaseWaitForDriverEarnings.setVisibility(View.GONE);
						updateUI();
					}});}
					
				};
	}

	void setOptimizeOn(boolean isOn){
		boolean oldState = sharedPreferences.getBoolean("optimizeCheckbox", false);
		
		if (HomeScreenActivity.alreadyAskedStoreAddress==false){
			int askedHowManyTimes = sharedPreferences.getInt("AskedAboutStoreAddres", 1);
 			HomeScreenActivity.alreadyAskedStoreAddress=true;
 			if (sharedPreferences.getString("storeAddress", "").length()==0) {
 				double a = Math.random();
 				double b = (1/(askedHowManyTimes*3));
 				if (askedHowManyTimes>1 & a<b) {
 					isOn=false;
	 				final Context theContext = getActivity().getApplicationContext();
	     			AlertDialog.Builder delBuilder = new AlertDialog.Builder(getActivity());
						delBuilder.setIcon(R.drawable.icon);
						delBuilder.setIcon(R.drawable.icon).setTitle(R.string.you_need_store);
						delBuilder.setPositiveButton(R.string.Yes, new OnClickListener() {
							public void onClick(final DialogInterface dialog, final int whichButton) {
								startActivityForResult(new Intent(theContext, SettingsActivity.class), 0);
							}
						}).setNegativeButton("No", null);
						delBuilder.create().show();
					prefEditor = sharedPreferences.edit();
					prefEditor.putInt("AskedAboutStoreAddres", askedHowManyTimes+1);
					prefEditor.commit();
 				}
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
				util.getRoundTripTimeAndGeopoints(getActivity().getApplicationContext(), orders, dataBase, routeOptimizationListener);
				Toast.makeText(getActivity().getApplicationContext(), R.string.Optimize_Route_Toast, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getApplicationContext(), R.string.Manual_Route_Toast, Toast.LENGTH_LONG).show();
			}
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
		if (gestureListener!=null) {
			return gestureListener.onTouch(v, event);
		} else {
			return false;
		}
	}};
	
	public static void formatTableRowViewForOrder(View v,Order order,SharedPreferences sharedPreferences, int index){
		 boolean showOrderCost = sharedPreferences.getBoolean("showOrderCost", true);
		 boolean showLastTipAmount = sharedPreferences.getBoolean("showLastTipAmount", false);
		 boolean showNumberOfPastDeliveries = sharedPreferences.getBoolean("showNumberOfPastDeliveries", false);
		 boolean showAveragePercentageTip = sharedPreferences.getBoolean("showAveragePercentageTip", false);
		 boolean showAverageTip = sharedPreferences.getBoolean("showAverageTip", true);
		 boolean showOrderTime = sharedPreferences.getBoolean("showOrderTime", true);
		 boolean showWaitTime = sharedPreferences.getBoolean("showWaitTime", false);
		 boolean showBestIcon = false;
		 boolean showWorstIcon = false;
		 boolean showBestWorstIcon = sharedPreferences.getBoolean("showIconForBestWorstTippers",false);
		 
		 
		 TextView addressView = (TextView)v.findViewById(R.id.addressSmall);   
	   	 TextView timeView = (TextView)v.findViewById(R.id.textView1);
	   	 TextView costView = (TextView)v.findViewById(R.id.textView2);
	   	 
	   	 TextView tipView = (TextView)v.findViewById(R.id.AverageTip);
	   	 TextView numberOfPastDeliveries = (TextView)v.findViewById(R.id.numberOfPastDeliveries);
	   	 TextView averagePercentageTip = (TextView)v.findViewById(R.id.AveragePercentageTip);
	   	 TextView waitTime = (TextView)v.findViewById(R.id.waitTime);
	   	 TextView lastTipThisAddress = (TextView)v.findViewById(R.id.lastTipThisAddress);
	   	 ImageView circleRedIcon = (ImageView)v.findViewById(R.id.circleRedIcon);
	   	 ImageView circleGreenIcon = (ImageView)v.findViewById(R.id.circleGreenIcon);
	     ImageView icon = (ImageView)v.findViewById(R.id.icon);
	   	 
	   	 //TODO: check that this isValidated is working
	     if (order.isValidated){
	    	 int imageResource = v.getContext().getResources().getIdentifier("drawable/map"+(index+1), null, v.getContext().getPackageName());
		   	 icon.setImageResource(imageResource);   	 
	   	 } else {
	   		icon.setImageResource(R.drawable.ic_warning);
	   	 }


	   	 if (showOrderCost) {
	   		 costView.setVisibility(View.VISIBLE);
	   		 costView.setText(Tools.getFormattedCurrency(order.cost));
	   	 }else{
	   		 costView.setVisibility(View.GONE);
	   	 }
	   	 if (showLastTipAmount && order.tipTotalsForThisAddress.deliveries>0){
	   		 lastTipThisAddress.setVisibility(View.VISIBLE);
	   		 lastTipThisAddress.setText(Tools.getFormattedCurrency(order.tipTotalsForThisAddress.lastTip));
	   	 } else {
	   		 lastTipThisAddress.setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (showNumberOfPastDeliveries){
	   		 numberOfPastDeliveries.setVisibility(View.VISIBLE);
	   		 numberOfPastDeliveries.setText("#"+order.tipTotalsForThisAddress.deliveries);
	   	 }else {
	   		 numberOfPastDeliveries.setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (showAveragePercentageTip && order.tipTotalsForThisAddress.deliveries>0){
	   		 averagePercentageTip.setVisibility(View.VISIBLE);
	   		 averagePercentageTip.setText("%"+(int)order.tipTotalsForThisAddress.averagePercentageTip);
	   	 } else {
	   		 averagePercentageTip.setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (showAverageTip && order.tipTotalsForThisAddress.deliveries>0){
	   		 tipView.setVisibility(View.VISIBLE);
	   	 }else{
	   		 tipView.setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (showOrderTime){
	   		 timeView.setVisibility(View.VISIBLE);
	   	 } else {
	   		 timeView.setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (showWaitTime){
	   		 waitTime.setVisibility(View.VISIBLE);
	   		 waitTime.setText(order.getMinutesAgo()+"min");
	   	 } else {
	   		 waitTime.setVisibility(View.GONE);
	   	 }
	   	 
	   	 addressView.setText(order.address);
	   	 timeView.setText(Tools.getFormattedTime(order.time));
	   	 if (Float.isNaN(order.tipTotalsForThisAddress.averageTip)) {
	   		 tipView.setText("");
	         } else {
	   		 tipView.setText(Tools.getFormattedCurrency(order.tipTotalsForThisAddress.averageTip));
	   	 }
	   	 
	   	 
	   	 
	   	 TextView driveTime = (TextView)v.findViewById(R.id.driveTime);
	   	 if (order.travelTime != null && order.travelTime.length()>0){
	   		 driveTime.setVisibility(View.VISIBLE);
	   		 driveTime.setText(order.travelTime);
	   	 } else {
	   		 driveTime.setVisibility(View.INVISIBLE);
	   	 }
	   	 
	   	 driveTime.setVisibility(View.INVISIBLE);
	   	 if (sharedPreferences.getBoolean("optimizeCheckbox", false)   == true 
	   	  || sharedPreferences.getBoolean("calculateRouteTimes", false)== true){
	   		 if (order!=null && order.isValidated){
	       	//	 ((ImageView)v.findViewById(R.id.warningIcon)).setImageResource(R.drawable.location_ok);	 
	       		 
	       		 if (order.travelTime != null && order.travelTime.length()>0){
	           		 driveTime.setVisibility(View.VISIBLE);
	           		 driveTime.setText(order.travelTime);
	           	 } 
	       		 
	   		 } else {	 
	   			 ((ImageView)v.findViewById(R.id.warningIcon)).setImageResource(R.drawable.ic_error_location);
	   		 }
	   	 } else {
	   		 ((ImageView)v.findViewById(R.id.warningIcon)).setVisibility(View.GONE);
	   	 }
	   	 
	   	 if (order.tipTotalsForThisAddress.deliveries>0 && showBestWorstIcon) {

             float badTipThreshold = 0.5f;
             try {
                 badTipThreshold = Float.parseFloat(sharedPreferences.getString("thresholdForBadTip","0.5"));
             } catch (NumberFormatException e) {};
             float goodTipThreshold = 0.5f;
             try {
                 goodTipThreshold = Float.parseFloat(sharedPreferences.getString("goodTipThreshold","5"));
             } catch (NumberFormatException e) {};


             if (order.tipTotalsForThisAddress.averageTip < badTipThreshold) showWorstIcon=true;
	       	 if (order.tipTotalsForThisAddress.averageTip > goodTipThreshold) showBestIcon=true;
	       	 
	       	 if (showBestIcon){
	       		 circleGreenIcon.setVisibility(View.VISIBLE);
	       	 } else {
	       		 circleGreenIcon.setVisibility(View.GONE);
	       	 }
	       	 
	       	 if (showWorstIcon){
	       		 circleRedIcon.setVisibility(View.VISIBLE);
	       	 } else {
	       		 circleRedIcon.setVisibility(View.GONE);
	       	 }
	   	 } else {
	   		 circleGreenIcon.setVisibility(View.GONE);
	   		 circleRedIcon.setVisibility(View.GONE);
	   	 }
	}

	static public class OrderAdapter extends ArrayAdapter<Order> {
		private ArrayList<Order> items;
		private Context context;
		boolean handleMode=false;
		DisplayMetrics metrics;

	    public OrderAdapter(Context context, int textViewResourceId, ArrayList<Order> items) {
	            super(context, textViewResourceId, items);
	            this.items = items;
	            this.context = context;
	             
	            metrics = new DisplayMetrics();
	            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
	            int height = (int)(metrics.heightPixels/metrics.density)-150;
	            int rowsHeight = (int)(items.size()*80*metrics.density);
	            
	            Log.i("rows","rowsheight="+rowsHeight+"   screenHeight="+height);
	            
	            if (rowsHeight>=height){
	            	 handleMode=true;
	            }     
	     }

	     @Override
	     public View getView(int position, View convertView, ViewGroup parent) {
	             View v = convertView;
	             if (v == null) {
	                 LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                 v = vi.inflate(R.layout.home_screen_table_row, null);
	             }
	             Order order = items.get(position);
	             if (order != null) {
                     SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                     formatTableRowViewForOrder(v,order,sharedPreferences,position);
	             }
	             
	             FrameLayout handle = (FrameLayout)v.findViewById(R.id.dragHandle);
	             LayoutParams lp = handle.getLayoutParams();
	             if (handleMode){
	            	 lp.width=(int)(90*metrics.density);
	             } else {
	            	 lp.width = LayoutParams.MATCH_PARENT;
	             }
	             
	             v.setTag(order);
	             return v;
	     }
	}
	
	OrderAdapter orderListAdapter;
	private DragSortListView orderListView;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    	getListView().setOnItemClickListener(new OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
    		showOptionsDialog(orders.get(arg2));
		}});

        prefEditor = sharedPreferences.edit();

        setOptimizeOn(sharedPreferences.getBoolean("optimizeCheckbox", false));
        optimizeClickable.setOnTouchListener(bluingToucher);
        optimizeClickable.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
        	setOptimizeOn(!sharedPreferences.getBoolean("optimizeCheckbox", false));
		}});
        
    	orderListView = (DragSortListView)getListView();
    	orderListView.setDropListener(this);
   	}


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.home_screen_drag_drop_list_fragment, null);
		view.setOnTouchListener(gestureListener);
        
		noListAltView = (ViewGroup)view.findViewById(R.id.noListAltView);
        //optimizeCheckbox = (CheckBox)view.findViewById(R.id.autoOptimizeCheckbox);
        //optimizeCheckbox.setOnTouchListener(gestureListener);
        roundTripTime = (TextView)view.findViewById(R.id.roundTripTime);
        driverEarnings = (TextView)view.findViewById(R.id.driverEarnings);
        pleaseWaitForDriverEarnings = (ProgressBar)view.findViewById(R.id.progressBarRoundTrip);
        errorIcon = (ImageView)view.findViewById(R.id.errorIcon);
        navToStore = (Button)view.findViewById(R.id.navToStore);
        settings = (Button)view.findViewById(R.id.settings);
        hourlyPayButton = (Button)view.findViewById(R.id.clockOutAndBackup);
        callStore = (Button)view.findViewById(R.id.callstoreButton);
        smsStore = (Button)view.findViewById(R.id.smsStoreButton);

        downloadMapsButton      = (Button)noListAltView.findViewById(R.id.downloadMapClickListener);
        menuGpsNotesButton      = (Button)noListAltView.findViewById(R.id.menuGpsNotesClickListener);
        menuShiftButton         = (Button)noListAltView.findViewById(R.id.menuShiftClickListener);
        menuSearchButton        = (Button)noListAltView.findViewById(R.id.menuSearchClickListener);
        orderSummaryButton      = (Button)noListAltView.findViewById(R.id.orderSummaryClickable);//Not sure what this one dies

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        
        
        helpBubble = (TextView)view.findViewById(R.id.SpeechBubbleHelp);
        
        optimizeClickable = (ViewGroup)view.findViewById(R.id.optimizeClickable);
        optimizeIcon = (ImageView)view.findViewById(R.id.optimizeIcon);
        optimizeText = (TextView)view.findViewById(R.id.optimizeRouteText);
        roundTripTimeArea = (ViewGroup)view.findViewById(R.id.roundTripTimeArea);
        
        makeADropButton = (Button)view.findViewById(R.id.makeADropButton);

		orderListView = (DragSortListView)view.findViewById(android.R.id.list);
	    
		DragSortController controller = new DragSortController(orderListView);
        controller.setDragHandleId(R.id.dragHandle);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DRAG);

        helpIcon = view.findViewById(R.id.imageView1);
        
	     
        orderListView.setFloatViewManager(controller);
        orderListView.setOnTouchListener(controller);
        orderListView.setDragEnabled(true);
	        
        return view;
		 
       
    }
	
	//Listener for drag drop events from list
	public void drop(final int from, final int to) {		
		float newDeliveryOrder;
		Order toOrder = orders.get(to);
		Order fromOrder = orders.get(from);
		float dragToOrder = toOrder.deliveryOrder;
		int count = orders.size();
		
		
		if (to < from) { //dragging item up
			if (to<=0){  //Drag to the beginning of the list
				float max =Float.MIN_VALUE;
				for (Order o : orders){
					if (max < o.deliveryOrder)
						max = o.deliveryOrder;
				}
				newDeliveryOrder = max+2;
			} else {
				newDeliveryOrder = (dragToOrder + orders.get(to-1).deliveryOrder) / 2;
			}
		} else { //Dragging item down
			if (to>=count-1){ //Drag to the end of the list
				float min =Float.MAX_VALUE;
				for (Order o : orders){
					if (min > o.deliveryOrder)
						min = o.deliveryOrder;
				}
				newDeliveryOrder = min-2;
			} else {
				newDeliveryOrder = (dragToOrder + orders.get(to+1).deliveryOrder) / 2;
			}
		}
		
		
	
		dataBase.changeOrder(fromOrder.primaryKey, newDeliveryOrder);
		updateUI();
	};

	synchronized protected void updateUI() {
		if (getActivity()==null) return;
		if (dataBase == null) {
        	dataBase = new DataBase(getActivity().getApplicationContext());
        	dataBase.open();
        }
		
		Log.i("CURSOR","HomeScreen_ListFragmentBase - updateUI");
		
		
			
		if (orders!=null){
			ArrayList<Order> oldOrders = orders;
			orders = dataBase.getUndeliveredOrdersArray();
			for (Order old : oldOrders){
				for (Order order : orders){
					if (order.primaryKey == old.primaryKey && order.address.equalsIgnoreCase(old.address)){
						order.legOfRoute = old.legOfRoute;
						order.hasBeenLookedUp = old.hasBeenLookedUp;
						order.distance = old.distance;
						order.travelTime = old.travelTime;
					}
				}
			}
		} else {
			orders = dataBase.getUndeliveredOrdersArray();
		}
		//noListAltView.setVisibility(View.GONE);
		
		
		//If dualWage is true and the pay rate is = to the on road pay rate the hourlyPayButton. Hijack the button as a switch to in store rate.
  		Wage currentWage = dataBase.currentWage();
  		float hourlyRateOnRoad = Tools.parseCurrency(sharedPreferences.getString("hourly_rate_on_road", "0"));
  		DecimalFormat df =  new DecimalFormat("#.##");
  		String onRoadWage = df.format(hourlyRateOnRoad);
  		String curWage = df.format(currentWage.wage);
  		final float inStoreRate = Tools.parseCurrency(sharedPreferences.getString("hourly_rate", "0"));

        if (sharedPreferences.getBoolean("dual_wage", false) && onRoadWage.equalsIgnoreCase(curWage)){
			isSwitchWageButton=true;
			hourlyPayButton.setVisibility(View.VISIBLE);
			hourlyPayButton.setText(getString(R.string.Set_Pay_Rate_To)+" "+ Tools.getFormattedCurrency(inStoreRate));
		} else {
			if (sharedPreferences.getBoolean("pay_rate_button", false)){
				hourlyPayButton.setVisibility(View.VISIBLE);
			} else {
				hourlyPayButton.setVisibility(View.GONE);
			}
			hourlyPayButton.setText(getString(R.string.Time_clock));
		}
        hourlyPayButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
        	if (isSwitchWageButton){
        		dataBase.setWage(inStoreRate, dataBase.getShift(DataBase.TodaysShiftCount), DateTime.now());
        		updateUI();
        		isSwitchWageButton=false;
        	} else {
	        	Intent intent = new Intent(getActivity(),ShiftStartEndDayActivity.class);
				startActivity(intent);
        	}
		}});
		
		
		
		if (orders!=null && orders.size()>0){
			for (Order order : orders){
				//Look up average tip this address
				order.tipTotalsForThisAddress = dataBase.getTipTotal(getActivity().getApplicationContext(), 
						" `"+DataBase.Address +"` LIKE "+DatabaseUtils.sqlEscapeString(order.address)
						+" AND `"+DataBase.AptNumber+"` LIKE "+DatabaseUtils.sqlEscapeString(order.apartmentNumber)
						+" AND Payed != -1",null);
			}
			noListAltView.setVisibility(View.GONE);
			getListView().setVisibility(View.VISIBLE);
		} else {
			noListAltView.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}
		
		if (sharedPreferences.getString("storeAddress", "").length()==0 || orders.size() < 2 || orders.size() > 8){
			optimizeClickable.setVisibility(View.GONE);
		} else {
			optimizeClickable.setVisibility(View.VISIBLE);
		}
		
		Log.i("CURSOR","HomeScreen_ListFragmentDragDrop - updateUI");
		
		TipTotalData tip = dataBase.getTipTotal(getActivity(),DataBase.Shift+"="+dataBase.getCurShift()+" AND "+DataBase.Payed+" >= 0",
				"WHERE shifts.ID="+DataBase.TodaysShiftCount);
		final float totalTipsMade = tip.payed-tip.cost;
		driverEarnings.setText(Tools.getFormattedCurrency(totalTipsMade + tip.mileageEarned));
		
		float dropAmount=0;
		try {
			String dropAmountString = sharedPreferences.getString("dropAlarmAmount", "0");
			dropAmount = Float.parseFloat(dropAmountString);
		} catch (Exception e){};
	
		int dropRowCount = sharedPreferences.getInt("DropRowCount", 1);
		float totalDrops = 0;
		for (int i = 0; i < dropRowCount; i++){
			totalDrops += sharedPreferences.getFloat("drop_val_"+i,0f); 
	    }
		float bankAmount = sharedPreferences.getFloat("BankAmount", 0);
		float bankMinusDrops = bankAmount - totalDrops;
        
        float cashOnHand;

        if (sharedPreferences.getBoolean("dontCountTipsForDropAlarms",false)==false) {
            cashOnHand = tip.payedCash + bankMinusDrops;
        } else {
            cashOnHand = tip.payedCash - tip.cashTips + bankMinusDrops;
        }
        NotificationManager mNotificationManager =
		    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
       

        //TODO: This should probably be moved up to the activity layer
        if (dropAmount>0 && dropAmount<=cashOnHand && sharedPreferences.getBoolean("justClearedBank", false)==false){
			makeADropButton.setVisibility(View.VISIBLE);
			if (sharedPreferences.getBoolean("playAlarmSoundForDrop", false)){
				RingtoneManager.getRingtone(getActivity().getApplicationContext(),RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
			}
			if (sharedPreferences.getBoolean("dropAlarmSystemNotification", false)){
				Notification.Builder mBuilder =
			        new Notification.Builder(getActivity())
			        .setSmallIcon(R.drawable.icon)
			        .setContentTitle("Make a drop")
			        .setContentText("Cash on hand "+ Tools.getFormattedCurrency(cashOnHand));
				// Creates an explicit intent for an Activity in your app
				Intent resultIntent = new Intent(getActivity(), HomeScreenActivity.class);
	
				// The stack builder object will contain an artificial back stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
				// Adds the back stack for the Intent (but not the Intent itself)
				stackBuilder.addParentStack(HomeScreenActivity.class);
				// Adds the Intent that starts the Activity to the top of the stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent =
				        stackBuilder.getPendingIntent(
				            0,
				            PendingIntent.FLAG_UPDATE_CURRENT
				        );
				mBuilder.setContentIntent(resultPendingIntent);
				
				// mId allows you to update the notification later on.
				mNotificationManager.notify(HomeScreenActivity.DROP_NOTIFICATION, mBuilder.build());
			}
			
			
		} else {
			makeADropButton.setVisibility(View.GONE);
			mNotificationManager.cancel(HomeScreenActivity.DROP_NOTIFICATION);
		}
		
		orderListAdapter = new OrderAdapter(getActivity().getApplicationContext(), R.layout.home_screen_table_row,orders);
		setListAdapter(orderListAdapter);
		
		
	}
	    
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


	public void postResults(ArrayList<Order> orders) {
		orderListAdapter = new OrderAdapter(getActivity().getApplicationContext(),R.layout.home_screen_table_row,orders);
		setListAdapter(orderListAdapter);
	}

	@Override
	public void drag(int from, int to) {
		setOptimizeOn(false);
		prefEditor.putBoolean("optimizeCheckbox", false);
    	prefEditor.commit();
	}
	
	protected void showOptionsDialog(final Order order) {
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(getActivity());
		popupBuilder.setIcon(R.drawable.icon);
		int listId;
		listId = R.array.main_list_options;
		popupBuilder.setItems(listId, new OnClickListener(){public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case 0: //View/Edit
				
				startActivity(new Intent(getActivity().getApplicationContext(), SummaryActivity.class).putExtra("DB Key", order.primaryKey));
				
				//startActivity(new Intent(getActivity().getApplicationContext(), EditOrderActivity.class).putExtra("DB Key", order.primaryKey));
				break;
			case 1: //Delete
				AlertDialog.Builder delBuilder = new AlertDialog.Builder(getActivity());
				delBuilder.setIcon(R.drawable.icon);
				delBuilder.setIcon(R.drawable.icon).setTitle(R.string.deleteThisRecord);
				delBuilder.setPositiveButton(R.string.Yes, new OnClickListener() {
						public void onClick(final DialogInterface dialog, final int whichButton) {
							dataBase.delete(order.primaryKey);
							updateUI();
						}
					}).setNegativeButton("No", null);
					delBuilder.create().show();
				break;
			case 2: //Address History
				startActivity(new Intent(getActivity().getApplicationContext(), ListAddressHistoryActivity.class).putExtra("DB Key", order.primaryKey));
				break;
	/*		case 3:{ //Configure list view
				Intent i = new Intent(getActivity().getApplicationContext(),SettingsListOptions.class);
				i.putExtra("order", order);
				startActivity(i);
				break;
			}
			case 4: //Correct Address
				startActivity(new Intent(getActivity().getApplicationContext(), AddressCorrectionActivity.class).putExtra("DB Key", order.primaryKey));
				break;
			*/
			}
		}});
		popupBuilder.create().show();
	}

	boolean[] listChoices = new boolean[7];
	private Button callStore;
	private Button smsStore;
	protected void showListConfigOptions() {
		
		
		/*
		listChoices[0] = sharedPreferences.getBoolean("showOrderCost", true);
		listChoices[1] = sharedPreferences.getBoolean("showLastTipAmount", false);
		listChoices[2] = sharedPreferences.getBoolean("showNumberOfPastDeliveries", false);
		listChoices[3] = sharedPreferences.getBoolean("showAveragePercentageTip", false);
		listChoices[4] = sharedPreferences.getBoolean("showAverageTip", true);
		listChoices[5] = sharedPreferences.getBoolean("showOrderTime", true);
		listChoices[6] = sharedPreferences.getBoolean("showWaitTime", false);
		listChoices[7] = sharedPreferences.getBoolean("showIconForBestWorstTippers", false);
		
		
		AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
		d.setIcon(R.drawable.icon);
		d.setMultiChoiceItems(R.array.homeListConficChoices, listChoices , new OnMultiChoiceClickListener() {public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			listChoices[which] = isChecked;
		}});
		d.setPositiveButton("Save", new OnClickListener() {public void onClick(DialogInterface dialog, int which) {
			Editor e = sharedPreferences.edit();
			e.putBoolean("showOrderCost", listChoices[0]);
			e.putBoolean("showLastTipAmount", listChoices[1]);
			e.putBoolean("showNumberOfPastDeliveries", listChoices[2]);
			e.putBoolean("showAveragePercentageTip", listChoices[3]);
			e.putBoolean("showOrderTime", listChoices[4]);
			e.putBoolean("showOrderTime", listChoices[5]);
			e.putBoolean("showWaitTime", listChoices[6]);
			e.putBoolean("showIconForBestWorstTippers", listChoices[7]);
			
			e.commit();
			updateUI();
		}});
		d.show();// TODO Auto-generated method stub
		*/
	}



	@Override
	public void onHiddenChanged(boolean isHidden){
		if (isHidden==false){
			updateUI();
		}
	}
	int visibleIcons;
	@Override
	public void onResume(){
		super.onResume();
		if (dataBase == null) {
        	dataBase = new DataBase(getActivity().getApplicationContext());
        	dataBase.open();
        }
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (sharedPreferences.getString("storeAddress", "").length()==0){
        	setOptimizeOn(false);
        	prefEditor = sharedPreferences.edit();
        	prefEditor.putBoolean("optimizeCheckbox", false);
        	prefEditor.commit();
        } else {
        	
        }


        navToStore.setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
        	String storeAddress = sharedPreferences.getString("storeAddress", "");
    		//final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+storeAddress));
			//startActivity(i);	
        	DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
        	activity.tools.navigateTo(storeAddress, activity);
        }});
        
        settings.setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
        	startActivityForResult(new Intent(getActivity().getApplicationContext(), SettingsActivity.class), 0);
        }});

        final String phoneNumber = sharedPreferences.getString("storePhoneNumber", "");
        if (phoneNumber.length()==0){
            callStore.setVisibility(View.GONE);
            smsStore.setVisibility(View.GONE);

        } else {
            callStore.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phoneNumber)));
            }});
            smsStore.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"+phoneNumber));
                i.putExtra("sms_body", sharedPreferences.getString("smsbody", ""));
                startActivity(i);
            }});
            settings.setVisibility(View.GONE);
        }

		updateUI();
		if (sharedPreferences.getString("storeAddress", "").length()>0 && orders.size()!=0){
			
			if (sharedPreferences.getBoolean("optimizeCheckbox", false) ){
				util.getRoundTripTimeAndGeopoints(getActivity().getApplicationContext(), orders, dataBase, routeOptimizationListener);
			} else if (sharedPreferences.getBoolean("calculateRouteTimes", false) ){
				util.getRoundTripTimeAndGeopoints(getActivity().getApplicationContext(), orders, dataBase, routeTimeEstimateListener);
			} else {
				
				pleaseWaitForDriverEarnings.setVisibility(View.GONE);
				errorIcon.setVisibility(View.GONE);
				
				if (orders.size()==0){
					getListView().setVisibility(View.GONE);
					noListAltView.setVisibility(View.VISIBLE);
				} else {
					getListView().setVisibility(View.VISIBLE);
					noListAltView.setVisibility(View.GONE);
					postResults(orders);
				}
			}
		} else {
			//false :sharedPreferences.getString("storeAddress", "").length()>0 && orders.size()!=0){
		}
		
		
		if (sharedPreferences.getBoolean("optimizeCheckbox", false)    == true 
		 || sharedPreferences.getBoolean("calculateRouteTimes", false) == true){
			roundTripTimeArea.setVisibility(View.VISIBLE);
		} else {
			roundTripTimeArea.setVisibility(View.GONE);
		}
		if (sharedPreferences.getString("storeAddress", "").length()==0){
			navToStore.setVisibility(View.GONE);
		} else {
			navToStore.setVisibility(View.VISIBLE);
		}

        HomeScreenActivity activity = (HomeScreenActivity)getActivity();

        if (menuGpsNotesButton!=null) menuGpsNotesButton.setOnClickListener(activity.menuGpsNotesClickListener);
        if (downloadMapsButton!=null) downloadMapsButton.setOnClickListener(activity.menuDownloadMapClickListener);

        if (menuShiftButton!=null)    menuShiftButton.setOnClickListener(activity.menuShiftClickListener);
        if (menuSearchButton!=null)   menuSearchButton.setOnClickListener(activity.menuSearchClickListener);
        if (orderSummaryButton!=null) orderSummaryButton.setOnClickListener(activity.orderSummaryClickListener);


        ArrayList<String> helpBubbleStrings = new ArrayList<String>();
		for (String s : getResources().getStringArray(R.array.main_screen_help_bubble)) {
			helpBubbleStrings.add(s);
		}
		
		
		if (sharedPreferences.getString("storeAddress", "").length()==0){
			roundTripTime.setText("need store address");
			roundTripTime.setTextSize(11);
			helpBubbleStrings.add(getResources().getString(R.string.set_store_address_help_bubble));
		} else {
			roundTripTime.setTextSize(16);
			roundTripTime.setText("...");
		}
		
		makeADropButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
			startActivity(new Intent(getActivity().getApplicationContext(), BankTillDropActivity.class));
		}});

        if (menuShiftButton!=null)    menuShiftButton.setVisibility(View.VISIBLE);
        if (menuGpsNotesButton!=null) menuGpsNotesButton.setVisibility(View.VISIBLE);
        if (menuSearchButton!=null)   menuSearchButton.setVisibility(View.VISIBLE);

        if (sharedPreferences.getBoolean("showHelpBubbles",true))
        {
            helpBubble.setVisibility(View.VISIBLE);
            int index = (int)(Math.random()*((float)helpBubbleStrings.size()));
            helpBubble.setText(helpBubbleStrings.get(index));
            if (helpIcon!=null) helpIcon.setVisibility(View.VISIBLE);
        } else {
            helpBubble.setVisibility(View.GONE);
            if (helpIcon!=null) helpIcon.setVisibility(View.GONE);

        }

		
	}
	
	
	public void onPause(){
		super.onPause();
		dataBase.close();
		dataBase=null;
	}
	
	
	
	
}
