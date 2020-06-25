package catglo.com.deliverydroid;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliveryDatabase.TipTotalData.PayRatePieriod;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class TipHistoryActivity extends DeliveryDroidBaseActivity {

    boolean userDates = false;
    private Button email;
    private EditText startDateField;
    private EditText endDateField;
    private TextView tipsMade;
    private TextView driverEarnings;
    private TextView bestTip;
    private TextView averageTip;
    private TextView worstTip;
    private TextView totalDeliveries;
    private Button text;
    protected Calendar startDate;
    protected Calendar endDate;
    private TextView milesDriven;
    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wendsday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private CheckBox sunday;
    private ViewGroup dateRangeContent;
    private Editor prefEditor;
    private TextView[] hoursWorked = new TextView[3];
    private TextView[] hoursWorkedTitle = new TextView[3];
    private TextView averagePayRate;
    private View moreClickable;
    private ScrollView scrollView;
    private View filterLayout;
    private DrawerLayout drawerLayout;
    private View menuDrawer;
    private Button filterMenuButton;

    @Override
    public void onPause() {
        super.onPause();
        Editor editor = getSharedPreferences().edit();
        editor.putInt("TipScrollPosition", scrollView.getScrollY());
        editor.apply();
    }

    //TODO: Save and restore the filter settings like if its this week recalculate to whatever week it is
    //      and if its a custom range those dates exactly.
    @Override
    public void onResume() {
        super.onResume();
        scrollView.scrollTo(0, getSharedPreferences().getInt("TipScrollPosition", 0));
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_totals);

        final Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuDrawer = findViewById(R.id.menu_drawer);

        findViewById(R.id.email_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                emailResults();
                drawerLayout.closeDrawers();
            }
        });

        findViewById(R.id.text_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                smsResults();
                drawerLayout.closeDrawers();
            }
        });

        filterMenuButton = (Button) findViewById(R.id.toggle_menu_button);
        filterMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilter();
                drawerLayout.closeDrawers();
            }
        });


        startDate = (Calendar) now.clone();
        startDate.setTimeInMillis(getSharedPreferences().getLong("tipHistorystartDate", (((Calendar) now.clone()).getTimeInMillis())));
        endDate = (Calendar) now.clone();
        endDate.setTimeInMillis(getSharedPreferences().getLong("tipHistoryendDate", (((Calendar) now.clone()).getTimeInMillis())));

        moreClickable = findViewById(R.id.moreClickable);
        moreClickable.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (drawerLayout.isDrawerOpen(menuDrawer)) {
                    drawerLayout.closeDrawer(menuDrawer);
                } else {
                    drawerLayout.openDrawer(menuDrawer);
                }
            }
        });

        filterLayout = findViewById(R.id.filterLayout);
        if (getSharedPreferences().getBoolean("show_tip_totals_day_filter", true)) {
            filterLayout.setVisibility(View.VISIBLE);
            filterMenuButton.setText(R.string.Hide_weekday_filters);
        } else {
            filterLayout.setVisibility(View.GONE);
            filterMenuButton.setText(R.string.Show_weekday_filters);
        }

        scrollView = (ScrollView) findViewById(R.id.sewidget29);

        monday = (CheckBox) findViewById(R.id.monday);
        tuesday = (CheckBox) findViewById(R.id.tuesday);
        wendsday = (CheckBox) findViewById(R.id.wendsday);
        thursday = (CheckBox) findViewById(R.id.thursday);
        friday = (CheckBox) findViewById(R.id.friday);
        saturday = (CheckBox) findViewById(R.id.saturday);
        sunday = (CheckBox) findViewById(R.id.sunday);
        monday.setChecked(getSharedPreferences().getBoolean("tipHistoryMonday", true));
        tuesday.setChecked(getSharedPreferences().getBoolean("tipHistoryTuesday", true));
        wendsday.setChecked(getSharedPreferences().getBoolean("tipHistoryWendsday", true));
        thursday.setChecked(getSharedPreferences().getBoolean("tipHistoryThursday", true));
        friday.setChecked(getSharedPreferences().getBoolean("tipHistoryFriday", true));
        saturday.setChecked(getSharedPreferences().getBoolean("tipHistorySaturday", true));
        sunday.setChecked(getSharedPreferences().getBoolean("tipHistorySunday", true));

        monday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistoryMonday", isChecked);
                prefEditor.apply();
            }
        });
        tuesday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistoryTuesday", isChecked);
                prefEditor.apply();
            }
        });
        wendsday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistoryWendsday", isChecked);
                prefEditor.apply();
            }
        });
        thursday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistoryThursday", isChecked);
                prefEditor.apply();
            }
        });
        friday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistoryFriday", isChecked);
                prefEditor.apply();
            }
        });
        saturday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistorySaturday", isChecked);
                prefEditor.apply();
            }
        });
        sunday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUI();
                prefEditor.putBoolean("tipHistorySunday", isChecked);
                prefEditor.apply();
            }
        });


        ((View) findViewById(R.id.backButton)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        prefEditor = getSharedPreferences().edit();

        dateRangeContent = (ViewGroup) findViewById(R.id.dateRangeContent);


        if (getSharedPreferences().getBoolean("dateRangeIndicator", true)) {
            dateRangeContent.setVisibility(View.VISIBLE);
        } else {
            dateRangeContent.setVisibility(View.GONE);
        }

        String[] listValues = {
                getString(R.string.today),
                getString(R.string.thisWeek),
                getString(R.string.ThisMonth),
                getString(R.string.ThisYear),
                getString(R.string.CustomDateRange)};
        Spinner dateRangeSpinner = (Spinner) findViewById(R.id.deliveryAreaSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.tip_totals_spinner_in_toolbar, listValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(adapter);
        int selection = getSharedPreferences().getInt("tipHistorySpinnerDefault", 1);
        dateRangeSpinner.setSelection(selection);

        dateRangeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                prefEditor.putInt("tipHistorySpinnerDefault", arg2);
                prefEditor.apply();
                startDate = Calendar.getInstance();
                startDate.setTimeInMillis(System.currentTimeMillis());
                endDate = Calendar.getInstance();
                endDate.setTimeInMillis(System.currentTimeMillis());

                setDateFieldsForSelection(arg2);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });




        startDateField = (EditText) findViewById(R.id.hourlyPayRate);
        endDateField = (EditText) findViewById(R.id.totalCash);

        getTools().getWorkWeekDates(now, startDate, endDate);
        startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
        endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));

        OnTouchListener dateTouchListener = new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getTools().getDateRangeDialog(startDate, endDate, new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                            endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                            updateUI();
                        }
                    });
                    return true;
                }
                return false;
            }
        };

        startDateField.setOnTouchListener(dateTouchListener);
        endDateField.setOnTouchListener(dateTouchListener);

        tipsMade = (TextView) findViewById(R.id.tipsMade);

        driverEarnings = (TextView) findViewById(R.id.driverEarnings);
        bestTip = (TextView) findViewById(R.id.bestTip);
        averageTip = (TextView) findViewById(R.id.averageTip);
        worstTip = (TextView) findViewById(R.id.worstTip);
        totalDeliveries = (TextView) findViewById(R.id.totalDeliveries);
        milesDriven = (TextView) findViewById(R.id.milesDriven);

        hoursWorkedTitle[0] = (TextView) findViewById(R.id.textView20);
        hoursWorkedTitle[1] = (TextView) findViewById(R.id.textView20_b);
        hoursWorkedTitle[2] = (TextView) findViewById(R.id.textView20_c);
        hoursWorked[0] = (TextView) findViewById(R.id.textView19);
        hoursWorked[1] = (TextView) findViewById(R.id.textView19_b);
        hoursWorked[2] = (TextView) findViewById(R.id.textView19_c);

        averagePayRate = (TextView) findViewById(R.id.averagePayRate);

        setDateFieldsForSelection(selection);
        updateUI();
    }

    private void setDateFieldsForSelection(int arg2) {
        Calendar now = Calendar.getInstance();
        switch (arg2) {
            case 0: //Today
                startDate.set(Calendar.HOUR_OF_DAY, 0);
                endDate.set(Calendar.HOUR_OF_DAY, 0);
                endDate.add(Calendar.DAY_OF_YEAR, 1);
                startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                updateUI();
                break;

            case 1: //This Week
                getTools().getWorkWeekDates(now, startDate, endDate);
                startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                updateUI();
                break;

            case 2: //This Month
                endDate.set(Calendar.DATE, 1);
                endDate.add(Calendar.MONTH, 1);
                startDate.set(Calendar.DATE, 1);
                startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                updateUI();
                break;

            case 3: //This Year
                startDate.set(Calendar.DAY_OF_YEAR, 1);
                endDate.add(Calendar.YEAR, 1);
                endDate.set(Calendar.DAY_OF_YEAR, 0);
                startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                updateUI();
                break;

            case 4: //Custom Date Range
                getTools().getDateRangeDialog(startDate, endDate, new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        startDateField.setText((startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR));
                        endDateField.setText((endDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
                        updateUI();
                    }
                });
                break;
        }
    }

    String getEmailText(TipTotalData tips) {
        return "" +
                getString(R.string.tipsHistoryExportEmailTop) + "\n" +
                getString(R.string.startDate) + ":" + (startDate.get(Calendar.MONTH) + 1) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR) + "  " +
                getString(R.string.endDate) + ":" + (startDate.get(Calendar.MONTH) + 1) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR) + "  " +
                getString(R.string.deliveries) + ":" + (tips.deliveries) + "\n" +
                getString(R.string.tipsMade) + ":" + Utils.getFormattedCurrency(tips.payed - tips.cost) + "\n" +
                getString(R.string.DriverEarnings) + ":" + Utils.getFormattedCurrency(tips.total) + "\n" +
                getString(R.string.bestTip) + ":" + Utils.getFormattedCurrency(tips.bestTip) + "\n" +
                getString(R.string.averageTip) + ":" + Utils.getFormattedCurrency(tips.averageTip) + "\n" +
                getString(R.string.worstTip) + ":" + Utils.getFormattedCurrency(tips.worstTip) + "\n" +
                getString(R.string.hoursWorked) + ":" + Utils.getFormattedCurrency(tips.hours) + "\n" +
                getString(R.string.milesDriven) + ":" + tips.odometerTotal + "\n";
    }

    String mileagePrefix() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean mileagePayForUndeliverable = prefs.getBoolean("mileagePayForUndeliverable", true);
        String mileageString = " (Payed >= 0";
        if (mileagePayForUndeliverable) {
            mileageString += " OR undeliverable = '1')";
        } else {
            mileageString += ")";
        }
        return mileageString;
    }

    void updateUI() {
        //sharedPreferences.getLong("tipHistoryendDate", (((Calendar) now.clone()).getTimeInMillis()))
        prefEditor.putLong("tipHistoryendDate", endDate.getTimeInMillis());
        prefEditor.putLong("tipHistorystartDate", startDate.getTimeInMillis());
        prefEditor.apply();

        String hoursWorkedTableWhere = ""
                + "WHERE shifts.`TimeStart` >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate)
                + "' AND shifts.`TimeStart` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "' ";
        
        String sqlQuery = mileagePrefix() + " AND `"
                        + DataBase.Time + "` >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate)
                        + "' AND `" + DataBase.Time + "` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "' ";

        String shiftTableWhere = ""
                + "WHERE shifts.`TimeStart` >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate)
                + "' AND shifts.`TimeStart` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "' ";


        if (sunday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '0'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '0'";
        }
        if (monday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '1'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '1'";
        }
        if (tuesday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '2'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '2'";
        }
        if (wendsday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '3'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '3'";
        }
        if (thursday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '4'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '4'";
        }
        if (friday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '5'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '5'";
        }
        if (saturday.isChecked() == false) {
            sqlQuery = sqlQuery + " AND `weekday` IS NOT '6'";
            hoursWorkedTableWhere = hoursWorkedTableWhere + " AND `weekday` IS NOT '6'";
        }


        TipTotalData tips = getDataBase().getTipTotal(getApplicationContext(), sqlQuery, hoursWorkedTableWhere, shiftTableWhere );


        tipsMade.setText(Utils.getFormattedCurrency(tips.payed - tips.cost));
        driverEarnings.setText(Utils.getFormattedCurrency(tips.total));
        bestTip.setText(Utils.getFormattedCurrency(tips.bestTip));
        if (Float.isNaN(tips.averageTip) == false) averageTip.setText(Utils.getFormattedCurrency(tips.averageTip));
        worstTip.setText(Utils.getFormattedCurrency(tips.worstTip));
        totalDeliveries.setText("" + tips.deliveries);
        milesDriven.setText("" + tips.odometerTotal);

        hoursWorked[0].setText(String.format("%.2f", tips.hours));

        float averagePayRateValue = 0;
        float totalPay = 0;
        float totalHours = 0;

        ArrayList<PayRatePieriod> payRates = new ArrayList<PayRatePieriod>();
        for (String key : tips.payRatePieriods.keySet()) {
            PayRatePieriod p = tips.payRatePieriods.get(key);
            payRates.add(p);
            totalPay += p.hourlyPay * p.hours;
            totalHours += p.hours;
        }
        averagePayRateValue = (totalPay / totalHours) * 60;

        averagePayRate.setText(Utils.getFormattedCurrency(averagePayRateValue));


        //If there was more than 1 pay rate show the breakdown of the top 2
        if (payRates.size() > 1) {

            Collections.sort(payRates, new Comparator<PayRatePieriod>() {
                public int compare(PayRatePieriod rhs, PayRatePieriod lhs) {
                    return (int) (lhs.hours - rhs.hours);
                }
            });

            PayRatePieriod prp = payRates.get(0);
            DecimalFormat df = new DecimalFormat("#.##");
            hoursWorked[1].setText(df.format(prp.hours) + "");
            hoursWorkedTitle[1].setText("Hours @ " + Utils.getFormattedCurrency(prp.hourlyPay * 60));

            prp = payRates.get(1);
            hoursWorked[2].setText(df.format(prp.hours) + "");
            hoursWorkedTitle[2].setText("Hours @ " + Utils.getFormattedCurrency(prp.hourlyPay * 60));


            hoursWorked[1].setVisibility(View.VISIBLE);
            hoursWorkedTitle[1].setVisibility(View.VISIBLE);
            hoursWorked[2].setVisibility(View.VISIBLE);
            hoursWorkedTitle[2].setVisibility(View.VISIBLE);

        } else {
            hoursWorked[1].setVisibility(View.GONE);
            hoursWorked[2].setVisibility(View.GONE);
            hoursWorkedTitle[1].setVisibility(View.GONE);
            hoursWorkedTitle[2].setVisibility(View.GONE);
        }
    }

    void emailResults() {
        TipTotalData tips = getDataBase().getTipTotal(getApplicationContext(),
                mileagePrefix() + " AND `" + DataBase.Time + "` >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate) +
                        "' AND `" + DataBase.Time + "` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "'",
                "WHERE shifts.TimeStart >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate) +
                        "' AND shifts.`TimeEnd` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "'",null);

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.putExtra(Intent.EXTRA_TEXT, getEmailText(tips));
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + getString(R.string.tipHistory) +
                startDate.get(Calendar.MONTH) + "/" + startDate.get(Calendar.DAY_OF_MONTH) + "/" + startDate.get(Calendar.YEAR) + "..." + startDate.get(Calendar.MONTH) + "/" + endDate.get(Calendar.DAY_OF_MONTH) + "/" + endDate.get(Calendar.YEAR));
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    void smsResults() {
        //TODO: Update to include new fields like e-mail
        TipTotalData tips = getDataBase().getTipTotal(getApplicationContext(),
                mileagePrefix() + " AND `" + DataBase.Time + "` >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate) +
                        "' AND `" + DataBase.Time + "` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "'",
                "WHERE shifts.TimeStart >= '" + String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate) +
                        "' AND shifts.`TimeEnd` <= '" + String.format("%3$tY-%3$tm-%3$td", endDate, endDate, endDate) + "'"
        ,null);

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", getEmailText(tips));
        startActivity(Intent.createChooser(sendIntent, "Send text..."));

    }

    void toggleFilter() {
        Editor editor = getSharedPreferences().edit();
        if (getSharedPreferences().getBoolean("show_tip_totals_day_filter", true)) {
            filterLayout.setVisibility(View.GONE);
            editor.putBoolean("show_tip_totals_day_filter", false);
            filterMenuButton.setText(R.string.Show_weekday_filters);
        } else {
            filterLayout.setVisibility(View.VISIBLE);
            editor.putBoolean("show_tip_totals_day_filter", true);
            filterMenuButton.setText(R.string.Hide_weekday_filters);
        }
        editor.commit();
    }

}
