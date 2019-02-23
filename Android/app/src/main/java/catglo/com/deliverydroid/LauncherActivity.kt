package catglo.com.deliverydroid

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import catglo.com.deliverydroid.homeScreen.HomeScreenActivity

class LauncherActivity : Activity() {

    override fun onResume() {
        super.onResume()
        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreferences.getBoolean("DatabaseOnSdcard",false)
            && checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),100)
        }
        else {
            finish()
            startActivity(Intent(this,HomeScreenActivity::class.java))
        }
    }
}
