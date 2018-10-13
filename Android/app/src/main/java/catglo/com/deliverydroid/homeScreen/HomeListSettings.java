package catglo.com.deliverydroid.homeScreen;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import catglo.com.deliverydroid.R;

public class HomeListSettings extends PreferenceActivity {


	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.home_list_view_settings);
	}

	

}
