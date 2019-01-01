package catglo.com.deliverydroid

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import catglo.com.deliverydroid.homeScreen.DownloadableMap

enum class MapDownloadOption {
    none,
    torrent,
    download,
    create
}

class Settings(context: Context) {

    fun useCurvedScreenTouchOverlay() : Boolean {
        return prefs.getBoolean("curved_screen_overlay",false);
    }

    fun omitTelFromPhoneNumbers() : Boolean {
        return prefs.getBoolean("no_tel_hack",false);
    }

    fun addMapDownload(map: DownloadableMap) {
        HashSet<String>(prefs.getStringSet("MapDownloadFiles",HashSet<String>())).let {
            it.add(map.path)
            prefs.edit().run {
                putStringSet("MapDownloadFiles",it)
                apply()
            }
        }
    }

    fun removeMapDownload(map: DownloadableMap) {
        HashSet<String>(prefs.getStringSet("MapDownloadFiles",HashSet<String>())).let {
            it.remove(map.path)
            prefs.edit().run {
                putStringSet("MapDownloadFiles",it)
                apply()
            }
        }
    }


    fun mapDownloads() : Set<String> {
        return prefs.getStringSet("MapDownloadFiles",HashSet<String>())
    }


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