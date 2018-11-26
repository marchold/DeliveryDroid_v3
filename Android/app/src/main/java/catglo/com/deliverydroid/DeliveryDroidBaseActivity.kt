package catglo.com.deliverydroid

import android.app.backup.BackupManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import catglo.com.deliveryDatabase.DataBase


open class DeliveryDroidBaseActivity : AppCompatActivity(), Tooled {

    var tools = Utils()
    var dataBase: DataBase? = null
    var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

    override fun getUtils(): Utils {
        return tools
    }

    interface OnKeyboardVisibilityListener {
        fun onVisibilityChanged(visible: Boolean)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBase = tools.create(this)
        bindService(Intent(),this,)
    }

    override fun onResume() {
        super.onResume()
        val b = BackupManager(this)
        b.dataChanged()
    }

    companion object {
        var startNewRun = true
    }
}
