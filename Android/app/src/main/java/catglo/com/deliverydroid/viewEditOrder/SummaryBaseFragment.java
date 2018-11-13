package catglo.com.deliverydroid.viewEditOrder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import catglo.com.deliveryDatabase.*;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;


import java.util.Calendar;

abstract public class SummaryBaseFragment extends Fragment {

	protected TextView deliveryTime;
	protected TextView orderTime;
	protected TextView address;
	protected TextView notes;
	protected Order order;
	protected TextView price;
	protected TextView orderNumber;
	protected TextView phoneNumber;
	
	protected DataBase dataBase;
	protected SharedPreferences sharedPreferences;
	protected Editor prefEditor;
	
	abstract protected int getLayoutId();
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefEditor = sharedPreferences.edit();
	}
	
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutId(), null);
		
		orderNumber = (TextView) view.findViewById(R.id.totalCash);
		deliveryTime = (TextView) view.findViewById(R.id.OrderPlacedTime);
		orderTime = (TextView) view.findViewById(R.id.DeliveryTime);
		address = (TextView) view.findViewById(R.id.orderAddress);
		phoneNumber  = (TextView) view.findViewById(R.id.phoneNumber);
		
	    notes = (TextView) view.findViewById(R.id.OrderNotes);
        price = (TextView) view.findViewById(R.id.textView6);
     
		
		
		return view;
	}

	@Override
	public void onResume(){
		super.onResume();
	   
		int orderID = getArguments().getInt("DB Key");
		
		Log.i("CURSOR","order is ="+orderID);
		
		dataBase = new DataBase(getActivity().getApplicationContext());
    	dataBase.open();
    	order = dataBase.getOrder(orderID);
    	
		orderNumber.setText(order.number);
		
		phoneNumber.setText(order.phoneNumber);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(order.payedTime.getTime());
		
		deliveryTime.setText(String.format("%tl:%tM %tp", calendar, calendar, calendar, calendar, calendar, calendar, calendar));
        
		deliveryTime.setOnTouchListener(new OnTouchListener(){ public boolean onTouch(View arg0, MotionEvent arg1) {
			DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
			activity.tools.showTimeSliderDialog(deliveryTime,order.payedTime,null, false);
		    return true;
		}});
	        
        //Order Time
        calendar.setTimeInMillis(order.time.getTime());
		orderTime.setText(String.format("%tl:%tM %tp", calendar, calendar, calendar, calendar, calendar, calendar, calendar));
		orderTime.setOnTouchListener(new OnTouchListener(){ public boolean onTouch(View arg0, MotionEvent arg1) {
			DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
			activity.tools.showTimeSliderDialog(orderTime,order.time,null, false);
		    return true;
		}});
        
        notes.setText(order.notes);
        
		price.setText(Tools.getFormattedCurrency(order.cost));
			
	
			
			
	}
	
	
	

	@Override
	public void onPause(){
		super.onPause();
		
		dataBase.close();
		dataBase = null;
		
	}
	
}
