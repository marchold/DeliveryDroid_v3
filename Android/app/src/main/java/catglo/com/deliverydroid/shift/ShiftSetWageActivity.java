package catglo.com.deliverydroid.shift;

import android.content.Intent;
import android.os.Bundle;


import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity;
import catglo.com.deliverydroid.R;
import catglo.com.widgets.ButtonPadFragment;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShiftSetWageActivity extends DeliveryDroidBaseActionBarActivity implements ButtonPadFragment.ButtonPadNextListener {
    public int whichShift;
    public Shift shift;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_set_wage_activity);

        Intent intent = getIntent();
        int id = intent.getIntExtra("ID", -1);
        if (id == -1) {
            whichShift = dataBase.getCurShift();
        } else {
            whichShift = id;
        }
        shift = dataBase.getShift(whichShift);
    }

    @Override
    public void onNextButtonPressed() {
        finish();
    }
}
/*	private ButtonPadFragment wagePad;
	private ArrayList<String> wageHistory;
	private int whichShift;
	private Shift shift;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        Intent intent = getIntent();
		int id = intent.getIntExtra("ID", -1);
		if (id == -1){
			whichShift = dataBase.getCurShift();
		} else {
			whichShift = id;
		}
		shift = dataBase.getShift(whichShift);
		
  //      wagePad = new ButtonPadView(this, null);
		setContentView(wagePad);

        String string = getString(R.string.Set_new_pay_rate);
	//	wagePad.setText(string);

		wageHistory = dataBase.wageHistory();		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line,wageHistory);
		if (adapter!=null) {
			wagePad.list.setAdapter(adapter);
			wagePad.list.setVisibility(View.VISIBLE);
		}
		
		wagePad.next.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			try {
				
				String wageString = wagePad.edit.getEditableText().toString();
				if (wageString.length()>0){
					float wage = Tools.parseCurrency(wageString);
					DecimalFormat df = new DecimalFormat("#.##");
					
					if (dataBase.isTodaysShift(shift)){
						Wage currentWage = dataBase.currentWage();
						if (df.format(wage).equalsIgnoreCase(df.format(currentWage.wage))==false){
							dataBase.setWage(wage,shift,DateTime.now());
						}
					} else {
						throw new IllegalStateException("set wage for past shift not supported");
					}
					
				}
				finish();
			} catch (NumberFormatException e){
				e.printStackTrace();
			};
		}});
    }
	

}*/

