package catglo.com.deliverydroid.shift;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import android.widget.TimePicker;
import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;

import catglo.com.deliverydroid.Utils;


public class ShiftStartEndEditWage extends DeliveryDroidBaseActivity {

	private TimePicker timePicker;
	private EditText payRate;
	private int whichShift;
	private long whichWage;
	private Shift shift;
	private Wage wage;
	boolean saveShiftTime;
	private View deleteButton;
	private View backButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_end_shift_edit_wage);
		
		Intent intent = getIntent();
		whichShift = intent.getIntExtra("shiftID", -1);
		whichWage = intent.getLongExtra("wageID", -1);
		

		timePicker = (TimePicker)findViewById(R.id.timePicker1);
		payRate = (EditText)findViewById(R.id.preferedDeliveryRadius);
		deleteButton = findViewById(R.id.deleteButton);
		backButton = findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			finish();
		}});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		shift = getDataBase().getShift(whichShift);
		wage = getDataBase().getWage(whichWage);
		saveShiftTime = getDataBase().isWageFirstInShift(wage,shift);
		
		
		timePicker.setCurrentHour(wage.startTime.getHourOfDay());
		timePicker.setCurrentMinute(wage.startTime.minuteOfHour().get());
		
		//TODO: Check the next wage transition and if it exists don't let the time picker scroll past it.
		
		payRate.setText(Utils.getFormattedCurrency(wage.wage));
		
	//	if (saveShiftTime){
		deleteButton.setVisibility(View.INVISIBLE);
	/*	} else {
			deleteButton.setVisibility(View.VISIBLE);
			deleteButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
				
				DialogFragment dialog = new DialogFragment(){
					@Override
				    public Dialog onCreateDialog(Bundle savedInstanceState) {
				        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				        alertDialogBuilder.setTitle("Delete pay rate?");
				        alertDialogBuilder.setMessage("Are you sure you want to remove this payment transition?");
				        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				        	
				            dialog.dismiss();
				            finish();
				        }});
				        alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				            dialog.dismiss();
				        }});
				        return alertDialogBuilder.create();
				    }
				};
				dialog.show(getSupportFragmentManager(), "confirm_delete_pay");
			}});
		}*/
		
	}
	
	@Override
	public void onPause(){
		
		wage.wage = Utils.parseCurrency(payRate.getText().toString());
	
		wage.startTime.setHourOfDay(timePicker.getCurrentHour());
		wage.startTime.setMinuteOfHour(timePicker.getCurrentMinute());
		getDataBase().saveWage(wage, shift);
		
		if (saveShiftTime){
			shift.startTime = wage.startTime;
			getDataBase().saveShift(shift);
		}
		
		super.onPause();
		
	}

}
