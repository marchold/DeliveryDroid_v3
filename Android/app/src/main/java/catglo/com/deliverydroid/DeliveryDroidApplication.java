package catglo.com.deliverydroid;

import android.app.Application;
import android.util.Log;
import net.danlew.android.joda.JodaTimeAndroid;

public class DeliveryDroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		JodaTimeAndroid.init(this);

	}
}