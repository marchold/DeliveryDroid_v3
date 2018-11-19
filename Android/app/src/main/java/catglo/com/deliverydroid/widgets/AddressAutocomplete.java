package catglo.com.deliverydroid.widgets;


import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.ArrayAdapter;


import catglo.com.api.GoogleAddressSuggester;
import catglo.com.deliveryDatabase.AddressInfo;
import catglo.com.deliveryDatabase.AddressSuggester;
import catglo.com.deliveryDatabase.DataBase;


import java.util.ArrayList;


public class AddressAutocomplete extends AppCompatAutoCompleteTextView {

	private TextWatcher textWatcher;
	private AddressSuggester suggestor;
	Context context;
	
	public AddressAutocomplete(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	public void startSuggestor(DataBase dataBase){
		textWatcher = new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count==1){
					suggestor.lookup(""+s);
				}
			}
		};
        suggestor = new AddressSuggester(context,dataBase,new GoogleAddressSuggester.AddressListListener() {
            @Override
            public void commit(ArrayList<AddressInfo> addressList, String searchString) {
                try {
                    ArrayAdapter<AddressInfo> streets = new ArrayAdapter<AddressInfo>(context, android.R.layout.simple_dropdown_item_1line, addressList);
                    AddressAutocomplete.this.setAdapter(streets);
                    try {
                        AddressAutocomplete.this.showDropDown();
                    } catch (Exception e){
                        //I was getting a google play crash because this was firing after the activity closed
                        e.printStackTrace();
                    }
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                    //Im guessing this happens when the result comes back after the activity is not using the view or finished.
                }
            }});
    	addTextChangedListener(textWatcher);
	}
	
}
