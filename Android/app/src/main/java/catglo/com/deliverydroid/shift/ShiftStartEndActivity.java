package catglo.com.deliverydroid.shift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;


import java.text.DecimalFormat;

public class ShiftStartEndActivity extends ShiftStartEndBaseActivity {
	protected TextView startODO;
	protected TextView endODO;
	protected TextView moneyCollected;
	protected TextView odoTotal;


	@Override
    public void updateUI(){
		super.updateUI();

		DecimalFormat df = new DecimalFormat("0000000");
		
		int recientOdometerValue = dataBase.getMostRecientOdomenterValue();
		if (shift.odometerAtShiftStart==0){
			shift.odometerAtShiftStart = recientOdometerValue;
		}
		if (shift.odometerAtShiftEnd < shift.odometerAtShiftStart){
			shift.odometerAtShiftEnd = shift.odometerAtShiftStart;
		}
		
		startODO.setText(df.format(shift.odometerAtShiftStart));
		endODO.setText(df.format(shift.odometerAtShiftEnd));
		
		if (tips.deliveries==0){
			endThisShift.setVisibility(View.GONE);
		} else {
			endThisShift.setVisibility(View.VISIBLE);
		}
		
		moneyCollected.setText(Tools.getFormattedCurrency(tips.payed));
		
		odoTotal.setText(""+(shift.odometerAtShiftEnd-shift.odometerAtShiftStart));
		
		if (dataBase.isTodaysShift(shift)==false) {
			endThisShift.setVisibility(View.GONE);
		}
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState, R.layout.start_end_shift_new);
		
		startODO = (TextView) findViewById(R.id.startingOdometer);
		endODO = (TextView) findViewById(R.id.endingOdometer);
		odoTotal = (TextView)findViewById(R.id.totalMilesDriven);
		
		startODO.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(),OdometerEntryActivity.class);
			i.putExtra("startValue", true);
			i.putExtra("ID", shift.primaryKey);
			startActivity(i);
		}});
		endODO.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(),OdometerEntryActivity.class);
			i.putExtra("startValue", false);
			i.putExtra("ID", shift.primaryKey);
			startActivity(i);
		}});
		View odometerLayout = findViewById(R.id.odometerLayout);
		//View odometerButton = findViewById(R.id.odometerClickable);
		if (sharedPreferences.getBoolean("track_odometer", true)){
			odometerLayout.setVisibility(View.VISIBLE);
			//odometerButton.setVisibility(View.GONE);  //I took out this tab bar item for now
		} else {
			odometerLayout.setVisibility(View.GONE);
			//odometerButton.setVisibility(View.GONE);
		}
		
		moneyCollected = (TextView)findViewById(R.id.moneyCollected);
		
		findViewById(R.id.deleteShiftClickable).setOnClickListener(new OnClickListener(){public void onClick(View v) {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShiftStartEndActivity.this);
			alertDialogBuilder.setTitle("Delete shift?");
			alertDialogBuilder.setMessage("Are you sure you want to delete this shift?");
			alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				dataBase.deleteShift(whichShift);
				dialog.dismiss();
				updateUI();
			}});
			alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}}).show();
			
		}});
		
		endThisShift =  findViewById(R.id.newShiftClickable);
		
		endThisShift.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			dataBase.setNextShift();
			updateUI();
		}});
		findViewById(R.id.timeClockClickable).setOnClickListener(new OnClickListener(){public void onClick(View v) {
			Intent intent = new Intent(getApplicationContext(),ShiftStartEndDayActivity.class);
			intent.putExtra("ID", whichShift);
			startActivity(intent);
		}});
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DELETE_SHIFT:
			return new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(
					R.string.deleteThisShift).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int whichButton) {
						//Log.i("Delivery Driver", "User Y/N Delete Order");
						int newShift = dataBase.getPrevoiusShiftNumber(whichShift);
						dataBase.deleteShift(whichShift);
						whichShift=newShift;
						updateUI();
					}
				}).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int whichButton) {
	
						/* User clicked Cancel so do some stuff */
					}
				}).create(); 
		}
		return null;
	}
	
	
}
