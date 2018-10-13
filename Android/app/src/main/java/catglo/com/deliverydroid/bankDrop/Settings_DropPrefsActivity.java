package catglo.com.deliverydroid.bankDrop;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import catglo.com.deliverydroid.R;

public class Settings_DropPrefsActivity extends PreferenceFragment {
		
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_drops);
	}
	
}