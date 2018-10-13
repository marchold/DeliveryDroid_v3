package catglo.com.deliverydroid.shift;

import android.content.Intent;
import android.os.Bundle;


import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity;
import catglo.com.deliverydroid.R;

public class OdometerEntryActivity extends DeliveryDroidBaseActionBarActivity {
	public int dataBasePrimaryKey;
    public boolean isStartShift;
    public Shift shift;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        final Intent i = getIntent();
		dataBasePrimaryKey = i.getIntExtra("ID", -1);
		isStartShift = i.getBooleanExtra("startValue", true);
		
		dataBase.createShiftRecordIfNonExists();
		shift = dataBase.getShift(dataBasePrimaryKey);

		setContentView(R.layout.odometer_entry_activity);

		/*odoPad.setText(getString(R.string.Odometer_Reading));

		ArrayAdapter<String> adapter = dataBase.getOdometerPredtion();
		
		if (adapter!=null)
			odoPad.list.setAdapter(adapter);
		
		odoPad.list.setVisibility(View.VISIBLE);
		
		odoPad.next.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			try {
				int value = Integer.valueOf(odoPad.edit.getEditableText().toString());
				if (isStartShift){
					shift.odometerAtShiftStart = value;
				} else {
					shift.odometerAtShiftEnd = value;
				}
				dataBase.saveShift(shift);
				finish();
			} catch (NumberFormatException e){
				e.printStackTrace();
			};
		}});*/
    }
}