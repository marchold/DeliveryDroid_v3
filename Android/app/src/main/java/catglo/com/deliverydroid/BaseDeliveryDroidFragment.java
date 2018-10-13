package catglo.com.deliverydroid;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View.OnTouchListener;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;


import java.util.ArrayList;

public abstract class BaseDeliveryDroidFragment extends Fragment {
	protected DataBase dataBase;
	protected SharedPreferences sharedPreferences;
	protected ArrayList<Order> orders;
	protected OnTouchListener gestureListener;
	protected Editor prefEditor;
	private BroadcastReceiver broadcastReceiver;
	
	public BaseDeliveryDroidFragment() {
		super();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefEditor = sharedPreferences.edit();
	}
	
	//Not sure if we need this, maybe remove it?
	@Override
	public void onHiddenChanged(boolean isHidden){
		if (isHidden==false){
			updateUI();
		}
	}
	
	abstract protected void updateUI();
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	synchronized public void onResume(){
		super.onResume();
		if (dataBase == null) {
        	dataBase = new DataBase(getActivity().getApplicationContext());
        	dataBase.open();
        }
		broadcastReceiver = new BroadcastReceiver() {public void onReceive(Context context, Intent intent) {
    		updateUI();
    	}};
    	updateUI();
    	getActivity().registerReceiver(broadcastReceiver, new IntentFilter("com.catglo.deliverydroid.DBCHANGED"));
	}
	
	synchronized public void onPause(){
		super.onPause();
		getActivity().unregisterReceiver(broadcastReceiver);
		dataBase.close();
		dataBase=null;
	}
	
		
}
