package catglo.com.deliverydroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliverydroid.widgets.DateRangeDialogFragment;
import catglo.com.deliverydroid.widgets.DateRangeSlider;
import catglo.com.deliverydroid.widgets.DateSlider;
import catglo.com.deliverydroid.widgets.TimeSlider;



import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public Utils(){

    }

    static final Pattern currencyExtractionPattern = Pattern.compile("\\d+\\.?\\d*");



    TextView currentTimeDateField = null;
    Timestamp currentEditTimestamp = null;
    MutableDateTime currentEditCalendar = null;


    TimeSlider timeSlider=null;
    public DateRangeSlider dateRangeSlider;

    // define the listener which is called once a user selected the date.
    public DateSlider.OnDateSetListener mDateSetListener = new DateSlider.OnDateSetListener() {
        public void onDateSet(DateSlider view, MutableDateTime selectedDate) {
            currentTimeDateField.setText(String.format("%tl:%tM %tp", selectedDate.toGregorianCalendar(), selectedDate.toGregorianCalendar(), selectedDate.toGregorianCalendar()));
            currentEditTimestamp.setTime(selectedDate.getMillis());
            currentEditCalendar.setMillis(selectedDate.getMillis());
        }
    };
    private Activity activity;



    public static String currencySymbol()
    {
        try {
            return Currency.getInstance(Locale.getDefault()).getSymbol().replaceAll("\\w", "");
        } catch (Exception e) {
            return NumberFormat.getCurrencyInstance().getCurrency().getSymbol();
        }
    }

    public static String getFormattedCurrency(Float f) {
        if (f.isNaN()) {
            return currencySymbol()+"0.0";
        }
        try {
            DecimalFormat currency = new DecimalFormat("#0.00");
            currency.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
            currency.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
            return currencySymbol() + currency.format(f);
        } catch (IllegalArgumentException e2) {
            DecimalFormat currency = new DecimalFormat("#0.00");
            currency.setMaximumFractionDigits(2);
            currency.setMinimumFractionDigits(2);
            return currencySymbol() + currency.format(f);
        }

    }

    static String getFormattedTime(Calendar c) {
        return String.format("%tl:%tM %tp", c, c, c);
    }

    public static String getFormattedTime(Timestamp t) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t.getTime());
        return getFormattedTime(c);
    }

    public static String getFormattedTimeDay(Calendar c) {
        return String.format("%tl:%tM %tp %ta", c, c, c, c);
    }

    static String getFormattedTimeDay(Timestamp t) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t.getTime());
        return getFormattedTimeDay(c);
    }

    public static float parseCurrency(String formattedCurrency) {
        if (formattedCurrency == null || formattedCurrency.length() < 1) return 0;
        Matcher m = currencyExtractionPattern.matcher(formattedCurrency);
        String s;
        Log.i("REGEX","parseing:"+formattedCurrency);
        if (m.find()) {
            Log.i("REGEX","group count = "+m.groupCount());
            for (int i = 0; i <= m.groupCount();i++)
            {
                Log.i("REGEX",i+",parse:"+m.group(i));
            }
            s = m.group(0);

            try {
                return Float.parseFloat(s.replaceAll(",","").replaceAll(" ",""));
            } catch (NumberFormatException e) {
            	//throw new NumberFormatException("Cant convert "+formattedCurrency+" to currency ");
            }
        }
        return 0;
    }




    public interface OnDateRangeDialogClosedListener {
        void dialogClosed(MutableDateTime start, MutableDateTime stop);
    }
    public OnDateRangeDialogClosedListener onDateRangeDialogClosedListener;


    public synchronized void getDateRangeDialog(final Calendar start, final Calendar stop, final DialogInterface.OnDismissListener dismissListener) {
    	DateRangeDialogFragment dialog = new DateRangeDialogFragment();
    	Bundle args = new Bundle();
    	DateTime startDateTime = new DateTime(start.getTimeInMillis());
    	DateTime endDateTime = new DateTime(stop.getTimeInMillis());

    	args.putSerializable("start", startDateTime);
    	args.putSerializable("end", endDateTime);
    	dialog.setArguments(args);
    	onDateRangeDialogClosedListener = new OnDateRangeDialogClosedListener(){
    		public void dialogClosed(MutableDateTime startDateTime, MutableDateTime stopDateTime) {
    			start.setTimeInMillis(startDateTime.getMillis());
    			stop.setTimeInMillis(stopDateTime.getMillis());
    			dismissListener.onDismiss(null);
			}};
		dialog.show(activity.getFragmentManager(), "date_range_dialog");
    }

    synchronized public void onDateRangeDialogClosed(MutableDateTime start, MutableDateTime stop){
    	if (onDateRangeDialogClosedListener!=null)
    		onDateRangeDialogClosedListener.dialogClosed(start,stop);
    }

    public void getWorkWeekDates(Calendar date, Calendar start, Calendar stop) {
    	final int weekStartDay = Integer.parseInt(sharedPreferences.getString("StartOfWorkWeek", "0"));

    	start.setTimeInMillis(date.getTimeInMillis());
    	start.set(Calendar.DAY_OF_WEEK, weekStartDay);

		//if the start is in the future roll back 1 week
		if (start.getTimeInMillis() > date.getTimeInMillis()) {
			start.add(Calendar.DAY_OF_WEEK, -7);
		}
		stop.setTimeInMillis(start.getTimeInMillis());
		stop.add(Calendar.DATE, 7);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void saveTextToClipboard(String text){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("address",text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public boolean navigateTo(String address, Activity activity){
        return mapOrNavTo(address, Integer.parseInt(sharedPreferences.getString("navigationIntent", "1")));
	}

    SharedPreferences sharedPreferences;
    public boolean mapTo(String address, Activity activity){
        return mapOrNavTo(address, Integer.parseInt(sharedPreferences.getString("mapsIntent", "2")));
    }

    public boolean mapOrNavTo(String address, int navOption){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if (sharedPreferences.getBoolean("autoCutForPaste", true)){
            saveTextToClipboard(address);
        }

		Intent intent;
		String addressEscaped = address.replace(' ', '+');
        switch (navOption){
            case 1:
                //Google navigation
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + addressEscaped));
                activity.startActivity(intent);
                return true;
            case 2:
                //Maps Intent
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+ addressEscaped));
                activity.startActivity(intent);
                return true;
            case 3:
                //HTTP navigation
                LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                try {
                    if (lastKnownLocation!=null){
                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();

                        addressEscaped = URLEncoder.encode(address, "UTF-8");
                        String navigationUrl = "http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+addressEscaped;

                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUrl));
                        activity.startActivity(intent);

                    } else {
                        //TODO: make them wait until we have a location fix
                        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);

                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return true;
        }
        return false;
	}

    public void showOnScreenKeyboard(View paymentTotal2){
		InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);

	}

    public void hideOnScreenKeyboard(View paymentTotal){
		InputMethodManager inputMethodManager=(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}

    void showTimeSliderDialog(final EditText field, final Timestamp time, final Dialog.OnDismissListener listener){
        currentTimeDateField=field;
        currentEditTimestamp=time;
        currentEditCalendar = MutableDateTime.now();
        currentEditCalendar.setMillis(time.getTime());

        if (timeDialog==null || timeDialog.isAdded()==false) {
            timeDialog = new TimeDialog();

            Bundle bundle = new Bundle();
            bundle.putSerializable(TimeDialog.DATE_TIME_ARGUMENT, currentEditCalendar.toDateTime());
            timeDialog.setArguments(bundle);

            timeDialog.setOnTimeChangedListener(new TimeDialog.OnTimeChangedListener(){public void onTimeChanged(DateTime newTime, DialogInterface arg0) {
                time.setTime(newTime.getMillis());

                field.setText(String.format("%tl:%tM %tp", newTime.toCalendar(null), newTime.toCalendar(null), newTime.toCalendar(null)));
                  currentEditTimestamp.setTime(newTime.getMillis());
                  currentEditCalendar.setMillis(newTime.getMillis());

                if (listener!=null) listener.onDismiss(arg0);
                timeDialog=null;
            }});
            timeDialog.show(activity.getFragmentManager(), "time_picker_dialog");
        }
    }

    public void showTimeSliderDialog(final TextView field, final MutableDateTime time, final Dialog.OnDismissListener listener){
    	currentTimeDateField=field;
		currentEditTimestamp=new Timestamp(time.getMillis());;
		currentEditCalendar = time;

        if (timeDialog==null) {
            timeDialog = new TimeDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable(TimeDialog.DATE_TIME_ARGUMENT, currentEditCalendar.toDateTime());
            timeDialog.setArguments(bundle);

            timeDialog.setOnTimeChangedListener(new TimeDialog.OnTimeChangedListener(){public void onTimeChanged(DateTime newTime, DialogInterface arg0) {
                time.setDate(newTime);

                field.setText(String.format("%tl:%tM %tp", newTime.toCalendar(null), newTime.toCalendar(null), newTime.toCalendar(null)));
                currentEditTimestamp.setTime(newTime.getMillis());
                currentEditCalendar.setMillis(newTime.getMillis());
                timeDialog = null;
                if (listener!=null) listener.onDismiss(arg0);
            }});
            timeDialog.show(activity.getFragmentManager(), "time_picker_dialog");
        }

	 }


    public TimeDialog timeDialog;

    @SuppressWarnings("deprecation")
	public void showTimeSliderDialog(final TextView deliveryTime, final Timestamp payedTime, final Dialog.OnDismissListener listener, boolean isFutureTime) {
		currentTimeDateField=deliveryTime;
		currentEditTimestamp=payedTime;
		currentEditCalendar = MutableDateTime.now();
		currentEditCalendar.setTime(payedTime.getTime());
		currentEditCalendar.setHourOfDay(payedTime.getHours());

		if (timeDialog==null) {
			timeDialog = new TimeDialog();
			Bundle bundle = new Bundle();
			bundle.putSerializable(TimeDialog.DATE_TIME_ARGUMENT, currentEditCalendar.toDateTime());
            bundle.putBoolean("isFutureTime",isFutureTime);
			timeDialog.setArguments(bundle);

			timeDialog.setOnTimeChangedListener(new TimeDialog.OnTimeChangedListener(){public void onTimeChanged(DateTime newTime, DialogInterface arg0) {
				payedTime.setTime(newTime.getMillis());//dateTime.setDate(newTime);

				deliveryTime.setText(String.format("%tl:%tM %tp", newTime.toCalendar(null), newTime.toCalendar(null), newTime.toCalendar(null)));
	          	currentEditTimestamp.setTime(newTime.getMillis());
	          	timeDialog=null;
	          	if (listener!=null) listener.onDismiss(arg0);
			}});
			timeDialog.show(activity.getFragmentManager(), "time_picker_dialog");
		}

	}

    void showTimeSliderDialog(EditText field, Timestamp time){
		showTimeSliderDialog(field, time, null);
	}

    public final void setKeyboardListener(final DeliveryDroidBaseActivity.OnKeyboardVisibilityListener listener) {
        final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

		    private boolean wasOpened;
   		    private final Rect r = new Rect();

            @Override
		    public void onGlobalLayout() {
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                //Log.i("MARC", "height dif " + heightDiff);
                boolean isOpen = !(heightDiff < 200);
                if (isOpen == wasOpened) {
                    return;
                }
                wasOpened = isOpen;
                listener.onVisibilityChanged(isOpen);
            }
	    });
    }

    public DecimalFormat currency = new DecimalFormat("#0.00");

    static DataBase dataBase = null;
    public DataBase create(Activity activity) {
        if (dataBase==null){
            dataBase = new DataBase(activity.getApplicationContext());
            dataBase.open();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        this.activity = activity;

        return dataBase;
    }

    public Context getContext(){
        return activity;
    }


    void showAltPayDialog(final CheckBox altPayCheckbox
            , final String   amountKey
            , final String   labelKey
            , final boolean  isChecked)
    {
        final String amount = sharedPreferences.getString(amountKey,"0");
        final String label = sharedPreferences.getString(labelKey,"");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            View dialogView = View.inflate(getContext(), R.layout.new_order_custom_mileage_settings_dialog, null);
            final TextView name = (TextView)dialogView.findViewById(R.id.customName);
            final TextView value = (TextView)dialogView.findViewById(R.id.customValue);
            name.setText(label);
            value.setText(amount);
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    try {
                        float newAmountValue = parseCurrency(value.getText().toString());
                        editor.putString(amountKey, value.getText().toString());
                        editor.putString(labelKey, name.getText().toString());
                        editor.commit();
                        altPayCheckbox.setChecked(true);
                        setCheckboxText(newAmountValue,name.getText().toString(),value.getText().toString(),altPayCheckbox);
                        initOptionalCheckBox(altPayCheckbox,amountKey,labelKey,isChecked);

                    } catch (NumberFormatException e){
                        Toast.makeText(getContext(),"Error Parsing "+ value.getText().toString(),Toast.LENGTH_LONG).show();
                    }

                }
            });
            alertDialogBuilder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    altPayCheckbox.setChecked(false);
                }
            });

            alertDialogBuilder.show();

    }

    //This function is shared between add and edit order screens
    public void initOptionalCheckBox(final CheckBox altPayCheckbox
            , final String   amountKey
            , final String   labelKey
            , final boolean  isChecked){

        final String amount = sharedPreferences.getString(amountKey,"0");
        final String label = sharedPreferences.getString(labelKey,"");

        final float amountValue = Utils.parseCurrency(amount);
        altPayCheckbox.setChecked(isChecked);
        altPayCheckbox.setEnabled(true);

        if (amountValue==0 && label.length()==0){
            //Pop-up dialog box so user can set up custom mileage pay checkboxes.
            altPayCheckbox.setText(R.string.Custom);
            altPayCheckbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showAltPayDialog(altPayCheckbox,amountKey,labelKey,isChecked);
                }
            });
        }
        else {
            setCheckboxText(amountValue,label,amount,altPayCheckbox);
            altPayCheckbox.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAltPayDialog(altPayCheckbox,amountKey,labelKey,isChecked);
                    return true;
                }
            });
        }


    }

    private void setCheckboxText(double amountValue, String label, String amount, CheckBox altPayCheckbox){
        //If the label is empty use the amount as the label
        if (Math.abs(amountValue) > 0) {
            if (label.length()==0) {
                float f = Utils.parseCurrency(amount);
                if (f >= 0) {
                    altPayCheckbox.setText(Utils.getFormattedCurrency(f));
                } else {
                    altPayCheckbox.setText(Utils.getFormattedCurrency(-f));
                }
            } else {
                altPayCheckbox.setText(label);
            }
        }
    }


    public static void appendLog(String text)
    {
        File logFile = new File("sdcard/dd-log.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
