package catglo.com.deliverydroid.settings;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import androidx.preference.EditTextPreference;

public class SettingsEmailMe extends EditTextPreference {

	Context	context;

	public SettingsEmailMe(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void onClick() {
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "goblets@gmail.com" });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Delivery Driver Feedback");
		emailIntent.putExtra(Intent.EXTRA_TEXT, "");
		
		//Environment.getExternalStorageDirectory()+"/dr_log"+".txt"
		//if (getSharedPreferences().getBoolean("generateDevLog", false)==true)
        {
			Uri uri = Uri.fromFile(new File("sdcard/dd-log.txt"));
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		
		
		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

}