package catglo.com.deliverydroid.shift;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;


public class ShiftStartEndBaseActivity extends DeliveryDroidBaseActivity implements Dialog.OnDismissListener {
	protected static final int SETTINGS = 1;
	protected static final int DELETE_SHIFT = 2;
	protected static final int ESTIMATE_TIME = 3;
	protected TextView startTime;
	protected TextView endTime;
	protected Shift shift;
	//protected InputMethodManager imm;
	int whichShift;
	protected View endThisShift;
	protected TipTotalData tips;
	protected TextView deliveries;
	protected TextView hoursWorked;
	protected TextView currentShiftNumber;
	
	@Override
	public void onPause(){
		//dataBase.saveShift(shift);
		super.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateUI();
	}
	
	public void updateUI(){
		shift = dataBase.getShift(whichShift);
		
		if (shift.endTime.getMillis()==0 || shift.startTime.getMillis()==0){
			dataBase.estimateShiftTimes(shift);
		}
		
		tips = dataBase.getTipTotal(getApplicationContext(), "Payed != -1 AND Shift="+whichShift,
				                                             "WHERE shifts.ID="+whichShift);
		
		startTime.setText(Tools.getFormattedTimeDay(shift.startTime.toGregorianCalendar()));
		endTime.setText(Tools.getFormattedTimeDay(shift.endTime.toGregorianCalendar()));
		
		deliveries.setText(tips.deliveries+"");
		
		
		
		hoursWorked.setText("---");
		
		float t1 =  shift.endTime.getMillis();
		float t2 = shift.startTime.getMillis();
		float total = t1-t2;
		total = total/3600000f;
		hoursWorked.setText(String.format("%.2f",total));
	}
	
	protected int id;
	public void onCreate(Bundle savedInstanceState,int contentLayout){
		super.onCreate(savedInstanceState);
		setContentView(contentLayout);
		
		
		Intent intent = getIntent();
		id = intent.getIntExtra("ID", -1);
		if (id == -1){
			whichShift = dataBase.getCurShift();
		} else {
			whichShift = id;
		}
		
		startTime = (TextView) findViewById(R.id.shiftStartTime);
		endTime = (TextView) findViewById(R.id.shiftEndTime);
		
	
		deliveries = (TextView) findViewById(R.id.deliveries);
		
		hoursWorked = (TextView)findViewById(R.id.hoursWorked);
		
		
		currentShiftNumber = (TextView)findViewById(R.id.currentShiftNumber);
		currentShiftNumber.setText(""+whichShift);
		
		//imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		//imm.hideSoftInputFromWindow(startTime.getWindowToken(), 0);
		//imm.hideSoftInputFromWindow(endTime.getWindowToken(), 0);
		
		
		//imm.hideSoftInputFromWindow(startODO.getWindowToken(), 0);


		startTime.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			tools.showTimeSliderDialog(startTime,shift.startTime, ShiftStartEndBaseActivity.this);
		}});
		endTime.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			tools.showTimeSliderDialog(endTime,shift.endTime,ShiftStartEndBaseActivity.this);
		}});

		
		//OK Button
		OnClickListener doneClickListener = new OnClickListener(){public void onClick(View v) {
			finish();
		}};
		findViewById(R.id.doneButton).setOnClickListener(doneClickListener);
		findViewById(R.id.backButton).setOnClickListener(doneClickListener);
		
	}
	
	

	public void onDismiss(DialogInterface dialog) {
		dataBase.saveShift(shift);
		updateUI();
	}
}
