package catglo.com.deliverydroid.orderSummary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.DataBase.ShiftCounts;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.shift.ShiftStartEndActivity;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class OrderSummaryActivity extends DeliveryDroidBaseActivity
{
    private static final int		SETTINGS						= 0;
    //private static final int		TIP_STATS						= 1;
    //private static final int		SHOW_ADDRESS					= 2;
    //private static final int		SHOW_NOTES						= 3;
    private static final int        DELETE_SHIFT                    = 4;
    static final int		        EDIT_ID							= 7;
    static final int		        DELETE_ID						= 8;
    private static final int        GOTO_DATE                       = 9;
    private static final int		EXPORT_DATA						= 11;
    private static final int 		EDIT_SHIFT_RECORD				= 14;
    private static final int 		ADD_ORDER                       = 15;

    private int viewingShift;
    private boolean use2ndView=false;
    private Fragment fragment;
    //private GestureDetector gestureDector;
    private ViewSwitcher viewFlipper;
    private TextView numberOfOrders;
    private TextView date;
    //private TextView headerDetailText;
    private TextView previousShift;
    private TextView nextShift;
    private TextView thisShift;
    private View actionBarButton;
    private LinearLayout dropDownMenu;
    private TextView totalsButton;
    private TextView orderListButton;
    private TextView splitViewButton;

    public SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            if (e1!=null) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            flingLeft();
                        } else {
                            flingRight();
                        }
                        result = true;
                    }
                }
            }
            return result;
        }
    };

    @SuppressWarnings("deprecation")
    public GestureDetector gestureDetector = new GestureDetector(gestureListener);
    public OnTouchListener gestureTouchListener = new OnTouchListener(){public boolean onTouch(View arg0, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }};
    public View configCashOwedToStore;

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureTouchListener.onTouch(null, event);
        return super.onTouchEvent(event);
    }

    private void flipViews(int viewingShift) {
        this.viewingShift=viewingShift;
        if (use2ndView) {
            use2ndView=false;
            loadFragment(R.id.fragmentContainer2,R.id.fragmentContainer2b);
        } else {
            use2ndView=true;
            loadFragment(R.id.fragmentContainer,R.id.fragmentContainer1b);
        }

        setupHeaderViews();
    }
    private static final int SWIPE_THRESHOLD = 1;
    private static final int SWIPE_VELOCITY_THRESHOLD = 1;

    static final int VIEW_TYPE_DETAILS=0;
    static final int VIEW_TYPE_LIST=1;
    static final int VIEW_TYPE_SPLIT=2;
    int viewType;
    private OrderSummaryListFragment orderListSummaryFragment;
    private OrderSummaryTotalsFragment orderSummaryTotalsFragment;
    private View prevShiftClickable;
    private View nextShiftClickable;
    private View moreMenuButton;
    DrawerLayout drawerLayout;
    private View menuDrawer;
    private View addOrderButton;
    View customizeListView;

    private void swtichToViewType(int viewType) {
        dropDownMenu.setVisibility(View.GONE);

        Editor prefEditor = sharedPreferences.edit();
        prefEditor.putInt("showListView", viewType);
        prefEditor.commit();

        this.viewType = viewType;
        if (use2ndView) {
            loadFragment(R.id.fragmentContainer,R.id.fragmentContainer1b);
        } else {
            loadFragment(R.id.fragmentContainer2,R.id.fragmentContainer2b);
        }

        setupHeaderViews();
    }

    void loadFragment(int container, int container2){
        Bundle args = new Bundle();
        args.putInt("viewingShift", viewingShift);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        findViewById(container2).setVisibility(View.GONE);
        ((ViewGroup)findViewById(container2)).removeAllViews();

        switch (viewType){
            case VIEW_TYPE_SPLIT:
                orderListSummaryFragment = new OrderSummaryListFragment();
                fragment = orderListSummaryFragment;
                fragment.setArguments(args);
                transaction.replace(container2, fragment);
                findViewById(container2).setVisibility(View.VISIBLE);
            case VIEW_TYPE_DETAILS:
                orderSummaryTotalsFragment = new OrderSummaryTotalsFragment();
                fragment = orderSummaryTotalsFragment;
                orderListSummaryFragment = null;
                break;
            case VIEW_TYPE_LIST:
                orderListSummaryFragment = new OrderSummaryListFragment();
                fragment = orderListSummaryFragment;
                orderSummaryTotalsFragment = null;
                break;
        }
        fragment.setArguments(args);
        transaction.replace(container, fragment);
        transaction.commit();
    }

    private void setupHeaderViews() {
        ShiftCounts counts = dataBase.getShiftCounts(viewingShift);
        if (counts.prev<=0){
            previousShift.setText("");
            thisShift.setText(getString(R.string.Shift)+" 1");
        }else{
            previousShift.setText(getString(R.string.Shift)+" "+counts.prev);
            thisShift.setText(getString(R.string.Shift)+" "+counts.cur);
        }
        if (counts.next<=0){
            nextShift.setText("");
        } else {
            nextShift.setText(getString(R.string.Shift)+" "+counts.next);
        }
        numberOfOrders.setText(""+dataBase.getNumberOfOrdersInShift(viewingShift));
        ArrayList<Order> orders = dataBase.getShiftOrderArray(viewingShift);
        if (orders.size()>0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(orders.get(0).time.getTime());
            date.setText(String.format("%tA %tb %te %tY", calendar, calendar, calendar, calendar));
        } else {
            date.setText(R.string.today);
        }
    }
    void flingRight(){
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_out));
        if (viewingShift < dataBase.getCurShift()) {
            flipViews(dataBase.getNextShiftNumber(viewingShift));
            viewFlipper.showPrevious();
        }
    }
    void flingLeft(){
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_out));
        if (viewingShift > 0) {
            flipViews(dataBase.getPrevoiusShiftNumber(viewingShift));
            viewFlipper.showNext();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt("viewingShift", viewingShift);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_summary_activity);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuDrawer = findViewById(R.id.menu_drawer);
        findViewById(R.id.export_to_menu_button).setOnClickListener(new OnClickListener(){@SuppressWarnings("deprecation")
                                                                                          public void onClick(View arg0) {
            exportDataDialog();
            drawerLayout.closeDrawers();
        }});
        findViewById(R.id.go_to_day_menu_button).setOnClickListener(new OnClickListener(){@SuppressWarnings("deprecation")
                                                                                          public void onClick(View arg0) {
            goToDateDialog();
            drawerLayout.closeDrawers();
        }});
        findViewById(R.id.delete_shift_menu_button).setOnClickListener(new OnClickListener(){@SuppressWarnings("deprecation")
                                                                                             public void onClick(View arg0) {
            confirmDeleteShiftDialog();
            drawerLayout.closeDrawers();
        }});
        findViewById(R.id.odometer_and_hours_menu_button).setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
            Intent  i = new Intent(getApplicationContext(),ShiftStartEndActivity.class);
            i.putExtra("ID", viewingShift);
            startActivity(i);
            drawerLayout.closeDrawers();
        }});
        addOrderButton = findViewById(R.id.add_order_menu_button);
        addOrderButton.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
            Order o = new Order();
            o.primaryKey = (int) dataBase.add(o,viewingShift);
            Intent  i = new Intent(getApplicationContext(),SummaryActivity.class);
            i.putExtra("openEdit", true);
            i.putExtra("DB Key", o.primaryKey);
            startActivity(i);
            drawerLayout.closeDrawers();
        }});
        customizeListView = findViewById(R.id.customize_list_menu_button);
        configCashOwedToStore = findViewById(R.id.configure_cash_owed_to_store_menu_button);
        customizeListView.setVisibility(View.GONE);
        configCashOwedToStore.setVisibility(View.GONE);


        if (savedInstanceState==null){
            viewingShift = dataBase.getCurShift();
        } else {
            viewingShift = savedInstanceState.getInt("viewingShift");
        }
        viewFlipper = (ViewSwitcher)findViewById(R.id.viewSwitcher1);


        //Shift number horizontal scroll bar
        previousShift = (TextView)findViewById(R.id.previousShift);
        nextShift = (TextView)findViewById(R.id.nextShift);
        thisShift = (TextView)findViewById(R.id.currentShift);

        prevShiftClickable = findViewById(R.id.prevShiftClickable);
        nextShiftClickable = findViewById(R.id.nextShiftClickable);

        prevShiftClickable.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            flingLeft();
        }});
        nextShiftClickable.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            flingRight();
        }});

        moreMenuButton     = (View)findViewById(R.id.moreClickable);
        moreMenuButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            if (drawerLayout.isDrawerOpen(menuDrawer)) {
                drawerLayout.closeDrawer(menuDrawer);
            } else {
                if (viewingShift == DataBase.TodaysShiftCount){
                    addOrderButton.setVisibility(View.GONE);
                } else {
                    addOrderButton.setVisibility(View.VISIBLE);
                }
                drawerLayout.openDrawer(menuDrawer);
            }
        }});

        //Action bar shift info
        numberOfOrders = (TextView)findViewById(R.id.osNumberOfDeliveries);
        date = (TextView)findViewById(R.id.osDate);
        setupHeaderViews();
		
		/* Action Bar */
        //headerDetailText  = (TextView)findViewById(R.id.headerDetailText);
        ((FrameLayout)findViewById(R.id.backButton)).setOnClickListener(new OnClickListener(){public void onClick(View v) {
            finish();
        }});
        //Action bar dropdown menu
        actionBarButton = (View)findViewById(R.id.actionMenuDropdown);
        dropDownMenu = (LinearLayout)findViewById(R.id.dropdownMenu);
        totalsButton = (TextView)findViewById(R.id.totalsMenuButton);
        orderListButton = (TextView)findViewById(R.id.orderListMenuButton);
        splitViewButton = (TextView)findViewById(R.id.splitViewButton);
        actionBarButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            dropDownMenu.setVisibility(View.VISIBLE);
        }});
        totalsButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            swtichToViewType(VIEW_TYPE_DETAILS);
        }});
        orderListButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            swtichToViewType(VIEW_TYPE_LIST);
        }});
        splitViewButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
            swtichToViewType(VIEW_TYPE_SPLIT);
        }});

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(width/dm.xdpi,2);
        double y = Math.pow(height/dm.ydpi,2);
        double screenInches = Math.sqrt(x+y);
        if (screenInches>5){
            viewType = sharedPreferences.getInt("showListView", VIEW_TYPE_SPLIT);
        } else {
            viewType = sharedPreferences.getInt("showListView", VIEW_TYPE_DETAILS);
        }
        use2ndView = false;
        flipViews(viewingShift);



    }




    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == 400) {
            setResult(400);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        //menu.add(0, SETTINGS, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, EXPORT_DATA, 0, R.string.ExportTo).setIcon(R.drawable.ic_menu_convert_csv);
        menu.add(0, GOTO_DATE, 0, R.string.goToDay).setIcon(android.R.drawable.ic_menu_day);
        menu.add(0, DELETE_SHIFT, 0, R.string.deleteThisShift).setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, EDIT_SHIFT_RECORD, 0, R.string.odo_and_hr).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, ADD_ORDER, 0, R.string.AddOrder).setIcon(android.R.drawable.ic_menu_add);
        return true;
    }



    //For CSV Export
    private Calendar startDate;
    private Calendar endDate;
    protected String fileName;
    protected ProgressDialog pd;
    //private TextView allOrders;

    long					recordToDelete= -1;


    public void exportDataDialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(OrderSummaryActivity.this);
        final CharSequence[] items = {getString(R.string.today),
                getString(R.string.thisWeek),
                getString(R.string.ThisMonth),
                getString(R.string.ThisYear),
                getString(R.string.CustomDateRange)};
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
        builder.setTitle(getString(R.string.selectDateRange));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressWarnings("deprecation")
            public void onClick(DialogInterface dialog, int item) {
                startDate = Calendar.getInstance();
                endDate = Calendar.getInstance();
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(System.currentTimeMillis());
                startDate.setTimeInMillis(System.currentTimeMillis());
                endDate.setTimeInMillis(System.currentTimeMillis());
                switch (item) {
                    case 0: startDate.set(Calendar.HOUR_OF_DAY, 0);
                        endDate.set(Calendar.HOUR_OF_DAY, 0);
                        endDate.add(Calendar.DAY_OF_YEAR, 1);
                        exportWhereDialog();
                        break;
                    case 1: tools.getWorkWeekDates(now,startDate,endDate);
                        exportWhereDialog();
                        break;
                    case 2: startDate.set(Calendar.DATE, 0);
                        endDate.add(Calendar.MONTH, 1);
                        startDate.set(Calendar.DATE, 1);
                        exportWhereDialog();
                        break;
                    case 3: startDate.set(Calendar.DAY_OF_YEAR, 1);
                        endDate.add(Calendar.YEAR, 1);
                        endDate.set(Calendar.DAY_OF_YEAR, 0);
                        exportWhereDialog();
                        break;
                    case 4:
                        tools.getDateRangeDialog(startDate,endDate,new OnDismissListener(){public void onDismiss(DialogInterface dialog) {
                            exportWhereDialog();
                        }});
                        break;
                }

            }
        });
        builder.setIcon(R.drawable.icon);
        builder.show();

    }

    public void exportWhereDialog(){
        DialogFragment dialogFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {AlertDialog.Builder alert = new AlertDialog.Builder(OrderSummaryActivity.this);
                AlertDialog.Builder builder;
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.export_csv_how_dialog, (ViewGroup) findViewById(R.id.linearLayout1));

                //User reported bad export dialog, found this stack overflow fix.
                LayoutParams params = getWindow().getAttributes();
                params.height = LayoutParams.MATCH_PARENT;
                getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                final Spinner exportTo = (Spinner) layout.findViewById(R.id.deliveryAreaSpinner);
                final EditText fileNameEditor = (EditText) layout.findViewById(R.id.hourlyPayRate);
                fileNameEditor.setText("deliveryData.csv");
                exportTo.setOnItemSelectedListener(new OnItemSelectedListener(){
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        switch (arg2){
                            case 0: fileNameEditor.setText("deliveryData.csv");
                                fileNameEditor.setEnabled(false);
                                break;
                            case 1:  fileNameEditor.setEnabled(true);
                                break;
                        }
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {}
                });
                String[] items = new String[] {
                        getString(R.string.email),     //0
                        getString(R.string.sdcard)};   //1
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(OrderSummaryActivity.this,android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                exportTo.setAdapter(adapter);
                Button exitButton   = (Button) layout.findViewById(R.id.button2);
                Button exportButton = (Button) layout.findViewById(R.id.setShiftTimesToOrderTimes);
                builder = new AlertDialog.Builder(OrderSummaryActivity.this);
                builder.setView(layout);
                final AlertDialog dialog = builder.create();
                exitButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
                    dialog.dismiss();
                }});
                exportButton.setOnClickListener(new OnClickListener(){

                    public void onClick(View v) {

                        pd = ProgressDialog.show(OrderSummaryActivity.this, "Exporting...", "This may take a minute", true, false);//cancelable)//.show(this, "Working..", "Calculating Pi", true, false);

                        Thread csvThread = new Thread(new Runnable(){public void run(){
                            String csvData = dataBase.getCSVData(startDate,endDate,pd,OrderSummaryActivity.this);
                            if (exportTo.getSelectedItemPosition()==1){
                                fileName = fileNameEditor.getEditableText().toString()+".csv";
                            } else {
                                fileName = "deliveryData.csv";
                            }
                            File sdCard = Environment.getExternalStorageDirectory();
                            File dir = new File (sdCard.getAbsolutePath());
                            File file = new File(dir, fileName);
                            try {
                                FileWriter fstream = new FileWriter(file);
                                BufferedWriter out = new BufferedWriter(fstream);
                                out.write(csvData);
                                out.flush();
                                out.close();

                            } catch (final IOException e) {

                                e.printStackTrace();
                            }
                            if (exportTo.getSelectedItemPosition()==0){
                                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setType("plain/text");
                                String subject = OrderSummaryActivity.this.getString(R.string.cvs_email_subject);
                                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                                emailIntent.setType("text/csv");
                                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/"+fileName));
                                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                runOnUiThread(new Runnable(){public void run(){
                                    try {

                                        getApplicationContext().startActivity(emailIntent);
                                    } catch(Exception e){
                                        e.printStackTrace();
                                        //	Toast.makeText(getApplicationContext(), "ERROR - Could not lounch system e-mail app", Toast.LENGTH_LONG).show();

                                    }
                                }});
                            }
                            pd.dismiss();
                        }});
                        csvThread.start();



                        dialog.dismiss();
                    }});

                return dialog;
            }
        };
        dialogFragment.show(getFragmentManager(), "");
    }

    public void confirmDeleteRecordDialog(){
        if (recordToDelete < 0) return;
        DialogFragment dialogFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new AlertDialog.Builder(OrderSummaryActivity.this).setIcon(R.drawable.icon).setTitle(
                        "Delete this record?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        //Log.i("Delivery Driver", "User Y/N Delete Order");
                        dataBase.delete(recordToDelete);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
	
						/* User clicked Cancel so do some stuff */
                    }
                }).create();
            }
        };
        dialogFragment.show(getFragmentManager(), "");
    }

    public void confirmDeleteShiftDialog(){
        DialogFragment dialogFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                TipTotalData tip = dataBase.getTipTotal(OrderSummaryActivity.this,DataBase.Shift+"="+viewingShift+" AND "+DataBase.Payed+" >= 0",
                        "WHERE shifts.ID="+viewingShift);

                return new AlertDialog.Builder(OrderSummaryActivity.this).setIcon(R.drawable.icon).setTitle(
                        "Delete this shift and all "+tip.deliveries+" order records?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        dataBase.deleteShift(viewingShift);
                        viewingShift=dataBase.getNextShiftNumber(viewingShift);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
	
						/* User clicked Cancel so do some stuff */
                    }
                }).create();
            }
        };
        dialogFragment.show(getFragmentManager(), "");
    }

    public void goToDateDialog(){
        DialogFragment dialogFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                AlertDialog.Builder alert = new AlertDialog.Builder(OrderSummaryActivity.this);

                alert.setTitle("Go To Day");
                //alert.setMessage("Enter the date you want to show");

                View view = View.inflate(OrderSummaryActivity.this, R.layout.date_picker_dialog, null);

                // Set an EditText view to get user input
                final DatePicker input =  (DatePicker)view.findViewById(R.id.datePicker);
                alert.setView(view);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Calendar c = Calendar.getInstance();
                        c.set(input.getYear(), input.getMonth(), input.getDayOfMonth());

                        int shift = dataBase.findShiftForTime(c);
                        if (shift>=0){
                            viewingShift=shift;
                            if (orderListSummaryFragment!=null) {
                                orderListSummaryFragment.viewingShift = viewingShift;
                                orderListSummaryFragment.updateUI();
                            }
                            if (orderSummaryTotalsFragment!=null){
                                orderSummaryTotalsFragment.viewingShift = viewingShift;
                                orderSummaryTotalsFragment.updateUI();
                            }
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                return alert.create();
            }
        };
        dialogFragment.show(getFragmentManager(), "");
    }

}