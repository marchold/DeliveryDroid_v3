package catglo.com.deliverydroid.shift

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import catglo.com.deliveryDatabase.Shift
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.shift.MeterView
import catglo.com.deliverydroid.shift.MeterNumberPicker

import kotlinx.android.synthetic.main.shift_odometer_activity.*
import java.util.*

fun Locale.isImperial(): Boolean {
    when (Locale.getDefault().country.toUpperCase()) {
        "GB", "MM", "LR", "US" -> return true
        else -> return false
    }
}

class OdometerActivity : DeliveryDroidBaseActivity() {

    private var whichShift: Int = 0
    lateinit var shift: Shift

    fun updateUI(){
        if (Locale.getDefault().isImperial()) {
            distanceDrivenTitle.text = getString(R.string.milesDriven)
            startOdometerValueLabel.text = "${shift.odometerAtShiftStart.toString()}mi"
            endOdometerValueLabel.text = "${shift.odometerAtShiftEnd.toString()}mi"
            distanceDrivenValueLabel.text = (shift.odometerAtShiftEnd - shift.odometerAtShiftStart).toString() + "mi"
        } else {
            distanceDrivenTitle.text = getString(R.string.killometers_driven)
            startOdometerValueLabel.text = "${shift.odometerAtShiftStart.toString()}km"
            endOdometerValueLabel.text = "${shift.odometerAtShiftEnd.toString()}km"
            distanceDrivenValueLabel.text = (shift.odometerAtShiftEnd - shift.odometerAtShiftStart).toString() + "km"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_odometer_activity)

        val intent = intent
        val id = intent.getIntExtra("ID", -1)
        if (id == -1) {
            whichShift = dataBase.curShift
        } else {
            whichShift = id
        }
        shift = dataBase.getShift(whichShift)

        val recentOdometerValue = dataBase.mostRecientOdomenterValue
        if (shift.odometerAtShiftStart == 0) {
            shift.odometerAtShiftStart = recentOdometerValue
        }
        if (shift.odometerAtShiftEnd < shift.odometerAtShiftStart) {
            shift.odometerAtShiftEnd = shift.odometerAtShiftStart
        }

        odometerDoneButton.setOnClickListener { finish() }

        startOdometerValueLabel.setOnClickListener {
            val meterLayout = View.inflate(this@OdometerActivity,R.layout.meter_view_dialog, null)
            val meterView = meterLayout.findViewById<MeterView>(R.id.meterView)
            val milesDriven = meterLayout.findViewById<TextView>(R.id.milesDriven)
            meterView.setOnValueChangedListener {
                if (it==0){
                    milesDriven.text = ""
                } else {
                    try {
                        if (milesDriven.text.toString().toInt() != it) {
                            milesDriven.setText("$it")
                        }
                    } catch (e: NumberFormatException) {
                        milesDriven.setText("$it")
                    }
                }
            }
            milesDriven.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        if (s.toString().toInt() != meterView.value) {
                            meterView.value = s.toString().toInt()
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
            })
            if (shift.odometerAtShiftStart==0) milesDriven.text = ""
            else milesDriven.setText(shift.odometerAtShiftStart.toString())
            meterView.value = shift.odometerAtShiftStart
            AlertDialog.Builder(this@OdometerActivity)
                .setTitle(R.string.Starting_odometer)
                .setView(meterLayout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (meterView.value < shift.odometerAtShiftStart) {
                        Toast.makeText(
                            this@OdometerActivity,
                            "Distance going backwards ",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    shift.odometerAtShiftStart = meterView.value
                    dataBase.saveShift(shift)
                    updateUI()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }


        endOdometerValueLabel.setOnClickListener {
            val meterLayout = View.inflate(this@OdometerActivity,
                R.layout.meter_view_dialog, null)
            val meterView = meterLayout.findViewById<MeterView>(R.id.meterView)
            val milesDriven = meterLayout.findViewById<EditText>(R.id.milesDriven)
            meterView.setOnValueChangedListener {value ->
                if (value == 0)
                {
                    milesDriven.setText("")
                } else {
                    try {
                        if (milesDriven.text.toString().toInt() != value) {
                            milesDriven.setText("$value")
                        }
                    } catch (e: NumberFormatException) {
                        milesDriven.setText("$value")
                    }
                }
            }
            milesDriven.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        if (s.toString().toInt() != meterView.value) {
                            meterView.value = s.toString().toInt()
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
            })
            meterView.value = shift.odometerAtShiftEnd
            if (shift.odometerAtShiftEnd==0){
                milesDriven.setText("")
            }else {
                milesDriven.setText("${shift.odometerAtShiftEnd}")
            }
            if (meterView.value < shift.odometerAtShiftStart) {
                meterView.value = shift.odometerAtShiftStart + 1
            }
            AlertDialog.Builder(this@OdometerActivity)
                .setTitle(R.string.End_odometer)
                .setView(meterLayout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (meterView.value < shift.odometerAtShiftStart) {
                        Toast.makeText(
                            this@OdometerActivity,
                            "Distance does not go backwards",
                            Toast.LENGTH_LONG
                        ).show()
                        updateUI()
                    } else {
                        shift.odometerAtShiftEnd = meterView.value
                        dataBase.saveShift(shift)
                        updateUI()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        updateUI()
    }

    public override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        dataBase.saveShift(shift);
    }

}
