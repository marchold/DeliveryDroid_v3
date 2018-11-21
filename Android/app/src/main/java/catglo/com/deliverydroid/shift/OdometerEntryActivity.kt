package catglo.com.deliverydroid.shift

import android.content.Intent
import android.os.Bundle


import catglo.com.deliveryDatabase.Shift
import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity
import catglo.com.deliverydroid.R
import kotlinx.android.synthetic.main.odometer_entry_activity.*

class OdometerEntryActivity : DeliveryDroidBaseActionBarActivity() {
    private var dataBasePrimaryKey: Int = 0
    private var isStartShift: Boolean = false
    private lateinit var shift: Shift

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = intent
        dataBasePrimaryKey = i.getIntExtra("ID", -1)
        isStartShift = i.getBooleanExtra("startValue", true)

        dataBase.createShiftRecordIfNonExists()
        shift = dataBase.getShift(dataBasePrimaryKey)

        setContentView(R.layout.odometer_entry_activity)

        meterView.value = if (isStartShift) {
             shift.odometerAtShiftStart
        } else {
             shift.odometerAtShiftEnd
        }
    }

    @Override
    override fun onPause() {
        super.onPause()

        shift.odometerAtShiftStart = if (isStartShift) {
            meterView.value
        } else {
            meterView.value
        }
        dataBase.saveShift(shift);

    }
}