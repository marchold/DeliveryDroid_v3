package catglo.com.deliverydroid.shift

import android.content.DialogInterface
import android.os.Build
import android.widget.Button
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import catglo.com.deliverydroid.R
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import android.view.View
import android.view.View.OnClickListener
import catglo.com.deliverydroid.widgets.HorizontalDatePicker


class TodayShiftActivity : ShiftActivity() {

    override fun shiftTimeClickListener(
        time: MutableDateTime,
        setter: (year:Int, month: Int, day : Int, hour : Int, minute : Int ) -> Unit
    ): OnClickListener {
        return OnClickListener {
            val customView = View.inflate(this@TodayShiftActivity, R.layout.time_day_picker_dialog, null)
            val timePicker = customView.findViewById<TimePicker>(R.id.timePicker)

            val datePicker = customView.findViewById<HorizontalDatePicker>(R.id.weekdayPicker)
            datePicker.setTime(time)
            val nowButton = customView.findViewById<Button>(R.id.nowButton)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = time.hourOfDay
                timePicker.minute = time.minuteOfHour
            } else {
                @Suppress("DEPRECATION")
                timePicker.currentHour = time.hourOfDay
                @Suppress("DEPRECATION")
                timePicker.currentMinute = time.minuteOfHour
            }
            nowButton.setOnClickListener {
                time.millis = DateTime.now().millis
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.hour = time.hourOfDay
                    timePicker.minute = time.minuteOfHour
                } else {
                    timePicker.currentHour = time.hourOfDay
                    timePicker.currentMinute = time.minuteOfHour
                }
                datePicker.snap()
            }

            AlertDialog.Builder(this@TodayShiftActivity)
                .setView(customView)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    var year = 0
                    var month = 0
                    var day = 0
                    var hour = 0
                    var minute = 0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hour = timePicker.hour
                        minute = timePicker.minute
                    } else {
                        hour = timePicker.currentHour
                        minute = timePicker.currentMinute
                    }
                    time.hourOfDay = hour
                    time.minuteOfDay = minute
                    datePicker.weekdayAdapter?.relativeWeekdayIndex?.let { time.addDays(it) }
                    year = time.year().get()
                    month = time.monthOfYear
                    day = time.dayOfMonth
                    setter(year, month, day, hour, minute)
                    updateUI()
                }
                .show()
        }
    }
}
