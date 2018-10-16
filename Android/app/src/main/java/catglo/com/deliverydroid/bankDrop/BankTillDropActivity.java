package catglo.com.deliverydroid.bankDrop;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;


import java.util.ArrayList;

public class BankTillDropActivity extends DeliveryDroidBaseActivity implements OnClickListener, TextWatcher{
	class DropRow {
		public EditText value;
		public Spinner kind;
		public ImageButton more;
	}
	ArrayList<DropRow> dropps = new ArrayList<DropRow>();
	private LinearLayout dropsContainer;
	private ArrayAdapter<String> adapter;
	private Editor prefEditor;
	private int dropRowCount;
	private EditText bankField;
	private EditText totalField;
	private EditText totalCash;
	private EditText totalCredit;
	private EditText totalCheck;
	private EditText totalEBT;
	private EditText myBank;
	private TextView totalType;
	private View backButton;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bank_till_drop);

        getSupportActionBar().hide();

        dropsContainer = (LinearLayout) findViewById(R.id.DropsContainer);
        bankField = (EditText) findViewById(R.id.hourlyPayRate); //Borrowed from store
        myBank = (EditText) findViewById(R.id.editText7);    //Drivers own money
        
        bankField.addTextChangedListener(this);
        myBank.addTextChangedListener(this);
        
        totalField = (EditText) findViewById(R.id.totalsField);
        totalType = (TextView) findViewById(R.id.whoOwesWho);
        totalCash  = (EditText) findViewById(R.id.totalCash);
        totalCredit  = (EditText) findViewById(R.id.totalCredit);
        totalCheck  = (EditText) findViewById(R.id.totalCheck);
        totalEBT  = (EditText) findViewById(R.id.totalEBT);
        
        Button clearValues = (Button)findViewById(R.id.setShiftTimesToOrderTimes);
        clearValues.setOnClickListener(new OnClickListener(){public void onClick(View v) {
        	prefEditor.putInt("DropRowCount", 1);
        	prefEditor.putInt("drop_kind_0",0);
        	prefEditor.putFloat("drop_val_0", 0);
        	prefEditor.putFloat("BankAmount", 0);
        	prefEditor.putFloat("MyMoney", 0);
        	prefEditor.putBoolean("justClearedBank", true);
        	prefEditor.commit();
        	dropsContainer.removeAllViews();
        	initDrops();
        }});
        initDrops();
        
        backButton = findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			finish();
		}});
     
	}
	
	
	void calculateTotal(){
		float total=0;
		float cashTotal=0;
		float creditTotal=0;
		float checkTotal=0;
		float ebtTotal=0;
		dropRowCount = sharedPreferences.getInt("DropRowCount", 1);
		if (dropRowCount<1)dropRowCount=1;
		
		try {
			total += new Float(bankField.getEditableText().toString());
		} catch (NumberFormatException e){}
		try {
			total -= new Float(myBank.getEditableText().toString());
		} catch (NumberFormatException e){}
		
		for (int i = 0; i < dropps.size() /*dropRowCount*/; i++){
			float val=0;
			try {
				val = new Float(dropps.get(i).value.getEditableText().toString());
			} catch (NumberFormatException e){};
			
			total -= val;
        	
        	switch (dropps.get(i).kind.getSelectedItemPosition()){
        	case 0: cashTotal += val;
        	break;
        	case 1: creditTotal += val;
        	break;
        	case 2: checkTotal += val;
        	break;
        	case 3: ebtTotal += val;
        	break;
        	}
        }
		if (total < -0.1) {
			totalType.setText(R.string.workOwsYou);
			totalField.setText(Tools.getFormattedCurrency(-total));
		} else if (total > 0.1){
			totalType.setText(R.string.youOweWork);
			totalField.setText(Tools.getFormattedCurrency(total));
		} else {
			totalField.setText(" ");		
		}
		totalCash.setText(Tools.getFormattedCurrency(cashTotal));
		totalCredit.setText(Tools.getFormattedCurrency(creditTotal));
		totalCheck.setText(Tools.getFormattedCurrency(checkTotal));
		totalEBT.setText(Tools.getFormattedCurrency(ebtTotal));
	}
	
	String formatMoney(Float value){
		if (value == 0)
			return "";
		return Tools.getFormattedCurrency(value);
	}
	
	void initDrops(){
		prefEditor = sharedPreferences.edit();
        dropRowCount = sharedPreferences.getInt("DropRowCount", 1);
        if (dropRowCount<1)dropRowCount=1;
        for (int i = 0; i < dropRowCount; i++){
        	addDropRow();
        	dropps.get(i).value.setText(formatMoney(sharedPreferences.getFloat("drop_val_"+i, 0)));
        	dropps.get(i).kind.setSelection(sharedPreferences.getInt("drop_kind_"+i, 0));
        }	
        bankField.setText(formatMoney(sharedPreferences.getFloat("BankAmount", 0)));
        myBank.setText(formatMoney(sharedPreferences.getFloat("MyMoney", 0)));
        calculateTotal();
	}
	
	public void commit(){
		prefEditor.putInt("DropRowCount",dropRowCount);
	    for (int i = 0; i < dropRowCount; i++){
	    	 try {
	    		 prefEditor.putFloat("drop_val_"+i,new Float(dropps.get(i).value.getEditableText().toString()));
	    	 } catch (NumberFormatException e){}
	    }
	    try {
	    	prefEditor.putFloat("BankAmount", new Float(bankField.getEditableText().toString()));
	    } catch (NumberFormatException e){};
	    try {
	    	prefEditor.putFloat("MyMoney"   , new Float(myBank.getEditableText().toString()));
	    } catch (NumberFormatException e){};
	    prefEditor.commit();
	}
	
	@Override
	public void onPause(){
		commit();
	    super.onPause();
	}
	
	private void addDropRow(){
		final LinearLayout row = (LinearLayout) getLayoutInflater().inflate(R.layout.drop_input_row, null);
	    dropsContainer.addView(row);
    	DropRow drop = new DropRow();
        drop.value = (EditText)row.findViewById(R.id.totalCredit);
        drop.value.addTextChangedListener(this);
        drop.value.setId((5000+dropRowCount));
        
        drop.kind = (Spinner)row.findViewById(R.id.deliveryAreaSpinner);
        drop.kind.setId((6000+dropRowCount));
        
        drop.more = (ImageButton)row.findViewById(R.id.imageButton1);
        String[] items = {getString(R.string.Cash),getString(R.string.Credit),getString(R.string.Check),getString(R.string.ebt)};
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	drop.kind.setAdapter(adapter);
        drop.more.setOnClickListener(this);
        for (int i = 0; i < dropps.size(); i++){
        	dropps.get(i).more.setVisibility(View.INVISIBLE);
        }
        final int index = dropps.size();
        dropps.add(drop);
        prefEditor.putInt("DropRowCount", index);
        drop.kind.setOnItemSelectedListener(new OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		prefEditor.putInt("drop_kind_"+index, dropps.get(index).kind.getSelectedItemPosition());
        		calculateTotal();
			}
        	public void onNothingSelected(AdapterView<?> arg0) {}}
        );
	}
	
	public void onClick(View v) {
    	addDropRow();
        prefEditor.putInt("DropRowCount", ++dropRowCount);
        prefEditor.commit();
	}
	
	public void afterTextChanged(Editable s) {
		calculateTotal();
	}
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
