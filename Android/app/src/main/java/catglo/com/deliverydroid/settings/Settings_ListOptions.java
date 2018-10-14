package catglo.com.deliverydroid.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.homeScreen.HomeScreen_ListFragmentDragDrop;

@SuppressWarnings("unused")
public class Settings_ListOptions extends Activity {
	Order order;
	View sampleTableRowView;
	private ViewGroup prieviewLayout;
	private SharedPreferences sharedPreferences;
	private OnSharedPreferenceChangeListener listener;

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_list_options);
		Intent i = this.getIntent();
		order = (Order) i.getSerializableExtra("order");
	
		if (order==null){
			order = new Order();
			order.address = "1234 State St. New Orleans LA 06890";
			order.cost = 9.99f;
			order.tipTotalsForThisAddress.averagePercentageTip = 10;	
		}
		
		order.tipTotalsForThisAddress.averagePercentageTip = 0.1f;
		order.tipTotalsForThisAddress.averageTip = 3.99f;
		order.tipTotalsForThisAddress.bestTip = 7;
		order.tipTotalsForThisAddress.deliveries = 21;
		order.tipTotalsForThisAddress.lastTip = 0.01f;
		order.tipTotalsForThisAddress.worstTip = 0;
		
		prieviewLayout = (ViewGroup)findViewById(R.id.sampleCellContainer);

        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sampleTableRowView = vi.inflate(R.layout.home_screen_table_row, null);
        prieviewLayout.addView(sampleTableRowView);

        //TODO: Read the prefs and set the values so they show when we launch from home screen

	}

	@Override
	public void onResume(){
		super.onResume();
		 sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        listener = new OnSharedPreferenceChangeListener() {public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
	        	updatePreview();
			}};
	        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	         
	        updatePreview();
	}
	@Override
	public void onPause(){
		super.onPause();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	
	
	void updatePreview(){
		HomeScreen_ListFragmentDragDrop.formatTableRowViewForOrder(
                sampleTableRowView,
                order,
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()),
                1);
	}
	

}
