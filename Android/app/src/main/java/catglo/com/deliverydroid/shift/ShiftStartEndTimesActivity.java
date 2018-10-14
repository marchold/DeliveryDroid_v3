package catglo.com.deliverydroid.shift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class ShiftStartEndTimesActivity extends ShiftStartEndBaseActivity {

	private Button setShiftToTimesToOrderTimesButton;
	private Button hourlyPayRateAutocomplete;
	ArrayList<String> wageHistory;
	Wage currentWage;
	ArrayList<Wage> todaysWageTransitions;
	private ViewGroup hourlyPayRatesToday;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.start_end_shift_hours_worked);
		
		setShiftToTimesToOrderTimesButton = (Button)findViewById(R.id.setShiftTimesToOrderTimes);
	}

/*	class MyDialogFragment extends DialogFragment{
	    Context mContext;
	    public MyDialogFragment() {
	        mContext = getActivity();
	    }
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
	        alertDialogBuilder.setTitle("Really?");
	        alertDialogBuilder.setMessage("Are you sure?");
	        //null should be your on click listener
	        alertDialogBuilder.setPositiveButton("OK", null);
	        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }
	        });


	        return alertDialogBuilder.create();
	    }
	}*/
	
	
	@Override
    public void updateUI(){
		super.updateUI();
		String text;
		
		DateTime now = new DateTime();
		MutableDateTime dt = dataBase.firstOrderTimeForShift(shift.primaryKey);
		if (dt==null)dt=MutableDateTime.now();
		long timeInMillsFirst = dt.getMillis();
	
		long timeInMillsLast;
		try {
			timeInMillsLast = dataBase.lastOrderTimeForShift(shift.primaryKey).getMillis();
			/*
			 java.lang.RuntimeException: Unable to resume activity {com.catglo.deliverydroid/com.catglo.deliverydroid.ShiftStartEndTimesActivity}: java.lang.NullPointerException
			at android.app.ActivityThread.performResumeActivity(ActivityThread.java:2571)
			at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:2592)
			at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:1905)
			at android.app.ActivityThread.access$1500(ActivityThread.java:135)
			at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1054)
			at android.os.Handler.dispatchMessage(Handler.java:99)
			at android.os.Looper.loop(Looper.java:150)
			at android.app.ActivityThread.main(ActivityThread.java:4385)
			at java.lang.reflect.Method.invokeNative(Native Method)
			at java.lang.reflect.Method.invoke(Method.java:507)
			at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:849)
			at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:607)
			at dalvik.system.NativeStart.main(Native Method)
			Caused by: java.lang.NullPointerException
			at com.catglo.deliverydroid.ShiftStartEndTimesActivity.updateUI(ShiftStartEndTimesActivity.java:80)
			at com.catglo.deliverydroid.ShiftStartEndBaseActivity.onResume(ShiftStartEndBaseActivity.java:38)
			at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1242)
			at android.app.Activity.performResume(Activity.java:3949)
			at android.app.ActivityThread.performResumeActivity(ActivityThread.java:2561)
			... 12 more  
			 */
		} catch (NullPointerException e) {
			timeInMillsLast = dt.getMillis();
		}
	
		DateTime firstShift = new DateTime(timeInMillsFirst);
		DateTime lastShift = new DateTime(timeInMillsLast);
		
		Hours hoursAgoStart = Hours.hoursBetween(firstShift, now);
		Hours hoursAgoEnd = Hours.hoursBetween(lastShift, now);
		
		if (hoursAgoEnd.getHours()!=0 && hoursAgoStart.getHours()!=0 && dataBase.isTodaysShift(shift)){
	
			text = "Your first order was "+hoursAgoStart+" hours ago and your last order was "+hoursAgoEnd+" hours ago. Set as shift times ";
			setShiftToTimesToOrderTimesButton.setText(text);
		} 
		else {
			setShiftToTimesToOrderTimesButton.setVisibility(View.GONE);
		}
		
		/*
		wageHistory = dataBase.wageHistory();		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line,wageHistory);
		hourlyPayRateAutocomplete.setAdapter(adapter);
		*/
		
		hourlyPayRateAutocomplete = (Button)findViewById(R.id.hourlyPayRateAutocomplete);
		hourlyPayRateAutocomplete.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),ShiftSetWageActivity.class);
			intent.putExtra("ID",shift.primaryKey);
			startActivity(intent);
		}});
		if (!dataBase.isTodaysShift(shift)){
			hourlyPayRateAutocomplete.setVisibility(View.GONE);
		}
		
		
		currentWage = dataBase.currentWage();
		
		todaysWageTransitions = dataBase.wageTransitionsForShift(shift);
		hourlyPayRatesToday = (ViewGroup)findViewById(R.id.dayHoursLabels);
		hourlyPayRatesToday.removeAllViews();
		int count=0;
		for (final Wage wage : todaysWageTransitions){
			
			count++;
			View row = View.inflate(getApplicationContext(), R.layout.activity_shift_start_end_times_payment_rate_row, null);
			final TextView payRate = (TextView)row.findViewById(R.id.rate);
			payRate.setText(Tools.getFormattedCurrency(wage.wage));
			final TextView howLongAgo = (TextView)row.findViewById(R.id.date);
			
			Minutes minutesAgo = Minutes.minutesBetween(wage.startTime,now);
			if (minutesAgo.isLessThan(Minutes.minutes(60))) {
				if (minutesAgo.isLessThan(Minutes.minutes(1))) {
					howLongAgo.setText(R.string.Now);
				} else {
					howLongAgo.setText(minutesAgo.getMinutes()+"m ago");
				}
			}
			else if (minutesAgo.getMinutes()<60*12) {
				int minutes = minutesAgo.getMinutes();
				int hours = minutes/60;
				minutes -= (hours*60);
				howLongAgo.setText(hours+"h "+minutes+"m ago");
			}
			else {
				DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd HH:mm");
				howLongAgo.setText(wage.startTime.toString(fmt));
			}
			
		
			
			howLongAgo.setOnClickListener(new OnClickListener(){public void onClick(View v) {
				tools.showTimeSliderDialog(howLongAgo,wage.startTime,new Dialog.OnDismissListener(){public void onDismiss(DialogInterface dialog) {
					dataBase.saveWage(wage,shift);
				}});
			}});
			payRate.setOnClickListener(new OnClickListener(){public void onClick(View v) {
				DialogFragment dialog = new DialogFragment(){
					@Override
				    public Dialog onCreateDialog(Bundle savedInstanceState) {
						
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				        alertDialogBuilder.setTitle(R.string.Pay_rate);
				        Minutes minutesAgo = Minutes.minutesBetween(wage.startTime,DateTime.now());
				        alertDialogBuilder.setMessage(getString(R.string.Change_pay_rate_starting_at)+" "+wage.startTime.getHourOfDay()+":"+wage.startTime.getMinuteOfHour()+" ("+minutesAgo.getMinutes()+" "+getString(R.string.minutes_ago)+")");
				        
				        final EditText rateInput = new EditText(getApplicationContext());
				        rateInput.setText(Tools.getFormattedCurrency(wage.wage));
				        rateInput.setInputType(InputType.TYPE_CLASS_NUMBER);
				        
				        alertDialogBuilder.setView(rateInput);
				        
				        alertDialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				        	String value = rateInput.getText().toString();
				        	dataBase.saveWage(wage, shift);
				        	dialog.dismiss();
				        }});

				        
				        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				        	dialog.dismiss();
				        }});


				        return alertDialogBuilder.create();
					}
				};
				dialog.show(getFragmentManager(), "set_pay_rate");
				
			}});
			
			
			
			hourlyPayRatesToday.addView(row);
		}
		
	}
	
	@Override
	public void onPause(){
		/*
		String wageString = hourlyPayRateAutocomplete.getText().toString();
		if (wageString.length()>0){
			float wage = Float.parseFloat(wageString);
			DecimalFormat df = new DecimalFormat("#.##");
			if (df.format(wage).equalsIgnoreCase(df.format(currentWage.wage))==false){
				dataBase.setWage(wage,shift,DateTime.now());
			}
		}
		*/
		
		super.onPause();
	}

}
