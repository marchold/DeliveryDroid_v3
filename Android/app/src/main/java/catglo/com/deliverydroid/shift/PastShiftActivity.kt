package catglo.com.deliverydroid.shift

import android.app.Activity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.shift_activity.*

class PastShiftActivity : ShiftActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteShiftClickable?.visibility = View.GONE
        newShiftButton?.visibility = View.GONE
    }
}
