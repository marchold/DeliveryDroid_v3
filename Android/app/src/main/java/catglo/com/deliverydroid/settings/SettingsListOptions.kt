package catglo.com.deliverydroid.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import catglo.com.deliveryDatabase.Order
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.homeScreen.HomeScreen_ListFragmentDragDrop


class SettingsListOptionsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.home_list_view_settings)


    }

}

class SettingsListOptions : AppCompatActivity() {
    internal var order: Order? = null
    internal var sampleTableRowView: View? = null
    private var prieviewLayout: ViewGroup? = null
    private var sharedPreferences: SharedPreferences? = null
    private var listener: OnSharedPreferenceChangeListener? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_list_options)


        val i = this.intent
        order = i.getSerializableExtra("order") as Order?
        if (order == null) {
            order = Order()
            order?.address = "1234 State St. New Orleans LA 06890"
            order?.cost = 9.99f
            order?.tipTotalsForThisAddress?.averagePercentageTip = 10f
        }
        order?.let {
            it.tipTotalsForThisAddress.averagePercentageTip = 0.1f
            it.tipTotalsForThisAddress.averageTip = 3.99f
            it.tipTotalsForThisAddress.bestTip = 7f
            it.tipTotalsForThisAddress.deliveries = 21
            it.tipTotalsForThisAddress.lastTip = 0.01f
            it.tipTotalsForThisAddress.worstTip = 0f
        }


        prieviewLayout = findViewById<View>(R.id.sampleCellContainer) as ViewGroup

        val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        sampleTableRowView = vi.inflate(R.layout.home_screen_table_row, null)
        prieviewLayout!!.addView(sampleTableRowView)

        //TODO: Read the prefs and set the values so they show when we launch from home screen

    }

    public override fun onResume() {
        super.onResume()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        listener = OnSharedPreferenceChangeListener { sharedPreferences, key -> updatePreview() }
        sharedPreferences!!.registerOnSharedPreferenceChangeListener(listener)

        updatePreview()
    }

    public override fun onPause() {
        super.onPause()
        sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(listener)
    }


    fun updatePreview() {
        HomeScreen_ListFragmentDragDrop.formatTableRowViewForOrder(
            sampleTableRowView,
            order!!,
            PreferenceManager.getDefaultSharedPreferences(applicationContext),
            1
        )
    }


}
