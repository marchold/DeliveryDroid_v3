package catglo.com.deliverydroid.neworder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker

import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.Utils
import catglo.com.deliverydroid.widgets.HorizontalDatePicker
import kotlinx.android.synthetic.main.new_order_time_frgament.*


import org.joda.time.DateTime
import org.joda.time.DurationFieldType
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.MutableDateTime
import java.lang.reflect.AccessibleObject.setAccessible



/**
 * Created by goblets on 2/17/14.
 */
class TimeFragment : DataAwareFragment(), TimePicker.OnTimeChangedListener, NumberPicker.OnValueChangeListener {

    private var sharedPreferences: SharedPreferences? = null
    private var inHowManyMinutes: NumberPicker? = null
    private var timePicker: TimePicker? = null
    //private var button1: View? = null
    //private var button2: View? = null
    //private var button3: View? = null
    private var nextButton: View? = null
    private var time: MutableDateTime? = null
    private var isInit = false
    private var datePicker: HorizontalDatePicker? = null
    private var inMinutes: TextView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        (activity as NewOrderActivity).tools.hideOnScreenKeyboard()
        time = MutableDateTime((activity as NewOrderActivity).order.time.time)
        datePicker?.setTime(time!!)

        nextButton?.setOnClickListener { (activity as ButtonPadFragment.ButtonPadNextListener).onNextButtonPressed() }

        timePicker?.setOnTimeChangedListener(this)
        inHowManyMinutes?.setOnValueChangedListener(this)

        setDatePicker()
        setNumberPicker()

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    internal fun setButtonClickListenerForMinutes(minutes: Int, button: View?) {
        button?.setOnClickListener {
            time = MutableDateTime.now()
            time?.add(Minutes.minutes(minutes))
            val activity = activity as NewOrderActivity?
            val order = activity?.order
            time?.let {  order?.time?.time = it.millis }
            setDatePicker()
            setNumberPicker()
        }
    }

    override fun onPause() {
        super.onPause()
        setDatePicker()
        setNumberPicker()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)

        val view = inflater.inflate(R.layout.new_order_time_frgament, null) as RelativeLayout

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
        return view
    }

    override fun onDataChangedHandler() {
        time = MutableDateTime((activity as NewOrderActivity).order.time.time)
        setDatePicker()
        setNumberPicker()
    }

    fun setDatePicker() {
        time?.let { t->
            timePicker?.currentHour = t.hourOfDay
            timePicker?.currentMinute = t.minuteOfHour
        }
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

    fun setNumberPicker() {
        val now = DateTime.now()
        time?.let {t ->
            val minutesToOrder = Minutes.minutesBetween(now, t).minutes
            if (minutesToOrder>=0 && minutesToOrder<=91){
                inHowManyMinutes?.value = minutesToOrder
                inHowManyMinutes?.visibility = View.VISIBLE
                numberPickerFirstNumberHack()
            }
            else if (minutesToOrder<0 && minutesToOrder>=-90){
                val value = 182+minutesToOrder
                inHowManyMinutes?.value = value
                inHowManyMinutes?.visibility = View.VISIBLE
                numberPickerFirstNumberHack()
            }
            else {
                inHowManyMinutes?.visibility = View.GONE
            }

        }
    }


    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        if (isInit == false) {
            val activity = activity as NewOrderActivity?
            val order = activity?.order
            time?.hourOfDay = hourOfDay
            time?.minuteOfHour = minute
            time?.let { t ->
                order?.time?.time = t.millis
            }
            isInit = true
            setNumberPicker()
            isInit = false
            val lastScreenFragment = activity?.getFragment(NewOrderActivity.Pages.order)
            lastScreenFragment?.onDataChanged()
        }
        time?.let { t ->
            datePicker?.setTime(t)
            val minutesDifference =  Minutes.minutesBetween(DateTime.now(), t).minutes
            inMinutes?.text = if (minutesDifference>0) "In $minutesDifference minutes" else "${-minutesDifference} minutes ago"
        }
    }

    override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
        if (isInit == false) {
            val activity = activity as NewOrderActivity?
            val order = activity?.order
            time = MutableDateTime(order!!.time)
            time?.let { t ->
                val value = if (newVal<=91) newVal else (0-(182-newVal))
                t.add(DurationFieldType.minutes(), value)
                order.time?.time = t.millis
                val lastScreenFragment = activity.getFragment(NewOrderActivity.Pages.order)
                timePicker?.currentHour = t.hourOfDay
                timePicker?.currentMinute = t.minuteOfDay
                lastScreenFragment?.onDataChanged()
            }
        }
    }
}
