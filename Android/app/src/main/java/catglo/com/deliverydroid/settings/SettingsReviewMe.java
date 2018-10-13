package catglo.com.deliverydroid.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class SettingsReviewMe extends EditTextPreference {

	Context	context;

	public SettingsReviewMe(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void onClick() {
		final Intent marketIntent;
		marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:\"Marc Holder Kluver\"")); 
		context.startActivity(marketIntent);
	}

}