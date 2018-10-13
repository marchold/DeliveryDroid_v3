package catglo.com.deliverydroid;

import android.app.Application;

public class DeliveryDroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		JodaTimeAndroid.init(this);
	}
}