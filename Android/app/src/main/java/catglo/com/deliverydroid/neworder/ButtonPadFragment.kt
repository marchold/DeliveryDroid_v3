package catglo.com.deliverydroid.neworder

import android.R
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.KeyListener
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.Settings
import catglo.com.deliverydroid.neworder.ButtonPadFragment
import catglo.com.deliverydroid.widgets.OnTextChangedListener

/**
 * Created by goblets on 2/16/14.
 */
abstract class ButtonPadFragment : DataAwareFragment(), OnTextChangedListener,
    AdapterView.OnItemClickListener {
    private val dirty = false
    private val keyListener: KeyListener? = null
    private var code = 0
    var abc: View? = null
    var space: View? = null
    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) {
        val tv = view.findViewById<View>(R.id.text1) as TextView
        edit?.setText(tv.text)
        try {
            edit?.setSelection(tv.text.length)
        } catch (e: Exception) {
            edit?.setSelection(tv.text.length - 1)
        }
    }

    /* If the activity implements this interface it will get the callback
         *
         */
    interface ButtonPadNextListener {
        fun onNextButtonPressed()
    }

    private var sharedPreferences: SharedPreferences? = null
    @JvmField
    var edit: EditText? = null
    @JvmField
    var list: ListView? = null
//    protected var context: Context? = null
    protected var text: TextView? = null
    protected var seven: View? = null
    protected var eight: View? = null
    protected var nine: View? = null
    protected var four: View? = null
    protected var five: View? = null
    protected var six: View? = null
    protected var one: View? = null
    protected var two: View? = null
    protected var three: View? = null
    protected var dot: View? = null
    protected var zero: View? = null
    protected var del: View? = null
    var next: View? = null
    var speakButton: View? = null
    var numbers: RelativeLayout? = null
    var callback: Runnable? = null
    var customButton: View? = null
    protected var buttons: RelativeLayout? = null
    var tooltipLayout: ViewGroup? = null
    var tooltipText: TextView? = null
    abstract val listAdapter: ListAdapter?
    fun press(keyVal: Int) { //edit.setKeyListener(keyListener);
        if (keyVal == -1) {
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_PERIOD
                )
            )
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_PERIOD
                )
            )
        }
        if (keyVal == -2) {
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_DEL
                )
            )
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_DEL
                )
            )
        }
        if (keyVal == -3) {
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_SPACE
                )
            )
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_SPACE
                )
            )
        } else {
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_0 + keyVal
                )
            )
            edit!!.dispatchKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_0 + keyVal
                )
            )
        }
    }

    fun setAdapter(adapter: ArrayAdapter<String?>) { //TODO: Tooltip
        list!!.visibility = View.VISIBLE
        tooltipLayout!!.visibility = View.GONE
        if (adapter.isEmpty == false) {
            list!!.adapter = adapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as DeliveryDroidBaseActivity?
        if (listAdapter != null) {
            list!!.adapter = listAdapter
        } else { //   list.setVisibility(View.GONE);
//TODO: Show help for new users and focus it for no keyboard
        }
        list!!.visibility = View.VISIBLE
        //  activity.tools.hideOnScreenKeyboard(edit);
/*
//TODO: Fix this so it works, The keyboard listener was firing when it should not
       activity.tools.setKeyboardListener(new DeliveryDroidBaseActivity.OnKeyboardVisibilityListener() {public void onVisibilityChanged(boolean visible) {
            if (visible) {
                buttons.setVisibility(View.GONE);
            }
            else {
                //No On Screen Keyboard
                buttons.setVisibility(View.VISIBLE);
            }}});*/edit!!.setOnClickListener {
            if (edit!!.isFocusable == false || edit!!.isFocusableInTouchMode == false) {
                edit!!.inputType = inputType
                edit!!.isFocusableInTouchMode = true
                edit!!.isFocusable = true
                edit!!.requestFocus()
                // buttons.setVisibility(View.GONE);
                val activity =
                    getActivity() as DeliveryDroidBaseActivity?
                activity!!.utils.showOnScreenKeyboard()
            }
        }
    }

    protected open val inputType: Int
        protected get() = InputType.TYPE_CLASS_NUMBER

    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(
        resultCode: Int,
        requestCode: Int,
        data: Intent?
    ) {
        if (requestCode == Activity.RESULT_OK && data != null && resultCode == code
        ) {
            val matches =
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val i: Iterator<String> = matches.iterator()
            if (i.hasNext()) {
                var s = i.next()
                if (edit!!.text.toString().length > 0) {
                    s = edit!!.text.toString() + " " + s
                }
                edit!!.setText(s)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        val settings =
            Settings(activity!!.applicationContext)
        var layoutResource = catglo.com.deliverydroid.R.layout.button_pad
        sharedPreferences?.run {
            val left = getBoolean("number_keyboard_left", false)
            if (getBoolean("number_keyboard_upsidedown", false)) {
                layoutResource = if (left) {
                    catglo.com.deliverydroid.R.layout.button_pad_upsidedown_left
                } else {
                    catglo.com.deliverydroid.R.layout.button_pad_upsidedown
                }
            } else if (left) {
                layoutResource = catglo.com.deliverydroid.R.layout.button_pad_left
            }
        }
        numbers = inflater.inflate(layoutResource, null) as RelativeLayout
        one =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button01) as View
        one!!.requestFocus()
        two =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button02) as View
        three =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button03) as View
        four =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button04) as View
        five =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button05) as View
        six =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button06) as View
        seven =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button07) as View
        eight =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button08) as View
        nine =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.Button09) as View
        dot =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.ButtonDot) as View
        zero =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.ButtonZero) as View
        del =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.ButtonDel) as View
        next =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.ButtonNext) as View
        edit =
            numbers!!.findViewById<View>(catglo.com.deliverydroid.R.id.buttonPadEdit) as EditText
        edit!!.setTextColor(Color.WHITE)
        tooltipLayout =
            numbers!!.findViewById<View>(catglo.com.deliverydroid.R.id.tooltipLayout) as ViewGroup
        tooltipText =
            numbers!!.findViewById<View>(catglo.com.deliverydroid.R.id.tooltipText) as TextView
        list =
            numbers!!.findViewById<View>(catglo.com.deliverydroid.R.id.buttonPadList) as ListView
        list!!.isFastScrollEnabled = false
        speakButton =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.ButtonSpeech) as View
        customButton =
            numbers!!.findViewById(catglo.com.deliverydroid.R.id.setShiftTimesToOrderTimes) as View
        code = VOICE_RECOGNITION_REQUEST_CODE++
        speakButton!!.setOnClickListener {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getActivity().getString(R.string.Speak_address));
                startActivityForResult(intent, code)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(getContext(), "Not supported", Toast.LENGTH_LONG).show()
            }
        }
        zero!!.setOnClickListener { press(0) }
        one!!.setOnClickListener { press(1) }
        two!!.setOnClickListener { press(2) }
        three!!.setOnClickListener { press(3) }
        four!!.setOnClickListener { press(4) }
        five!!.setOnClickListener { press(5) }
        six!!.setOnClickListener { press(6) }
        seven!!.setOnClickListener { press(7) }
        eight!!.setOnClickListener { press(8) }
        nine!!.setOnClickListener { press(9) }
        dot!!.setOnClickListener { press(-1) }
        del!!.setOnClickListener { press(-2) }
        next!!.setOnClickListener {
            val activity: Activity? = activity
            if (activity is ButtonPadNextListener) {
                (activity as ButtonPadNextListener).onNextButtonPressed()
            }
        }
        list!!.cacheColorHint = -0x11111101
        list!!.onItemClickListener = this
        //Detect on screen keyboard show/hide
        buttons =
            numbers!!.findViewById<View>(catglo.com.deliverydroid.R.id.buttonPadButtonLayout) as RelativeLayout
        edit!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                try {
                    if (settingInitialValues == false) {
                        this@ButtonPadFragment.onTextChanged(s.toString())
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        })
        edit!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                next!!.performClick()
                return@OnEditorActionListener true
            }
            false
        })
        space = numbers?.findViewById(catglo.com.deliverydroid.R.id.ButtonSpace)
        space?.setVisibility(View.VISIBLE)
        space?.setBackgroundColor(0)
        space?.setOnClickListener(View.OnClickListener {
            press(-3)
            onSpace()
        })
        abc = numbers?.findViewById(catglo.com.deliverydroid.R.id.ButtonAbc)
        abc?.setVisibility(View.VISIBLE)
        abc?.setBackgroundColor(0)
        abc?.setOnClickListener(View.OnClickListener {
            edit!!.isFocusableInTouchMode = true
            edit!!.inputType = inputType
            edit!!.requestFocus()
            val mgr =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // only will trigger it if no physical keyboard is open
            mgr.showSoftInput(
                edit,
                InputMethodManager.SHOW_FORCED
            ) // .SHOW_IMPLICIT)
        })
        abc?.setVisibility(View.INVISIBLE)
        space?.setVisibility(View.INVISIBLE)
        val touchOverlay1 =
            numbers?.findViewById<View>(catglo.com.deliverydroid.R.id.curvedScreenOverlay1)
        val touchOverlay2 =
            numbers?.findViewById<View>(catglo.com.deliverydroid.R.id.curvedScreenOverlay2)
        if (settings.useCurvedScreenTouchOverlay()) {
            touchOverlay1?.visibility = View.VISIBLE
            touchOverlay2?.visibility = View.VISIBLE
            val eatTouches = OnTouchListener { v, event -> true }
            touchOverlay1?.setOnTouchListener(eatTouches)
            touchOverlay2?.setOnTouchListener(eatTouches)
        } else {
            touchOverlay1?.visibility = View.GONE
            touchOverlay2?.visibility = View.GONE
        }
        return numbers
    }

    protected open fun onSpace() {}

    companion object {
        private var VOICE_RECOGNITION_REQUEST_CODE = 100
    }
}