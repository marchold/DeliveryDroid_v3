package catglo.com.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Filterable;
import android.widget.ListAdapter;
import catglo.com.deliveryDatabase.AddressHistorySuggester;
import catglo.com.deliveryDatabase.AddressHistorySuggester.AddressResultListener;
import catglo.com.deliveryDatabase.AddressInfo;


public class AddressHistoryAutocomplete extends AppCompatAutoCompleteTextView {

	public AddressHistoryAutocomplete(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}
	
	public AddressHistoryAutocomplete(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	public AddressHistoryAutocomplete(Context context) {
		super(context);
		this.context = context;
	}
	
	public AddressHistoryAutocomplete(Context context, AttributeSet attrs, String prefKey) {
		super(context, attrs);
		this.context = context;
		this.prefKey = prefKey;
	}

	private TextWatcher textWatcher;
	private AddressHistorySuggester suggestor;
	Context context;
	private String prefKey;
	AddressInfo selectedAddress=null;

	AdapterView.OnItemClickListener itemSelectedListener2=null;
	AdapterView.OnItemClickListener itemSelectedListener;
	
	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener l){
		itemSelectedListener2 = l;
		super.setOnItemClickListener(itemSelectedListener);
	}
	
	
	
	@Override
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		super.setAdapter(adapter);
		itemSelectedListener = new AdapterView.OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (itemSelectedListener2!=null) itemSelectedListener2.onItemClick(arg0, arg1, arg2, arg3);
			@SuppressWarnings("unchecked")
			ArrayAdapter<AddressInfo> streets = (ArrayAdapter<AddressInfo>)arg0.getAdapter();
			selectedAddress = streets.getItem(arg2);
				
		}};
		super.setOnItemClickListener(itemSelectedListener);
	}
	
	public void startSuggestor(){
		textWatcher = new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count==1){
					suggestor.lookup(""+s);
				}
			}
		};
		
		//TODO: Address history suggester needs to return GPS coordinates as well as the address so we can save them in the settings and use them for geol
		suggestor = new AddressHistorySuggester(context,prefKey, new AddressResultListener(){public void onResultAddress(final ArrayList<AddressInfo> results) {post(new Runnable(){public void run(){
			ArrayAdapter<AddressInfo> streets = new ArrayAdapter<AddressInfo>(context, android.R.layout.simple_dropdown_item_1line, results);
			AddressHistoryAutocomplete.this.setAdapter(streets);
			AddressHistoryAutocomplete.this.showDropDown();
		}});}});
		

    	addTextChangedListener(textWatcher);
	}

	public void saveResult(AddressInfo value) {
		suggestor.saveResult(value);
	}

	public AddressInfo getSelectedAddress() {
		return selectedAddress;
	}
	
}
