package catglo.com.deliverydroid

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

enum class MapDownloadOption {
    none,
    torrent,
    download,
    create
}

class Settings(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var mapDownloadOption : MapDownloadOption
        get() = prefs.getInt("MapDownloadOption",0).let {
            if (it == 1) MapDownloadOption.torrent
            else if (it == 2) MapDownloadOption.download
            else if (it == 3) MapDownloadOption.create
            else MapDownloadOption.none
        }
        set(value) {
            prefs.edit().run {
                when (value)
                {
                    MapDownloadOption.torrent  -> putInt("MapDownloadOption",1)
                    MapDownloadOption.download -> putInt("MapDownloadOption",2)
                    MapDownloadOption.create   -> putInt("MapDownloadOption",3)
                    else -> putInt("MapDownloadOption",0)
                }
                apply()
            }
        }

}