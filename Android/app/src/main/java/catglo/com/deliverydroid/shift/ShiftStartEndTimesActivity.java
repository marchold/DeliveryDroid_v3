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
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class ShiftStartEndTimesActivity extends DeliveryDroidBaseActivity {

	private Button setShiftToTimesToOrderTimesButton;
	private Button hourlyPayRateAutocomplete;
	ArrayList<String> wageHistory;
	Wage currentWage;
	ArrayList<Wage> todaysWageTransitions;
	private ViewGroup hourlyPayRatesToday;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_end_shift_hours_worked);

		setShiftToTimesToOrderTimesButton = (Button)findViewById(R.id.setShiftTimesToOrderTimes);
	}

/*
	
	//@Override
    public void updateUI(){
	//	super.updateUI();
		String text;
		
		DateTime now = new DateTime();
		MutableDateTime dt = dataBase.firstOrderTimeForShift(getShift().primaryKey);
		if (dt==null)dt=MutableDateTime.now();
		long timeInMillsFirst = dt.getMillis();
	
		long timeInMillsLast;
		try {
			timeInMillsLast = dataBase.lastOrderTimeForShift(getShift().primaryKey).getMillis();

		} catch (NullPointerException e) {
			timeInMillsLast = dt.getMillis();
		}
	
		DateTime firstShift = new DateTime(timeInMillsFirst);
		DateTime lastShift = new DateTime(timeInMillsLast);
		
		Hours hoursAgoStart = Hours.hoursBetween(firstShift, now);
		Hours hoursAgoEnd = Hours.hoursBetween(lastShift, now);
		
		if (hoursAgoEnd.getHours()!=0 && hoursAgoStart.getHours()!=0 && dataBase.isTodaysShift(getShift())){
	
			text = "Your first order was "+hoursAgoStart+" hours ago and your last order was "+hoursAgoEnd+" hours ago. Set as shift times ";
			setShiftToTimesToOrderTimesButton.setText(text);
		} 
		else {
			setShiftToTimesToOrderTimesButton.setVisibility(View.GONE);
		}
		

		
		hourlyPayRateAutocomplete = (Button)findViewById(R.id.hourlyPayRateAutocomplete);
		hourlyPayRateAutocomplete.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),ShiftSetWageActivity.class);
			intent.putExtra("ID", getShift().primaryKey);
			startActivity(intent);
		}});
		if (!dataBase.isTodaysShift(getShift())){
			hourlyPayRateAutocomplete.setVisibility(View.GONE);
		}
		
		
		currentWage = dataBase.currentWage();
		
		todaysWageTransitions = dataBase.wageTransitionsForShift(getShift());
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
					dataBase.saveWage(wage, getShift());
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
				        	dataBase.saveWage(wage, getShift());
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


		super.onPause();
	}
*/
}
