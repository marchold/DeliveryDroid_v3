package catglo.com.deliverydroid.homeScreen;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


import catglo.com.deliveryDatabase.StreetList;
import catglo.com.deliveryDatabase.ZipCode;
import catglo.com.deliverydroid.*;
import catglo.com.deliverydroid.bankDrop.BankTillDropActivity;
import catglo.com.deliverydroid.data.MyGeoPoint;
import catglo.com.deliverydroid.neworder.NewOrderActivity;
import catglo.com.deliverydroid.orderSummary.OrderSummaryActivity;
import catglo.com.deliverydroid.outTheDoor.GpsNotes;
import catglo.com.deliverydroid.outTheDoor.OutTheDoorActivity;
import catglo.com.deliverydroid.settings.SettingsActivity;
import catglo.com.deliverydroid.settings.Settings_ListOptions;
import catglo.com.deliverydroid.shift.ShiftStartEndActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.joda.time.DateTime;

public class HomeScreenActivity extends DeliveryDroidBaseActivity {

	private static final int REQUEST_CODE_ACCESS_LOCATION = 1001;
	protected Editor prefEditor;
	protected View orderSummaryButton;
	protected View outTheDoorButton;
	protected View bankButton;
	protected View totalsButton;
	protected View addOrderButton;
	protected View moreMenuButton;
	private View sortClickable;
	private View mapClickable;
	private View sortUnderline;
	private View mapUnderline;
	private FrameLayout fragmentContainer;


    public HomeScreenActivity() {
		super();
	}

	static final int SORT = 2;
	static final int MAP = 3;
	static final int SETTINGS = 4;
	static final int NEW_SHIFT = 5;
	static final int SEARCH_HISTORY = 6;
	static final int NAV_TO_STORE = 7;
	static final int TRIAL_OVER = 9;
	static final int REVIEW_FOR_MORE = 10;
	static final int STORE_ADDRESS_FIRST = 11;

	static final int DROP_NOTIFICATION = 12;
	public static final int DELIVERY_NOTIFICATION = 13;

	static final int CALL_STORE = 14;
	static final int GPS_NOTES = 15;
	static final int CUSTOMIZE_LIST = 16;

	public StreetList streetList = null;
	public Thread startUpThread;
	private DrawerLayout drawerLayout;
	private View menuDrawer;
	private Button menuSettingsButton;
	private Button menuNavigateButton;
	private Button menuGpsNotesButton;
	private Button menuShiftButton;
	private Button menuSearchButton;
	private Button menuCustomizeListButton;
	private Button menuCallStoreListButton;
	public OnClickListener menuGpsNotesClickListener;
	public OnClickListener menuShiftClickListener;
	public OnClickListener menuSearchClickListener;
	public OnClickListener orderSummaryClickListener;
    public OnClickListener menuDownloadMapClickListener;


	private boolean isGeofenceServiceRunning() {
		return GeofenceIntentService.isRunning;
	 /*  ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (GeofenceIntentService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }*/
	}

	@Override
	public void onResume() {
		super.onResume();


		if (sharedPreferences.getString("storePhoneNumber", "").length() > 0) {
			menuCallStoreListButton.setVisibility(View.VISIBLE);
		} else {
			menuCallStoreListButton.setVisibility(View.GONE);
		}

		String storeAddress = sharedPreferences.getString("storeAddress", "");
		if (storeAddress.length() > 0) {
			menuNavigateButton.setVisibility(View.VISIBLE);
		} else {
			menuNavigateButton.setVisibility(View.GONE);
		}


		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
			prefEditor.putBoolean("HasGooglePlay", false);
		} else {
			prefEditor.putBoolean("HasGooglePlay", true);
		}

		int count = dataBase.getUndeliveredOrderCount();
		if (count == 0) {
			outTheDoorButton.setVisibility(View.GONE);

		} else {
			outTheDoorButton.setVisibility(View.VISIBLE);
		}
		final int ordersThisShift = dataBase.getNumberOfOrdersThisShift();
		if (ordersThisShift > 0) {
			prefEditor.putBoolean("inActiveShift", true);
		}


		if (dataBase.getUndeliveredOrderCount() > 0
				&& sharedPreferences.getBoolean("pay_rate_location_aware", false)
				&& sharedPreferences.getBoolean("dual_wage", false)
				&& isGeofenceServiceRunning() == false
				&& GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
			Intent serviceIntent = new Intent(getApplicationContext(), GeofenceIntentService.class);
			serviceIntent.putExtra("init_store_geofence", true);
			startService(serviceIntent);
		}

	}


	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen_activity);
		prefEditor = sharedPreferences.edit();

		if (getSupportActionBar()!=null) getSupportActionBar().hide();

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		menuDrawer = findViewById(R.id.menu_drawer);
		menuSettingsButton = (Button) findViewById(R.id.settings_menu_button);
		menuNavigateButton = (Button) findViewById(R.id.navigate_menu_button);
		menuGpsNotesButton = (Button) findViewById(R.id.gps_notes_menu_button);
		menuShiftButton = (Button) findViewById(R.id.shift_menu_button);
		menuSearchButton = (Button) findViewById(R.id.search_menu_button);
		menuCustomizeListButton = (Button) findViewById(R.id.customize_list_menu_button);
		menuCallStoreListButton = (Button) findViewById(R.id.call_store_menu_button);
		menuSettingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), 0);
				drawerLayout.closeDrawers();
			}
		});
		menuNavigateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String storeAddress = sharedPreferences.getString("storeAddress", "");
				if (storeAddress.length() >= 0) {
					tools.navigateTo(storeAddress, HomeScreenActivity.this);
				} else {
					if (alreadyAskedStoreAddress == false) {
						showDialog(STORE_ADDRESS_FIRST);
						alreadyAskedStoreAddress = true;
					}
				}
			}
		});

        menuDownloadMapClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                startActivity(new Intent(getApplicationContext(), DownloadMapActivity.class));
                drawerLayout.closeDrawers();
            }
        };

		menuGpsNotesClickListener = new OnClickListener() {
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(), GpsNotes.class));
				drawerLayout.closeDrawers();
			}
		};
		menuGpsNotesButton.setOnClickListener(menuGpsNotesClickListener);
		menuShiftClickListener = new OnClickListener() {
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(), ShiftStartEndActivity.class));
				drawerLayout.closeDrawers();
			}
		};
		menuShiftButton.setOnClickListener(menuShiftClickListener);
		menuSearchClickListener = new OnClickListener() {
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(), ListAddressHistoryActivity.class));
				drawerLayout.closeDrawers();
			}
		};
		menuSearchButton.setOnClickListener(menuSearchClickListener);
		menuCustomizeListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(), Settings_ListOptions.class));
				drawerLayout.closeDrawers();
			}
		});
		menuCallStoreListButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				drawerLayout.closeDrawers();
				String phoneNumber = sharedPreferences.getString("storePhoneNumber", "");
				if (phoneNumber.length() < 1) {
					Toast.makeText(getApplicationContext(), R.string.missing_phone_number, Toast.LENGTH_LONG).show();
				} else {
					String uri = "tel:" + phoneNumber;
					Intent intent = new Intent(Intent.ACTION_DIAL);
					intent.setData(Uri.parse(uri));
					startActivity(intent);
				}
			}
		});

		startShift();

		if (sharedPreferences.getString("addressFilterComponents", "").length() < 2) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String bestProvider = locationManager.getBestProvider(criteria, false);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_ACCESS_LOCATION);
			}
			else {

                Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);//TODO: DEAL with null (if not google api's)
                if (lastKnownLocation == null) {
                    //TODO:Display error-Missing Google API's OR Location Disabled
                } else {
                    prefEditor.putFloat("centrPoint_lat", (float) lastKnownLocation.getLatitude());
                    prefEditor.putFloat("centrPoint_lng", (float) lastKnownLocation.getLongitude());
                    prefEditor.putString("centrPoint_lat_s", lastKnownLocation.getLatitude() + "");
                    prefEditor.putString("centrPoint_lng_s", lastKnownLocation.getLongitude() + "");
			/*	new WebServiceUpdateLocalePreferances(sharedPreferences,(float) lastKnownLocation.getLatitude(),(float) lastKnownLocation.getLongitude(),new WebServiceUpdateLocalePreferances.LocalityListener(){public void result(LocalityInfo info) {
					prefEditor.putString("addressFilterComponents", info.filter);
					prefEditor.commit();
				}}).lookup();*/
                }
            }
    	}
    	prefEditor.putFloat("dileveryRadius",0.1f);

    	
		prefEditor.commit();	
		try {
            streetList = StreetList.LoadState(getApplicationContext());
            if (StreetList.parentList.size()==0) {
                startUpThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            streetList = StreetList.LoadState(getApplicationContext());
                            if (StreetList.parentList.size() == 0) {
                                try {
                                    final MyGeoPoint g = streetList.getCurrentLocation();
                                    if (g != null) {
                                        // Spin endlessly until we can get the near by zip
                                        // codes from the Internet
                                        // presumably we don't have good Internet
                                        // connectivity and there is no point
                                        // in continuing
                                        do {
                                            streetList.findCloseByZipCodes();
                                            try {
                                                if (StreetList.zipCodes.size() == 0) {
                                                    Thread.sleep(10000);
                                                }
                                            } catch (final Exception e) {
                                            }
                                        } while (StreetList.zipCodes.size() == 0);
                                        StreetList.zipCodes.get(0).state = ZipCode.STATE_NEEDS_LOOKUP;
                                        //Now we have zip codes and can start the download the street names
                                        //						streetList.run();

                                    }
                                } catch (NoClassDefFoundError e) {
                                    e.printStackTrace();
                                    //If we don't have GeoPoint - Whatever they don't get street names now. better than a force close
                                }
                            }
                        } catch (NoClassDefFoundError e){
                            e.printStackTrace();
                            //If we don't have GeoPoint - Whatever they don't get street names now. better than a force close
                        }
                    }
                });
                startUpThread.start();
            }
		} catch (NoClassDefFoundError e){
            e.printStackTrace();
        }
		
		//Header Elements
		sortClickable    = (View)findViewById(R.id.sortClickable);
		mapClickable     = (View)findViewById(R.id.mapClcikable);
		sortUnderline    = (View)findViewById(R.id.sortUnderline);
		mapUnderline     = (View)findViewById(R.id.mapUnderline);
		
		sortClickable.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			setSelectedFragment(SORT);
		}});
		mapClickable.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			setSelectedFragment(MAP);
		}});
		
		//Footer Buttons
		orderSummaryButton = (View)findViewById(R.id.orderSummaryClickable);
		outTheDoorButton   = (View)findViewById(R.id.outTheDoorClickable);
		bankButton         = (View)findViewById(R.id.bankClickable);
		totalsButton	   = (View)findViewById(R.id.totalsClickable);
		addOrderButton     = (View)findViewById(R.id.addOrderClickable);
		moreMenuButton     = (View)findViewById(R.id.moreClickable);

        orderSummaryClickListener = new OnClickListener(){public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), OrderSummaryActivity.class));
        }};
        orderSummaryButton.setOnClickListener(orderSummaryClickListener);
		outTheDoorButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            if (sharedPreferences.getBoolean("TRIAL_OVER",false)==true){
                showDialog(TRIAL_OVER);
            } else {
                String onRoadRateString = sharedPreferences.getString("hourly_rate_on_road", "");

                if (sharedPreferences.getBoolean("switch_on_out_the_door", false)){
                    float onRoadRate = Tools.parseCurrency(onRoadRateString);
                    dataBase.setWage(onRoadRate, dataBase.getShift(dataBase.getCurShift()), DateTime.now());
                }

                final Intent myIntent = new Intent(getApplicationContext(), OutTheDoorActivity.class);
                myIntent.putExtra("startNewRun", DeliveryDroidBaseActivity.startNewRun);
                startActivityForResult(myIntent, 0);
            }
		}});
		bankButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			startActivity(new Intent(getApplicationContext(), BankTillDropActivity.class));
		}});
		totalsButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			startActivityForResult(new Intent(getApplicationContext(), TipHistoryActivity.class), 0);
		}});
		addOrderButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			if (sharedPreferences.getBoolean("TRIAL_OVER",false)==true){
                showDialog(TRIAL_OVER);
            } else {
                startActivityForResult(new Intent(getApplicationContext(), NewOrderActivity.class), 0);
            }
		}});
		moreMenuButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			if (drawerLayout.isDrawerOpen(menuDrawer)) {
				drawerLayout.closeDrawer(menuDrawer);
			} else {
				drawerLayout.openDrawer(menuDrawer);
			}
			
		}});
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
	//			return gestureListener.onTouch(v, event);
		//	} else {
				return false;
		//	}
		}};
		orderSummaryButton.setOnTouchListener(bluingToucher);
		
		fragmentContainer = (FrameLayout)findViewById(R.id.fragmentContainerFrame);
		setSelectedFragment(sharedPreferences.getInt("currentFragment", SORT));	
		
		// Gesture detection
      //  gestureDetector = new GestureDetector(new MyGestureDetector());
      //  gestureListener = new View.OnTouchListener() {
       //     public boolean onTouch(View v, MotionEvent event) {
      //          return gestureDetector.onTouchEvent(event);
       //     }
       // };
        sortClickable.setOnTouchListener(bluingToucher);
        mapClickable.setOnTouchListener(bluingToucher);
       // sortUnderline.setOnTouchListener(gestureListener);
       // mapUnderline.setOnTouchListener(gestureListener);
        orderSummaryButton.setOnTouchListener(bluingToucher);
        outTheDoorButton.setOnTouchListener(bluingToucher);
        bankButton.setOnTouchListener(bluingToucher);
        totalsButton.setOnTouchListener(bluingToucher);
        addOrderButton.setOnTouchListener(bluingToucher);
        //moreMenuButton.setOnTouchListener(bluingToucher);
      //  fragmentContainer.setOnTouchListener(gestureListener);
        
	}

	private int lastLastOrderDelta=-100;
	@SuppressWarnings("deprecation")
	private void startShift() {
    	final String odometerPay = sharedPreferences.getString("odometer_per_mile", "");
		Float odoPay = 0f;
		
		final int lastOrderDelta  = dataBase.getHoursSinceLastOrder();
		final int openOrders      = dataBase.getUndeliveredOrderCount();
		final int ordersThisShift = dataBase.getNumberOfOrdersThisShift();
		
		
		if (lastOrderDelta > 12 && ordersThisShift > 0 && openOrders==0 && lastOrderDelta != lastLastOrderDelta) {
			lastLastOrderDelta = lastOrderDelta;

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Starting a Shift?");
			alertDialogBuilder.setMessage(getString(R.string.Its_been)+" "+lastOrderDelta+" "+getString(R.string.hours_since_you));
			alertDialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				dataBase.setNextShift();
				startActivity(new Intent(getApplicationContext(),ShiftStartEndActivity.class));
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), getString(R.string.check_ourder_summary), Toast.LENGTH_LONG).show();
			}});
			alertDialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}});

			alertDialogBuilder.create().show();

		}


        final int extraTime = sharedPreferences.getInt("extraDays", 0);
        if ((dataBase.getCurShift() > (7+extraTime)) && getPackageName().contains("free")){
            if (extraTime == 0) {
                showDialog(REVIEW_FOR_MORE);
            } else {
                prefEditor.putBoolean("TRIAL_OVER",true);
                prefEditor.commit();
                showDialog(TRIAL_OVER);
            }
        }
	}
    
    
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case REVIEW_FOR_MORE:{
			return new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(
			"Trial Expired").setMessage("Your trial period with this app has expired. If you dont want to purchase the app yet you can click the write review button below and get to keep using the app for 10 more shifts.").
				setNeutralButton("Write Review", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.catglo.deliverydroidfree")); 
					prefEditor.putInt("extraDays", 10);
					prefEditor.commit();
					startActivity(marketIntent);
					finish();
				}}).
				setPositiveButton("Buy",
			new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.catglo.deliverydroid")); 
					startActivity(marketIntent);
					finish();	
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
					finish();	
				}}).create();
		}
		case TRIAL_OVER:{
			return new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(
			"Trial Expired").setMessage("Your trial period with this app has expired.").
				setPositiveButton("Buy",
			new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=com.catglo.deliverydroid")); 
					startActivity(marketIntent);
					finish();	
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
					finish();
				}}).create();
		}

		case STORE_ADDRESS_FIRST: {
			
			AlertDialog.Builder delBuilder = new AlertDialog.Builder(this);
			delBuilder.setIcon(R.drawable.icon);
			delBuilder.setIcon(R.drawable.icon).setTitle(R.string.you_need_store);
			delBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog, final int whichButton) {
								startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), 0);
							}
						}).setNegativeButton("No", null);
			return delBuilder.create();
			}		
		}
		return null;
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent ev){
//		return gestureDetector.onTouchEvent(ev);
//	}
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
   // private GestureDetector gestureDetector;
   // View.OnTouchListener gestureListener;
	public static boolean alreadyAskedStoreAddress=false;
//	static class MyGestureDetector extends SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            try {
//                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
//                    return false;
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences();
//                int curFrag = sharedPreferences.getInt("currentFragment", SORT);
//                // right to left swipe
//                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                	switch (curFrag){
//                //	case DETAILS: setSelectedFragment(SORT);
//               // 	break;
//                	case SORT: setSelectedFragment(MAP);
//                	break;
//                	}
//                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                	switch (curFrag){
//                //	case SORT: setSelectedFragment(DETAILS);
//                //	break;
//                	case MAP: setSelectedFragment(SORT);
//                	break;
//                	}
//                }
//            } catch (Exception e) {
//                // nothing
//            }
//            return false;
//        }
//
//    }
	
	
	//TODO: This seems like a hack way of handling when we start a new run, come up with something clean
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (resultCode) {
			case 300: {
				DeliveryDroidBaseActivity.startNewRun=true;
				break;
			}
			case 400: {
				finish();
				break;
			}
			case 500: {
				//Exit outTheDoor and start new Run
				DeliveryDroidBaseActivity.startNewRun=true;
				break;
			}
			case 501: {
				//Exit outTheDoor and do not start new Run
				DeliveryDroidBaseActivity.startNewRun=false;
				break;
			}		
		}// end switch
	}
	
	
	void setSelectedFragment(int index){
		sortUnderline.setVisibility(   View.INVISIBLE);
		mapUnderline.setVisibility(    View.INVISIBLE);
		Fragment fragment;
		FragmentTransaction transaction;
		switch (index){
			
			default:
			case SORT:
				menuCustomizeListButton.setVisibility(View.VISIBLE);
				sortUnderline.setVisibility(View.VISIBLE);
				fragment = new HomeScreen_ListFragmentDragDrop();//TODO: need a solution to pass gestureListener in
	            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerFrame, fragment).commit();
	            prefEditor.putInt("currentFragment", SORT);
	            prefEditor.commit();
			break;
			case MAP:
				menuCustomizeListButton.setVisibility(View.GONE);
				mapUnderline.setVisibility(View.VISIBLE);
				fragment = new HomeScreen_MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerFrame, fragment).commit();
	            prefEditor.putInt("currentFragment", MAP);
	            prefEditor.commit();
			break;
			
		}
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, SETTINGS, 0, getString(R.string.settings)).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, NEW_SHIFT, 0, getString(R.string.Shift)).setIcon(R.drawable.ic_menu_guy_clock);
		menu.add(0, NAV_TO_STORE, 0, getString(R.string.Nav_To_Store)).setIcon(R.drawable.ic_nav);
		menu.add(0, SEARCH_HISTORY, 0, getString(R.string.Search_Notes_Or_Addresses)).setIcon(R.drawable.ic_action_search);
		menu.add(0, GPS_NOTES, 0, getString(R.string.GPS_notes)).setIcon(android.R.drawable.ic_menu_compass);
	    menu.add(0, CUSTOMIZE_LIST, 0, "Customize List ");
		
		return true;
	} 

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if (sharedPreferences.getString("storePhoneNumber", "").length()>0){
			if (menu.findItem(CALL_STORE)==null){
				menu.add(0, CALL_STORE, 0, getString(R.string.call_store) ).setIcon(android.R.drawable.ic_menu_call);
			}
		} else {
			menu.removeItem(CALL_STORE);
		}
		return true;
	}

	
	/* Handles item selections */
	@SuppressWarnings("deprecation")
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case SETTINGS: {
				startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), 0);
				return true;
			}
			case NEW_SHIFT: {
				startActivity(new Intent(getApplicationContext(), ShiftStartEndActivity.class));
				return true;
			}
			case SEARCH_HISTORY: {
				startActivity(new Intent(getApplicationContext(), ListAddressHistoryActivity.class));
				return true;
			}
			case NAV_TO_STORE:
				//TODO: Error dialog box with link to settings
				String storeAddress = sharedPreferences.getString("storeAddress", "");
				if (storeAddress.length()>=0){
		    		tools.navigateTo(storeAddress, HomeScreenActivity.this);
				} else {
					if (alreadyAskedStoreAddress==false){
						showDialog(STORE_ADDRESS_FIRST);
						alreadyAskedStoreAddress=true;
					}
				}
			break;
			case CALL_STORE:
				String phoneNumber = sharedPreferences.getString("storePhoneNumber", "");
				if (phoneNumber.length()<1){
					Toast.makeText(getApplicationContext(), R.string.missing_phone_number, Toast.LENGTH_LONG).show();
				} else {
					 String uri = "tel:" + phoneNumber;
					 Intent intent = new Intent(Intent.ACTION_DIAL);
					 intent.setData(Uri.parse(uri));
					 startActivity(intent);
				}
				break;
			case GPS_NOTES:
				startActivity(new Intent(getApplicationContext(), GpsNotes.class));
				return true;
			case CUSTOMIZE_LIST:
				Intent i = new Intent(getApplicationContext(),Settings_ListOptions.class);
				startActivity(i);
				break;
		}
		return false;
	}
 
}