package catglo.com.deliverydroid.shift

import android.app.AlertDialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import catglo.com.deliveryDatabase.Shift
import catglo.com.deliveryDatabase.TipTotalData
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.Utils
import kotlinx.android.synthetic.main.start_end_shift_form.*
import kotlinx.android.synthetic.main.shift_start_end_activity.*

import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.Toast
import com.alexzaitsev.meternumberpicker.MeterView
import org.joda.time.MutableDateTime
import java.util.*


fun Locale.isImperial() : Boolean {
    when (Locale.getDefault().country.toUpperCase()) {
        "GB", "MM", "LR", "US" -> return true
        else -> return false
    }
}

class ShiftStartEndActivity : DeliveryDroidBaseActivity() {

    internal var whichShift: Int = 0

    lateinit var shift: Shift

    private var tips: TipTotalData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_start_end_activity)

        deleteShiftClickable.setOnClickListener {
            AlertDialog.Builder(this@ShiftStartEndActivity).run {
                setTitle("Delete shift?")
                setMessage("Are you sure you want to delete this shift?")
                setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dataBase.deleteShift(whichShift)
                    dialog.dismiss()
                    updateUI()
                }
                setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    show()
            }

        }

        newShiftButton?.setOnClickListener {
            dataBase.setNextShift()
            updateUI()
        }


        /*Took this out along with the XML until I could get a really good time tracking code
        timeClockClickable.setOnClickListener {
            val intent = Intent(applicationContext, ShiftStartEndDayActivity::class.java)
            intent.putExtra("ID", whichShift)
            startActivity(intent)
        }*/


        val intent = intent
        val id = intent.getIntExtra("ID", -1)
        if (id == -1) {
            whichShift = dataBase.curShift
        } else {
            whichShift = id
        }


        val currentShiftNumber = findViewById<View>(R.id.currentShiftNumber) as TextView
        currentShiftNumber.text = whichShift.toString()

        shiftStartTime.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(this@ShiftStartEndActivity,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val time = MutableDateTime.now()
                    time.hourOfDay = selectedHour
                    time.minuteOfHour = selectedMinute
                    shift.startTime = time
                    dataBase.saveShift(shift)
                    updateUI()
                }, hour, minute, DateFormat.is24HourFormat(this)
            )
            timePicker.setTitle("Select Time")
            timePicker.show()
        }
        shiftEndTime.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(this@ShiftStartEndActivity,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val time = MutableDateTime.now()
                    time.hourOfDay = selectedHour
                    time.minuteOfHour = selectedMinute
                    if (time.isBefore(shift.startTime))
                    {
                        time.dayOfYear++
                        time.hourOfDay = selectedHour
                    }
                    shift.endTime = time

                    dataBase.saveShift(shift)
                    updateUI()
                }, hour, minute, DateFormat.is24HourFormat(this)
            )
            timePicker.setTitle("Select Time")
            timePicker.show()
        }

        startingOdometer.setOnClickListener {
            val meterLayout = View.inflate(this@ShiftStartEndActivity,R.layout.meter_vew_wrapper,null)
            val meterView = meterLayout.findViewById<MeterView>(R.id.meterView)
            val milesDriven = meterLayout.findViewById<TextView>(R.id.milesDriven)
            meterView.value = shift.odometerAtShiftStart
            AlertDialog.Builder(this@ShiftStartEndActivity)
                .setTitle(R.string.Starting_odometer)
                .setView(meterLayout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (meterView.value<shift.odometerAtShiftStart)
                    {
                        Toast.makeText(this@ShiftStartEndActivity,"Distance going backwards ",Toast.LENGTH_LONG).show()
                    }
                    shift.odometerAtShiftStart = meterView.value
                    dataBase.saveShift(shift)
                    updateUI()
                }
                .setNegativeButton(android.R.string.cancel,null)
                .show()
        }

        endingOdometer.setOnClickListener {
            val meterLayout = View.inflate(this@ShiftStartEndActivity,R.layout.meter_vew_wrapper,null)
            val meterView = meterLayout.findViewById<MeterView>(R.id.meterView)
            val milesDriven = meterLayout.findViewById<TextView>(R.id.milesDriven)

            meterView.value = shift.odometerAtShiftEnd
            if (meterView.value<shift.odometerAtShiftStart){
                meterView.value = shift.odometerAtShiftStart+1
            }
            AlertDialog.Builder(this@ShiftStartEndActivity)
                .setTitle(R.string.End_odometer)
                .setView(meterLayout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (meterView.value<shift.odometerAtShiftStart)
                    {
                        Toast.makeText(this@ShiftStartEndActivity,"Distance does not go backwards",Toast.LENGTH_LONG).show()
                        endingOdometer.performClick()
                    }
                    else {
                        shift.odometerAtShiftEnd = meterView.value
                        dataBase.saveShift(shift)
                        updateUI()
                    }
                }
                .setNegativeButton(android.R.string.cancel,null)
                .show()
        }




        //OK Button
        val doneClickListener = View.OnClickListener { finish() }
        doneButton.setOnClickListener(doneClickListener)
        backButton.setOnClickListener(doneClickListener)
    }

    public override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onPause() {
        super.onPause()



        dataBase.saveShift(shift);
    }

    fun updateUI() {
        shift = dataBase.getShift(whichShift)

        if (shift.endTime.millis == 0L && shift.startTime.millis == 0L) {
            dataBase.estimateShiftTimes(shift)
        }

        tips = dataBase.getTipTotal(
            applicationContext, "Payed != -1 AND Shift=$whichShift",
            "WHERE shifts.ID=$whichShift"
        )

        shift.startTime.toGregorianCalendar().let { shiftStartTime.setText(String.format("%tl:%tM %tp %ta", it, it, it, it)) }
        shift.endTime.toGregorianCalendar().let {  shiftEndTime.setText(String.format("%tl:%tM %tp %ta", it, it, it, it)) }

        deliveries.text = tips?.deliveries.toString()

        val t1 = shift.endTime.millis.toFloat()
        val t2 = shift.startTime.millis.toFloat()
        var total = t1 - t2
        total /= 3600000f
        hoursWorked.text = String.format("%.2f", total)

        val recentOdometerValue = dataBase.mostRecientOdomenterValue
        if (shift.odometerAtShiftStart == 0) {
            shift.odometerAtShiftStart = recentOdometerValue
        }
        if (shift.odometerAtShiftEnd < shift.odometerAtShiftStart) {
            shift.odometerAtShiftEnd = shift.odometerAtShiftStart
        }

        if (Locale.getDefault().isImperial()) {
            startingOdometer.setText("${shift.odometerAtShiftStart.toString()}mi")
            endingOdometer.setText("${shift.odometerAtShiftEnd.toString()}mi")
        } else {
            startingOdometer.setText("${shift.odometerAtShiftStart.toString()}km")
            endingOdometer.setText("${shift.odometerAtShiftEnd.toString()}km")
        }
        moneyCollected.text = Utils.getFormattedCurrency(tips?.payed)

        totalMilesDriven.text = (shift.odometerAtShiftEnd - shift.odometerAtShiftStart).toString()

            //  val minutesWorked = hoursWorked

     ///   hoursWorked.setText((shift.endTime.hourOfDay.toInt()-shift.startTime.hourOfDay.toInt()).toString())

        //TODO: Figure out what this endThisShift view was about
        /*   endThisShift.s = if (tips.deliveries == 0) { GONE } else { VISIBLE }

           if (dataBase.isTodaysShift(shift) == false) {
               endThisShift.visibility = GONE
           }*/
    }




}
