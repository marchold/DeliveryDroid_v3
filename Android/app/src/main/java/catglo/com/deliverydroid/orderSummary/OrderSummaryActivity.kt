package catglo.com.deliverydroid.orderSummary

import android.Manifest
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.app.DialogFragment
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.pm.PackageManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import catglo.com.deliveryDatabase.DataBase
import catglo.com.deliveryDatabase.DataBase.ShiftCounts
import catglo.com.deliveryDatabase.Order
import catglo.com.deliveryDatabase.TipTotalData
import catglo.com.deliverydroid.BuildConfig
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.shift.PastShiftActivity
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList
import java.util.Calendar

class OrderSummaryActivity : DeliveryDroidBaseActivity() {

    private var viewingShift: Int = 0
    private var use2ndView = false
    private var fragment: Fragment? = null
    //private GestureDetector gestureDector;
    private var viewFlipper: ViewSwitcher? = null
    private var numberOfOrders: TextView? = null
    private var date: TextView? = null
    //private TextView headerDetailText;
    private var previousShift: TextView? = null
    private var nextShift: TextView? = null
    private var thisShift: TextView? = null
    private var actionBarButton: View? = null
    private var dropDownMenu: LinearLayout? = null
    private var totalsButton: TextView? = null
    private var orderListButton: TextView? = null
    private var splitViewButton: TextView? = null

    var gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            if (e1 != null) {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            flingLeft()
                        } else {
                            flingRight()
                        }
                        result = true
                    }
                }
            }
            return result
        }
    }

    var gestureDetector = GestureDetector(gestureListener)
    var gestureTouchListener: OnTouchListener = OnTouchListener { arg0, event ->
        gestureDetector.onTouchEvent(event)
        false
    }
    lateinit var configCashOwedToStore: View
    internal var viewType: Int = 0
    private var orderListSummaryFragment: OrderSummaryListFragment? = null
    private var orderSummaryTotalsFragment: OrderSummaryTotalsFragment? = null
    private var prevShiftClickable: View? = null
    private var nextShiftClickable: View? = null
    private var moreMenuButton: View? = null
    lateinit var drawerLayout: DrawerLayout
    private var menuDrawer: View? = null
    private var addOrderButton: View? = null
    lateinit var customizeListView: View

    /*    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        //menu.add(0, SETTINGS, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, EXPORT_DATA, 0, R.string.ExportTo).setIcon(R.drawable.ic_menu_convert_csv);
        menu.add(0, GOTO_DATE, 0, R.string.goToDay).setIcon(android.R.drawable.ic_menu_day);
        menu.add(0, DELETE_SHIFT, 0, R.string.deleteThisShift).setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, EDIT_SHIFT_RECORD, 0, R.string.odo_and_hr).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, ADD_ORDER, 0, R.string.AddOrder).setIcon(android.R.drawable.ic_menu_add);
        return true;
    }
*/


    //For CSV Export
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    protected var fileName: String = ""
    protected var pd: ProgressDialog? = null
    //private TextView allOrders;

    var recordToDelete: Long = -1

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureTouchListener.onTouch(null, event)
        return super.onTouchEvent(event)
    }

    private fun flipViews(viewingShift: Int) {
        this.viewingShift = viewingShift
        if (use2ndView) {
            use2ndView = false
            loadFragment(R.id.fragmentContainer2, R.id.fragmentContainer2b)
        } else {
            use2ndView = true
            loadFragment(R.id.fragmentContainer, R.id.fragmentContainer1b)
        }

        setupHeaderViews()
    }

    private fun swtichToViewType(viewType: Int) {
        dropDownMenu?.visibility = View.GONE

        val prefEditor = sharedPreferences.edit()
        prefEditor.putInt("showListView", viewType)
        prefEditor.commit()

        this.viewType = viewType
        if (use2ndView) {
            loadFragment(R.id.fragmentContainer, R.id.fragmentContainer1b)
        } else {
            loadFragment(R.id.fragmentContainer2, R.id.fragmentContainer2b)
        }

        setupHeaderViews()
    }

    internal fun loadFragment(container: Int, container2: Int) {
        val args = Bundle()
        args.putInt("viewingShift", viewingShift)
        val transaction = fragmentManager.beginTransaction()

        findViewById<View>(container2).visibility = View.GONE
        (findViewById<View>(container2) as ViewGroup).removeAllViews()

        when (viewType) {
            VIEW_TYPE_SPLIT -> {
                orderListSummaryFragment = OrderSummaryListFragment()
                fragment = orderListSummaryFragment
                fragment?.arguments = args
                transaction.replace(container2, fragment)
                findViewById<View>(container2).visibility = View.VISIBLE
                orderSummaryTotalsFragment = OrderSummaryTotalsFragment()
                fragment = orderSummaryTotalsFragment
                orderListSummaryFragment = null
            }
            VIEW_TYPE_DETAILS -> {
                orderSummaryTotalsFragment = OrderSummaryTotalsFragment()
                fragment = orderSummaryTotalsFragment
                orderListSummaryFragment = null
            }
            VIEW_TYPE_LIST -> {
                orderListSummaryFragment = OrderSummaryListFragment()
                fragment = orderListSummaryFragment
                orderSummaryTotalsFragment = null
            }
        }
        fragment?.arguments = args
        transaction.replace(container, fragment)
        transaction.commit()
    }

    private fun setupHeaderViews() {
        dataBase?.let { db ->
            val counts = db.getShiftCounts(viewingShift)
            if (counts.prev < 0) {
                previousShift?.text = ""
                thisShift?.text = getString(R.string.Shift) + " 1"
            } else {
                previousShift?.text = getString(R.string.Shift) + " " + counts.prev
                thisShift?.text = getString(R.string.Shift) + " " + counts.cur
            }
            if (counts.next < 0) {
                nextShift?.text = ""
            } else {
                nextShift?.text = getString(R.string.Shift) + " " + counts.next
            }
            numberOfOrders?.text = "" + db?.getNumberOfOrdersInShift(viewingShift)
            val orders = db.getShiftOrderArray(viewingShift)
            if (orders.size > 0) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = orders[0].time.time
                date?.text = String.format("%tA %tb %te %tY", calendar, calendar, calendar, calendar)
            } else {
                date?.setText(R.string.today)
            }
        }
    }

    internal fun flingRight() {
        dataBase?.let { db ->
            viewFlipper?.inAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_in)
            viewFlipper?.outAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_left_out)
            if (viewingShift < db.curShift) {
                flipViews(db.getNextShiftNumber(viewingShift))
                viewFlipper?.showPrevious()
            }
        }
    }

    internal fun flingLeft() {
        dataBase?.let { db ->
            viewFlipper?.inAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_right_in)
            viewFlipper?.outAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_right_out)
            if (viewingShift > 1) {
                flipViews(db.getPrevoiusShiftNumber(viewingShift))
                viewFlipper?.showNext()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putInt("viewingShift", viewingShift)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_summary_activity)

        dataBase?.let { db ->
            drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
            menuDrawer = findViewById(R.id.menu_drawer)
            findViewById<View>(R.id.export_to_menu_button).setOnClickListener {
                exportDataDialog()
                drawerLayout.closeDrawers()
            }
            findViewById<View>(R.id.go_to_day_menu_button).setOnClickListener {
                goToDateDialog()
                drawerLayout.closeDrawers()
            }
            findViewById<View>(R.id.delete_shift_menu_button).setOnClickListener {
                confirmDeleteShiftDialog()
                drawerLayout.closeDrawers()
            }
            findViewById<View>(R.id.odometer_and_hours_menu_button).setOnClickListener {
                val i = Intent(applicationContext, PastShiftActivity::class.java)
                i.putExtra("ID", viewingShift)
                startActivity(i)
                drawerLayout.closeDrawers()
            }
            addOrderButton = findViewById(R.id.add_order_menu_button)
            addOrderButton?.setOnClickListener {
                val o = Order()
                o.primaryKey = db.add(o, viewingShift).toInt()
                val i = Intent(applicationContext, SummaryActivity::class.java)
                i.putExtra("openEdit", true)
                i.putExtra("DB Key", o.primaryKey)
                startActivity(i)
                drawerLayout.closeDrawers()
            }
            customizeListView = findViewById(R.id.customize_list_menu_button)
            configCashOwedToStore = findViewById(R.id.configure_cash_owed_to_store_menu_button)
            customizeListView.visibility = View.GONE
            configCashOwedToStore.visibility = View.GONE


            if (savedInstanceState == null) {
                viewingShift = db.curShift
            } else {
                viewingShift = savedInstanceState.getInt("viewingShift")
            }
            viewFlipper = findViewById<View>(R.id.viewSwitcher1) as ViewSwitcher


            //Shift number horizontal scroll bar
            previousShift = findViewById<View>(R.id.previousShift) as TextView
            nextShift = findViewById<View>(R.id.nextShift) as TextView
            thisShift = findViewById<View>(R.id.currentShift) as TextView

            prevShiftClickable = findViewById(R.id.prevShiftClickable)
            nextShiftClickable = findViewById(R.id.nextShiftClickable)

            prevShiftClickable?.setOnClickListener { flingLeft() }
            nextShiftClickable?.setOnClickListener { flingRight() }

            moreMenuButton = findViewById(R.id.moreClickable) as View
            moreMenuButton?.setOnClickListener {
                if (drawerLayout.isDrawerOpen(menuDrawer!!)) {
                    drawerLayout.closeDrawer(menuDrawer!!)
                } else {
                    if (viewingShift == DataBase.TodaysShiftCount) {
                        addOrderButton?.visibility = View.GONE
                    } else {
                        addOrderButton?.visibility = View.VISIBLE
                    }
                    drawerLayout.openDrawer(menuDrawer!!)
                }
            }

            //Action bar shift info
            numberOfOrders = findViewById<View>(R.id.osNumberOfDeliveries) as TextView
            date = findViewById<View>(R.id.osDate) as TextView
            setupHeaderViews()

            /* Action Bar */
            //headerDetailText  = (TextView)findViewById(R.id.headerDetailText);
            (findViewById<View>(R.id.backButton) as FrameLayout).setOnClickListener { finish() }
            //Action bar dropdown menu
            actionBarButton = findViewById(R.id.actionMenuDropdown) as View
            dropDownMenu = findViewById<View>(R.id.dropdownMenu) as LinearLayout
            totalsButton = findViewById<View>(R.id.totalsMenuButton) as TextView
            orderListButton = findViewById<View>(R.id.orderListMenuButton) as TextView
            splitViewButton = findViewById<View>(R.id.splitViewButton) as TextView
            actionBarButton?.setOnClickListener { dropDownMenu?.visibility = View.VISIBLE }
            totalsButton?.setOnClickListener { swtichToViewType(VIEW_TYPE_DETAILS) }
            orderListButton?.setOnClickListener { swtichToViewType(VIEW_TYPE_LIST) }
            splitViewButton?.setOnClickListener { swtichToViewType(VIEW_TYPE_SPLIT) }

            val display = windowManager.defaultDisplay
            val width = display.width  // deprecated
            val height = display.height  // deprecated
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            val x = Math.pow((width / dm.xdpi).toDouble(), 2.0)
            val y = Math.pow((height / dm.ydpi).toDouble(), 2.0)
            val screenInches = Math.sqrt(x + y)
            if (screenInches > 5) {
                viewType = sharedPreferences.getInt("showListView", VIEW_TYPE_SPLIT)
            } else {
                viewType = sharedPreferences.getInt("showListView", VIEW_TYPE_DETAILS)
            }
            use2ndView = false
            flipViews(viewingShift)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == 400) {
            setResult(400)
            finish()
        }
    }


    fun exportDataDialog() {

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            return
        }

        val items = arrayOf<CharSequence>(
            getString(R.string.today),
            getString(R.string.thisWeek),
            getString(R.string.ThisMonth),
            getString(R.string.ThisYear),
            getString(R.string.CustomDateRange)
        )
        val builder = AlertDialog.Builder(this@OrderSummaryActivity)
        builder.setTitle(getString(R.string.selectDateRange))
        builder.setItems(items) { dialog, item ->
            startDate = Calendar.getInstance()
            endDate = Calendar.getInstance()
            val now = Calendar.getInstance()
            now.timeInMillis = System.currentTimeMillis()
            startDate?.timeInMillis = System.currentTimeMillis()
            endDate?.timeInMillis = System.currentTimeMillis()
            when (item) {
                0 -> {
                    startDate?.set(Calendar.HOUR_OF_DAY, 0)
                    endDate?.set(Calendar.HOUR_OF_DAY, 0)
                    endDate?.add(Calendar.DAY_OF_YEAR, 1)
                    exportWhereDialog()
                }
                1 -> {
                    tools.getWorkWeekDates(now, startDate, endDate)
                    exportWhereDialog()
                }
                2 -> {
                    startDate?.set(Calendar.DATE, 0)
                    endDate?.add(Calendar.MONTH, 1)
                    startDate?.set(Calendar.DATE, 1)
                    exportWhereDialog()
                }
                3 -> {
                    startDate?.set(Calendar.DAY_OF_YEAR, 1)
                    endDate?.add(Calendar.YEAR, 1)
                    endDate?.set(Calendar.DAY_OF_YEAR, 0)
                    exportWhereDialog()
                }
                4 -> tools.getDateRangeDialog(startDate, endDate) { exportWhereDialog() }
            }
        }
        builder.setIcon(R.drawable.icon)
        builder.show()

    }

    fun exportWhereDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.Warning))
            .setMessage(getString(R.string.csv_warning_no_backup))
            .setPositiveButton(android.R.string.ok){ dialog, which ->
                pd = ProgressDialog.show(
                    this@OrderSummaryActivity,
                    "Exporting...",
                    "This may take a minute",
                    true,
                    false
                )

                GlobalScope.launch(Dispatchers.Default){
                    dataBase?.let{ db ->
                        val csvData = db.getCSVData(startDate, endDate, pd, this@OrderSummaryActivity)

                        fileName ="deliveryData.csv"
                        val file = File( Environment.getExternalStorageDirectory(), fileName)
                        try {
                            BufferedWriter(FileWriter(file)).run {
                                write(csvData)
                                flush()
                                close()
                            }

                            val emailIntent = Intent(Intent.ACTION_SEND)
                            emailIntent.type = "plain/text"
                            val subject = this@OrderSummaryActivity.getString(R.string.cvs_email_subject)
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "")
                            emailIntent.type = "text/csv"

                            emailIntent.putExtra(Intent.EXTRA_STREAM,
                                    FileProvider.getUriForFile(this@OrderSummaryActivity,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                        file ))

                            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            GlobalScope.launch(Dispatchers.Main) {
                                applicationContext.startActivity(emailIntent)
                            }

                        } catch (e: IOException) {
                            GlobalScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext,"Error Saving File",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    GlobalScope.launch(Dispatchers.Main) { pd?.dismiss() }
                }
        }
        .setNegativeButton(android.R.string.cancel){ dialog, which ->  }
        .show()

    }

    fun confirmDeleteRecordDialog() {
        if (recordToDelete < 0) return

        AlertDialog.Builder(this@OrderSummaryActivity).setIcon(R.drawable.icon).setTitle(
            "Delete this record?"
        ).setPositiveButton("Yes") { dialog, whichButton ->
            //Log.i("Delivery Driver", "User Y/N Delete Order");
            dataBase?.delete(recordToDelete)
        }.setNegativeButton("No") { dialog, whichButton -> /* User clicked Cancel so do some stuff */ }.show()
    }


    fun confirmDeleteShiftDialog() {
        dataBase?.let { db ->

            val tip = db.getTipTotal(
                this@OrderSummaryActivity, DataBase.Shift + "=" + viewingShift + " AND " + DataBase.Payed + " >= 0",
                "WHERE shifts.ID=$viewingShift", null
            )

            AlertDialog.Builder(this@OrderSummaryActivity).setIcon(R.drawable.icon).setTitle(
                "Delete this shift and all " + tip.deliveries + " order records?"
            ).setPositiveButton("Yes") { dialog, whichButton ->
                db.deleteShift(viewingShift)
                viewingShift = db.getNextShiftNumber(viewingShift)
            }.setNegativeButton("No") { dialog, whichButton -> /* User clicked Cancel so do some stuff */ }.show()
        }

    }

    fun goToDateDialog() {

        val alert = AlertDialog.Builder(this@OrderSummaryActivity)

        alert.setTitle("Go To Day")
        //alert.setMessage("Enter the date you want to show");

        val view = View.inflate(this@OrderSummaryActivity, R.layout.date_picker_dialog, null)

        // Set an EditText view to get user input
        val input = view.findViewById<View>(R.id.datePicker) as DatePicker
        alert.setView(view)

        alert.setPositiveButton("Ok") { dialog, whichButton ->
            dataBase?.let {db ->
                val c = Calendar.getInstance()
                c.set(input.year, input.month, input.dayOfMonth)

                val shift = db.findShiftForTime(c)
                if (shift >= 0) {
                    viewingShift = shift
                    if (orderListSummaryFragment != null) {
                        orderListSummaryFragment?.viewingShift = viewingShift
                        orderListSummaryFragment?.updateUI()
                    }
                    if (orderSummaryTotalsFragment != null) {
                        orderSummaryTotalsFragment?.viewingShift = viewingShift
                        orderSummaryTotalsFragment?.updateUI()
                    }
                }
            }
        }

        alert.setNegativeButton("Cancel") { dialog, whichButton ->
            // Canceled.
        }

        alert.show()

    }

    companion object {
        private val SETTINGS = 0
        //private static final int		TIP_STATS						= 1;
        //private static final int		SHOW_ADDRESS					= 2;
        //private static final int		SHOW_NOTES						= 3;
        private val DELETE_SHIFT = 4
        internal val EDIT_ID = 7
        internal val DELETE_ID = 8
        private val GOTO_DATE = 9
        private val EXPORT_DATA = 11
        private val EDIT_SHIFT_RECORD = 14
        private val ADD_ORDER = 15
        private val SWIPE_THRESHOLD = 1
        private val SWIPE_VELOCITY_THRESHOLD = 1

        internal val VIEW_TYPE_DETAILS = 0
        internal val VIEW_TYPE_LIST = 1
        internal val VIEW_TYPE_SPLIT = 2
    }

}