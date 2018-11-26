package catglo.com.deliverydroid

import android.app.backup.BackupManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import catglo.com.MapFileSharingService
import catglo.com.deliveryDatabase.DataBase


open class DeliveryDroidBaseActivity : AppCompatActivity(), Tooled , ServiceConnection {

    var mapSharingServce : MapFileSharingService? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        mapSharingServce = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        (service as MapFileSharingService.LocalBinder)?.let {
            mapSharingServce = it.getService()
        }
    }

    var tools = Utils()
    lateinit var dataBase: DataBase
    lateinit var sharedPreferences: SharedPreferences

    override fun getUtils(): Utils {
        return tools
    }

    private var isBound = false
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        dataBase = tools.create(this)
    }

    override fun onResume() {
        super.onResume()
        val b = BackupManager(this)
        b.dataChanged()
        if (Settings(this).mapDownloadOption == MapDownloadOption.torrent) {
            val serviceIntent = Intent(this, MapFileSharingService::class.java)
            startService(serviceIntent)
            bindService(serviceIntent,this, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (isBound)
        {
            isBound = false
            unbindService(this)
            mapSharingServce = null
        }
    }

    companion object {
        var startNewRun = true
    }
}
