package catglo.com.deliverydroid;

import android.app.Application;
import android.util.Log;
import net.danlew.android.joda.JodaTimeAndroid;
import org.mapsforge.map.layer.cache.TileCache;

import java.util.ArrayList;

public class DeliveryDroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		JodaTimeAndroid.init(this);
	}

    //public ArrayList<TileCache> tileCaches = new ArrayList()
}