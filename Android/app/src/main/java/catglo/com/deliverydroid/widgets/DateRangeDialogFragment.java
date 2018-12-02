package catglo.com.deliverydroid.widgets;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

public class DateRangeDialogFragment extends DialogFragment {

	private View startTab;
	private View endTab;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private boolean hasNavigated;
	private View okButton;

	public DateRangeDialogFragment() {
		// TODO Auto-generated constructor stub
	}

	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.date_range_dialog_new, null);
  
        hasNavigated = false;
        
        startTab = view.findViewById(R.id.startDateTab);
        endTab   = view.findViewById(R.id.endDateTab);
        
        startDatePicker = (DatePicker)view.findViewById(R.id.datePicker1);
        endDatePicker = (DatePicker)view.findViewById(R.id.datePicker2);
        
        Bundle args = getArguments();
        DateTime start = (DateTime)args.getSerializable("start");
        DateTime stop  = (DateTime)args.getSerializable("end");
        startDatePicker.init(start.getYear(), start.getMonthOfYear()-1, start.getDayOfMonth(), new OnDateChangedListener(){
        	public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}});
        endDatePicker.init(stop.getYear(), stop.getMonthOfYear()-1, stop.getDayOfMonth(), new OnDateChangedListener(){
        	public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}});
        
   
        
        startTab.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
        	showStart();
		}});
        endTab.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
        	showEnd();
		}});
        alertDialogBuilder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (hasNavigated){
	        		DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
	        		MutableDateTime start = new MutableDateTime();
	        		MutableDateTime stop = new MutableDateTime();
	        	
	        		start.setMonthOfYear(startDatePicker.getMonth()+1);
	        		start.setYear(startDatePicker.getYear());
	        		start.setDayOfMonth(startDatePicker.getDayOfMonth());
	        		
	        		stop.setMonthOfYear(endDatePicker.getMonth()+1);
	        		stop.setYear(endDatePicker.getYear());
	        		stop.setDayOfMonth(endDatePicker.getDayOfMonth());
	        		
	        		dialog.dismiss();
	        		
	        		activity.getTools().onDateRangeDialogClosed(start, stop);
	        	}else {
	        		showEnd();
	        	}
			}
		});
        
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
      
        
        alertDialogBuilder.setView(view);      
        final AlertDialog dialog = alertDialogBuilder.create();
        return dialog; 
    }
	
	void showStart(){
		startTab.setBackgroundColor(getResources().getColor(R.color.android_blue));
		endTab  .setBackgroundColor(getResources().getColor(R.color.transparent));
		startDatePicker.setVisibility(View.VISIBLE);
		endDatePicker.setVisibility(View.GONE);
		
	}
	
	
	void showEnd(){
		startTab.setBackgroundColor(getResources().getColor(R.color.transparent));
		endTab  .setBackgroundColor(getResources().getColor(R.color.android_blue));
		startDatePicker.setVisibility(View.GONE);
		endDatePicker.setVisibility(View.VISIBLE);
		hasNavigated = true;


	}
}
