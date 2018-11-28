package catglo.com.deliverydroid

import android.app.Application
import android.util.Log
import net.danlew.android.joda.JodaTimeAndroid
import org.mapsforge.map.layer.cache.TileCache

import java.util.ArrayList

class DeliveryDroidApplication : Application() {

    val tileCaches: List<TileCache> = ArrayList()
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    val notificationChannelId = "my_channel_01"


}