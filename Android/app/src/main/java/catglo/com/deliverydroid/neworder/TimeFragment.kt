package catglo.com.deliverydroid.neworder

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker

import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.widgets.HorizontalDatePicker
import kotlinx.android.synthetic.main.new_order_time_frgament.*


import org.joda.time.DateTime
import org.joda.time.DurationFieldType
import org.joda.time.Minutes
import org.joda.time.MutableDateTime
import kotlin.math.absoluteValue
import android.app.Activity
import android.util.Log
import android.view.View.*


/**
 * Created by goblets on 2/17/14.
 */
class TimeFragment : DataAwareFragment(), TimePicker.OnTimeChangedListener, NumberPicker.OnValueChangeListener {

    private var sharedPreferences: SharedPreferences? = null
    private var inHowManyMinutes: NumberPicker? = null
    private var timePicker: TimePicker? = null
    private var nextButton: View? = null
    private var time = MutableDateTime(MutableDateTime.now())
    private var isInit = false
    private var datePicker: HorizontalDatePicker? = null
    private var inMinutes: TextView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        (activity as NewOrderActivity).tools.hideOnScreenKeyboard()
        datePicker?.setTime(time)

        nextButton?.setOnClickListener { (activity as ButtonPadFragment.ButtonPadNextListener).onNextButtonPressed() }

        timePicker?.setOnTimeChangedListener(this)
        inHowManyMinutes?.setOnValueChangedListener(this)


        setDatePicker()
        setNumberPicker()

        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity?.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        timePicker?.findViewById<View>(Resources.getSystem().getIdentifier("minutes","id", "android"))?.let {
            it.post {  it.performClick() }
        }
        timePicker?.findViewById<View>(Resources.getSystem().getIdentifier("toggle_mode","id", "android"))?.let {
            it.post {  it.visibility = GONE }
        }
        datePicker?.onDaySelected = onDayChanged()


    }


    override fun onPause() {
        super.onPause()
        setDatePicker()
        setNumberPicker()
    }

    var once = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)

        val view = inflater.inflate(R.layout.new_order_time_frgament, null)

        inHowManyMinutes = view.findViewById<View>(R.id.numberPicker) as NumberPicker
        timePicker = view.findViewById<View>(R.id.timePicker) as TimePicker

        nextButton = view.findViewById(R.id.next_button)

        inHowManyMinutes?.maxValue = 181
        inHowManyMinutes?.minValue = 0
        inHowManyMinutes?.setFormatter {
            if (it<=91) it.toString()
            else (0-(182-it)).toString()
        }
        datePicker = view.findViewById(R.id.timeLabel)
        inMinutes = view.findViewById<View>(R.id.inMinutes) as TextView

        val nowButton = view.findViewById<View>(R.id.nowButton)
        nowButton.setOnClickListener {
            updateAllPickers( MutableDateTime(MutableDateTime.now()))
        }

        return view
    }

    fun numberPickerFirstNumberHack(){
        try {
            val method = numberPicker!!.javaClass.getDeclaredMethod("changeValueByOne", Boolean::class.javaPrimitiveType)
            method.setAccessible(true)
            method.invoke(numberPicker!!, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun updateAllPickers(t:MutableDateTime){
        val activity = activity as NewOrderActivity? ?: return
        if (isInit) return
        isInit = true
        Log.i("DATE","updateAllPickers ${t.toString()} ")
        time = t
        datePicker?.setTime(time)
        setDatePicker()
        setNumberPicker()
        val timeDiff = time.dayOfYear - MutableDateTime.now().dayOfYear().get()
        inHowManyMinutes?.visibility = if (timeDiff == 0) { VISIBLE } else { GONE }
        val lastScreenFragment = activity.getFragment(NewOrderActivity.Pages.order)
        lastScreenFragment?.onDataChanged()
        isInit = false
    }

    override fun onDataChangedHandler() {
        time = MutableDateTime((activity as NewOrderActivity).order.time.time)
        updateAllPickers(time)
    }

    fun setDatePicker() {
        time.let { t->
            timePicker?.currentHour = t.hourOfDay
            timePicker?.currentMinute = t.minuteOfHour
        }
    }


    var blockSetNumberPicker = false

    fun setNumberPicker() {
        if (blockSetNumberPicker) return
        val now = DateTime.now()
        time?.let {t ->
            val minutesToOrder = Minutes.minutesBetween(now, t).minutes
            if (minutesToOrder in 0..91){
                inHowManyMinutes?.value = minutesToOrder
                inHowManyMinutes?.visibility = VISIBLE
                numberPickerFirstNumberHack()
            }
            else if (minutesToOrder in -90..0){
                val value = 182+minutesToOrder
                inHowManyMinutes?.value = value
                inHowManyMinutes?.visibility = VISIBLE
                numberPickerFirstNumberHack()
            }
            else {
                inHowManyMinutes?.visibility = GONE
            }
        }
    }


    /***************
     * Callbacks for each of the 3 controls
     */
    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        val activity = activity as NewOrderActivity?

        Log.i("DATE","onTimeChanged $hourOfDay : $minute ")

        val order = activity?.order
        time?.hourOfDay = hourOfDay
        time?.minuteOfHour = minute
        time?.let { t ->
            order?.time?.time = t.millis
        }
        if (isInit == false) {

            isInit = true
            updateAllPickers(time)
            isInit = false

        }
        time?.let { t ->
            datePicker?.setTime(t)
            val minutesDifference =  Minutes.minutesBetween(DateTime.now(), t).minutes
            inMinutes?.text = if (minutesDifference>0) "In $minutesDifference minutes" else "${-minutesDifference} minutes ago"
        }
    }

    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        if (isInit == false) {
            Log.i("DATE","onValueChange $newVal ")
            blockSetNumberPicker = true
            val activity = activity as NewOrderActivity?
            val order = activity?.order
            time = MutableDateTime(DateTime.now())
            time?.let { t ->
                val value = if (newVal<=91) newVal else (0-(182-newVal))
                t.add(DurationFieldType.minutes(), value)
                order?.time?.time = t.millis
                val lastScreenFragment = activity?.getFragment(NewOrderActivity.Pages.order)
                timePicker?.currentHour = t.hourOfDay.absoluteValue
                timePicker?.currentMinute = t.minuteOfHour.absoluteValue
                time = t
                updateAllPickers(t)
            }
            blockSetNumberPicker = false
        }
    }

    private fun onDayChanged(): (Int) -> Unit {
        return {
            Log.i("DATE","onDayChanged $it ")
            time.dayOfYear = MutableDateTime.now().dayOfYear().get() + it
            updateAllPickers(time)
        }
    }

}
