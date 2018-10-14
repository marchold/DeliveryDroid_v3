package catglo.com.deliverydroid.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import catglo.com.deliverydroid.R;


public class Settings_PayRateDialog extends DialogPreference {

	public Settings_PayRateDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//to indicate to the super Preference class that you persist the preference value on your own.
		setPersistent(false);
		
		setDialogLayoutResource(R.layout.settings_pay_rate_dialog);
	}

}
