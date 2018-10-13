package catglo.com.deliverydroid.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import catglo.com.deliverydroid.R;


public class Settings_ListOptions_Activity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.home_list_view_settings);
		
	
	}
	
}
