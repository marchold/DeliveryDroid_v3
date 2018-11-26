package catglo.com.deliverydroid.shift;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Utils;
import catglo.com.deliverydroid.widgets.MyScrollView;
import catglo.com.deliverydroid.widgets.MyScrollView.ScrollViewListener;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static catglo.com.deliveryDatabase.DataBase.*;

public class ShiftStartEndDayActivity extends DeliveryDroidBaseActivity {
	private int whichShift;
	private Shift shift;
	private LinearLayout daysLayout;
	private MyScrollView scrollView;
	private RelativeLayout calendarLayout;
	private Button payRateButton;
	private Button showRawDataButton;
	private Button clockInOutButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_end_shift_day_activity);
		
		
		Intent intent = getIntent();
		int id = intent.getIntExtra("ID", -1);

		if (id == -1){
			whichShift = TodaysShiftCount;
		} else {
			whichShift = id;
		}
		
		daysLayout = (LinearLayout)findViewById(R.id.dayHoursLabels);
		scrollView = (MyScrollView)findViewById(R.id.scrollView);
		calendarLayout = (RelativeLayout)findViewById(R.id.contentLayout);
		payRateButton = (Button)findViewById(R.id.payRateButton);
		showRawDataButton = (Button)findViewById(R.id.showRawDataButton);
		clockInOutButton = (Button)findViewById(R.id.clockInOutButton);
		
		findViewById(R.id.backButton).setOnClickListener(new OnClickListener(){public void onClick(View v) {
			finish();
		}});
		
		
	}
	TextView lastVisibleAmPmLabel;
	private ArrayList<Wage> todaysWageTransitions;
	
	
	@Override
	public void onResume(){
		super.onResume();
		updateUI();
	}
	
	private void updateUI(){
		shift = getDataBase().getShift(whichShift);

		todaysWageTransitions = getDataBase().wageTransitionsForShift(shift);
		Collections.sort(todaysWageTransitions,new Comparator<Wage>(){public int compare(Wage lhs, Wage rhs) {
			return lhs.startTime.compareTo(rhs.startTime);
		}});
		
		//We need to determine the range of hours to show. Starting at the shift start time (or the first order time if the shift start time is zero)
		//If the shift start time is after the first order time for the shift use the first order time
		MutableDateTime fisrtOrderTime = getDataBase().firstOrderTimeForShift(whichShift);
		MutableDateTime calendarStart;
		if (fisrtOrderTime != null && (shift.startTime.isBefore(600000000) || fisrtOrderTime.isBefore(shift.startTime))){
			calendarStart = new MutableDateTime(fisrtOrderTime);
		} else {
			calendarStart = new MutableDateTime(shift.startTime);
		}
		calendarStart.setMinuteOfHour(0);
		
		final float cellHeight = getResources().getDimension(R.dimen.hour_of_day_cell_height);
	
		final String noonTag = "noon";
		MutableDateTime calendarCurrent = new MutableDateTime(calendarStart);
		
		//in case its not the first time we draw this view remove all the stuff we added last time
		daysLayout.removeAllViews();
		while (calendarLayout.getChildCount()>1){
			calendarLayout.removeViewAt(calendarLayout.getChildCount()-1);
		}
		
		MutableDateTime calendarEnd = new MutableDateTime(shift.endTime);
		MutableDateTime lastOrderTime = getDataBase().lastOrderTimeForShift(whichShift);
		MutableDateTime lastWageTransition = null;
		MutableDateTime shiftEndGuess;
		if (todaysWageTransitions.size()>=1){
			lastWageTransition = todaysWageTransitions.get(todaysWageTransitions.size()-1).startTime;
		}
		
		if (lastOrderTime==null && lastWageTransition==null){
			//This is unlikely to happen because it would be for shifts before this feature was added that have no orders
			calendarEnd = new MutableDateTime(shift.startTime);
			shiftEndGuess = new MutableDateTime(shift.startTime);
			shiftEndGuess.add(Hours.hours(2));
			calendarEnd.add(Hours.hours(8));

		} else {
			
			if (lastOrderTime==null){
			//	if (calendarEnd==null){
			//		//calendarEnd==null, lastOrderTime==null, lastWageTransition!=null
			//		calendarEnd = new MutableDateTime(lastWageTransition);
			//	} else {
					//calendarEnd and lastWageTransition have values pick the greatest
					//calendarEnd!=null, lastOrderTime==null, lastWageTransition!=null
					if (lastWageTransition != null && lastWageTransition.isAfter(calendarEnd)){
						calendarEnd = new MutableDateTime(lastWageTransition);
			//		}
				}

				
			} else {
				if (lastWageTransition==null){
					//This happens for shifts added before this feature was added
					
					//if (calendarEnd==null){
					//	//calendarEnd==null, lastOrderTime!=null, lastWageTransition==null
					//	calendarEnd = new MutableDateTime(lastWageTransition);
					//} else
					{
						//calendarEnd!=null, lastOrderTime!=null, lastWageTransition==null
						if (lastOrderTime.isAfter(calendarEnd)){
							calendarEnd = new MutableDateTime(lastOrderTime);
						}
					}
					
				} else {
					//pick greatest of lastOrderTime and the shift end time
					//if (calendarEnd==null){
					//	//calendarEnd==null, lastOrderTime!=null, lastWageTransition!=null
					//	if (lastOrderTime.isAfter(lastWageTransition)){
					//		calendarEnd = new MutableDateTime(lastOrderTime);
					//	} else {
					//		calendarEnd = new MutableDateTime(lastWageTransition);
					//	}
					//} else 
					{
						//calendarEnd!=null, lastOrderTime!=null, lastWageTransition!=null
						if (lastWageTransition.isAfter(calendarEnd)){
							calendarEnd = new MutableDateTime(lastWageTransition);
						}
						if (lastOrderTime.isAfter(calendarEnd)){
							calendarEnd = new MutableDateTime(lastOrderTime);
						}
					}			
				}
			}
			shiftEndGuess = new MutableDateTime(calendarEnd);
			shiftEndGuess.add(Hours.hours(1));
			calendarEnd.add(Hours.hours(2));
		}
		int range = Hours.hoursBetween(calendarStart, calendarEnd).getHours();
		if (range < 8){
			calendarEnd.add(Hours.hours(8-range));
		}
		
		
		int hoursToShow = Hours.hoursBetween(calendarStart, calendarEnd).getHours();
		final TextView[] hourLabel        = new TextView[hoursToShow];
		final TextView[] amPmLabel        = new TextView[hoursToShow];
		final View    [] dayHourLabelCell = new View    [hoursToShow];
		
		
		
		//Now I need to go through the times and create rectangles for the known payment ranges
		int i = 0;
		for (final Wage wage: todaysWageTransitions){
			Log.i("WAGE",""+i+", "+wage.startTime);
			i++;
			
			
			float oneMinute = cellHeight/60.0f;
			Minutes topMin;
			if (i == 1){
				topMin = Minutes.minutesBetween( calendarStart,shift.startTime);
				Log.i("WAGE","shift.startTime="+shift.startTime);
			} else {
				topMin = Minutes.minutesBetween( calendarStart,wage.startTime);
				
				Log.i("WAGE","wage.startTime="+wage.startTime);
			}
			    
			Log.i("WAGE",i+",topMin="+topMin);
			
			Minutes bottomMin;
			
			if (getDataBase().isTodaysShift(shift)){
				if (todaysWageTransitions.size()==i){
					//If its the last wage transition the shift end goes to now
					bottomMin = Minutes.minutesBetween( calendarStart,shiftEndGuess);
					
					if (Minutes.minutesBetween( wage.startTime ,shiftEndGuess).getMinutes()<45) {
						bottomMin=topMin.plus(45);
					}
					
				} else {
					//If we have a next wage transition stop this one where that one starts
					Wage nextWage = todaysWageTransitions.get(i);
					bottomMin = Minutes.minutesBetween( calendarStart,nextWage.startTime);
				}
			} else {
				//For past shifts the shift end time is either the last order/delivery time for the shift or the shift end time which ever one is greater 
				if (lastOrderTime.isAfter(shift.endTime)){
					bottomMin = Minutes.minutesBetween( calendarStart,lastOrderTime);
				} else {
					bottomMin = Minutes.minutesBetween( calendarStart,shift.endTime);
				}
				
			}
		
			
			
			int top    = (int) (topMin.getMinutes()*oneMinute);
			int bottom = (int) (bottomMin.getMinutes()*oneMinute);
			int height = bottom - top;
			
			final float leftMargin = getResources().getDimension(R.dimen.hour_of_day_marker_width);
			View wageRangeOverlay = View.inflate(getApplicationContext(), R.layout.start_end_shift_day_overlay, null);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
			lp.setMargins((int) leftMargin, top, 0, 0);
			wageRangeOverlay.setLayoutParams(lp);
			
			TextView overlayLabel = (TextView)wageRangeOverlay.findViewById(R.id.textView1);
			overlayLabel.setText(Utils.getFormattedCurrency(wage.wage)+getString(R.string.Per_Hour));
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if ((i&1)==1){
				wageRangeOverlay.setBackgroundColor(getResources().getColor(R.color.pay_range_1));
			} else {
				wageRangeOverlay.setBackgroundColor(getResources().getColor(R.color.pay_range_2));
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			}
			overlayLabel.setLayoutParams(params);
			
			wageRangeOverlay.setOnClickListener(new OnClickListener(){public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),ShiftStartEndEditWage.class);
				intent.putExtra("shiftID", shift.primaryKey);
				intent.putExtra("wageID", wage.id);
				startActivity(intent);
			}});

			calendarLayout.addView(wageRangeOverlay);
			
		}
		
		
		for (i = 0; i < hoursToShow; i++){
			dayHourLabelCell[i] = View.inflate(getApplicationContext(), R.layout.start_end_shift_day_cell, null);
			hourLabel[i] = (TextView)dayHourLabelCell[i].findViewById(R.id.timeHourLabel);
			amPmLabel[i] = (TextView)dayHourLabelCell[i].findViewById(R.id.timeAmPmLabel);
			
			int hour = calendarCurrent.hourOfDay().get();
			
			if (i==0){
				amPmLabel[i].setVisibility(View.VISIBLE);
				lastVisibleAmPmLabel = amPmLabel[i];
			}
			if (hour==0 || hour==12){
				amPmLabel[i].setTag(noonTag);
			}
			if (hour!=0 && hour!=12){
				amPmLabel[i].setVisibility(View.GONE);
			} else {
				amPmLabel[i].setVisibility(View.VISIBLE);
			}
			
			boolean pm = false;
			if (hour>11){
				pm=true;
			}
			if (hour>12){
				hour-=12;
			}
			if (hour==0)hour=12;
			hourLabel[i].setText(""+hour);
			
			
			if (pm){
				amPmLabel[i].setText("PM");
			}else{
				amPmLabel[i].setText("AM");
			}
			
			final MutableDateTime calendarCurrentFinal = new MutableDateTime(calendarCurrent);
			final int index = i;
			dayHourLabelCell[i].setOnClickListener(new OnClickListener(){public void onClick(View v) {
				setClockOutTime();
			}});
			
			daysLayout.addView(dayHourLabelCell[i]);
			
			
			calendarCurrent.add(Hours.hours(1));
		}
		
		scrollView.setScrollListener(new ScrollViewListener(){public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
			int[] locationOnScreen = new int[2];
			scrollView.getLocationOnScreen(locationOnScreen);
			
			int distanceScrolledOffScreen = y-locationOnScreen[1];
			if (distanceScrolledOffScreen<=0){
				//Entire scroll view is visible
			} else {
				try {
					int topCellIndex = (int) (distanceScrolledOffScreen/cellHeight);
					
					if (lastVisibleAmPmLabel.getTag()!=noonTag){
						lastVisibleAmPmLabel.setVisibility(View.GONE);
					}
					lastVisibleAmPmLabel = amPmLabel[topCellIndex];
					lastVisibleAmPmLabel.setVisibility(View.VISIBLE);
				} catch (ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
	

		}});
		
		payRateButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),ShiftSetWageActivity.class);
			intent.putExtra("ID",shift.primaryKey);
			startActivity(intent);
		}});
		
		showRawDataButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),ShiftStartEndTimesActivity.class);
			intent.putExtra("ID",shift.primaryKey);
			startActivity(intent);
		}});
		if (getDataBase().isTodaysShift(shift)){
			clockInOutButton.setVisibility(View.VISIBLE);
			payRateButton.setVisibility(View.VISIBLE);
			clockInOutButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
				setClockOutTime();
			}});
		}else{
			clockInOutButton.setVisibility(View.INVISIBLE);
			payRateButton.setVisibility(View.INVISIBLE);
		}
		
	}
	static final int SET_CLOCK_OUT_DIALOG=1;
	
	void setClockOutTime(){
		ShiftEndTimeDialogFragment dialog = new ShiftEndTimeDialogFragment();
		Bundle args = new Bundle();
		args.putInt("shiftID", shift.primaryKey);
		dialog.setArguments(args);
		dialog.setOnCompletionListener(new Runnable(){public void run() {
			updateUI();
		}});
		dialog.show(getFragmentManager(), "TimePickerDialogFragment");
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	


}
