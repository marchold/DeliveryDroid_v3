package catglo.com.deliverydroid.shift

import android.os.Bundle
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import catglo.com.deliveryDatabase.DataBase
import catglo.com.deliveryDatabase.Shift
import catglo.com.deliveryDatabase.TipTotalData
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.R

import kotlinx.android.synthetic.main.shift_activity.*
import org.joda.time.DateTime
import org.joda.time.MutableDateTime


class ShiftActivity : DeliveryDroidBaseActivity() {

    fun shiftTimeClickListener(
        time: MutableDateTime,
        setter: (datePicker: DatePicker, timePicker: TimePicker) -> Unit
    ): View.OnClickListener {
        return View.OnClickListener {
            val customView = View.inflate(this@ShiftActivity, R.layout.time_date_picker_dialog, null)
            val timePicker = customView.findViewById<TimePicker>(R.id.timePicker)
            val datePicker = customView.findViewById<DatePicker>(R.id.weekdayPicker)
            val nowButton = customView.findViewById<Button>(R.id.nowButton)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = time.hourOfDay
                timePicker.minute = time.minuteOfHour
            } else {
                timePicker.currentHour = time.hourOfDay
                timePicker.currentMinute = time.minuteOfHour
            }
            nowButton.setOnClickListener {
                val now = DateTime.now()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.hour = now.hourOfDay
                    timePicker.minute = now.minuteOfHour
                } else {
                    timePicker.currentHour = now.hourOfDay
                    timePicker.currentMinute = now.minuteOfHour
                }
                datePicker.updateDate(now.year, now.monthOfYear - 1, now.dayOfMonth)
            }
            datePicker.updateDate(time.year, time.monthOfYear - 1, time.dayOfMonth)
            AlertDialog.Builder(this@ShiftActivity)
                .setView(customView)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    setter(datePicker, timePicker)
                    updateUI()
                }
                .show()
        }
    }


    var whichShift: Int = 0
    lateinit var shift: Shift
    var tips: TipTotalData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_activity)

        val intent = intent
        val id = intent.getIntExtra("ID", -1)
        if (id == -1) {
            shift = dataBase.getShift(DataBase.TodaysShiftCount)
            whichShift = DataBase.TodaysShiftCount
        } else {
            whichShift = id
            shift = dataBase.getShift(whichShift)
        }



        doneButton.setOnClickListener { finish() }

        startTimeValueLabel.setOnClickListener(shiftTimeClickListener(shift.startTime) { datePicker, timePicker ->
            shift.startTime.year = datePicker.year
            shift.startTime.monthOfYear = datePicker.month + 1
            shift.startTime.dayOfMonth = datePicker.dayOfMonth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shift.startTime.hourOfDay = timePicker.hour
                shift.startTime.minuteOfHour = timePicker.minute
            } else {
                shift.startTime.hourOfDay = timePicker.currentHour
                shift.startTime.minuteOfHour = timePicker.currentMinute
            }
            dataBase.saveShift(shift)
        })

        endTimeValueLabel.setOnClickListener(shiftTimeClickListener(shift.endTime) { datePicker, timePicker ->
            shift.endTime.year = datePicker.year
            shift.endTime.monthOfYear = datePicker.month + 1
            shift.endTime.dayOfMonth = datePicker.dayOfMonth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shift.endTime.hourOfDay = timePicker.hour
                shift.endTime.minuteOfHour = timePicker.minute
            } else {
                shift.endTime.hourOfDay = timePicker.currentHour
                shift.endTime.minuteOfHour = timePicker.currentMinute
            }
            dataBase.saveShift(shift)
        })

     /*
*/

        deleteShiftClickable.setOnClickListener {
            AlertDialog.Builder(this@ShiftActivity).run {
                setTitle("Delete shift?")
                setMessage("Are you sure you want to delete this shift?")
                setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dataBase.deleteShift(whichShift)
                    dialog.dismiss()
                    shift = dataBase.getShift(dataBase.curShift)
                    updateUI()
                }
                setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                show()
            }

        }

        newShiftButton?.setOnClickListener {
            dataBase.setNextShift()
            shift = dataBase.getShift(dataBase.curShift)
            updateUI()
        }


        setOdometerButton.setOnClickListener {
            startActivity(Intent(this@ShiftActivity, OdometerActivity::class.java).run { putExtra("whichShift",whichShift) })
        }

    }

    public override fun onResume() {
        super.onResume()

        shift = dataBase.getShift(whichShift)
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        dataBase.saveShift(shift);
    }

    fun updateUI() {

        if (shift.endTime.millis == 0L && shift.startTime.millis == 0L) {
            dataBase.estimateShiftTimes(shift)
        }

        tips = dataBase.getTipTotal(
            applicationContext, "Payed != -1 AND Shift=$whichShift",
            "WHERE shifts.ID=$whichShift",null
        )

        shift.startTime.toGregorianCalendar()
            .let { startTimeValueLabel.text = String.format("%tl:%tM %tp %ta", it, it, it, it) }
        shift.endTime.toGregorianCalendar()
            .let { endTimeValueLabel.text = String.format("%tl:%tM %tp %ta", it, it, it, it) }

        //  deliveries.text = tips?.deliveries.toString()

        val t1 = shift.endTime.millis.toFloat()
        val t2 = shift.startTime.millis.toFloat()
        var total = t1 - t2
        total /= 3600000f
        hoursWorkedValueLabel.text = String.format("%.2f", total)



      /*  */
        //  moneyCollected.text = Utils.getFormattedCurrency(tips?.payed)


    }


}
