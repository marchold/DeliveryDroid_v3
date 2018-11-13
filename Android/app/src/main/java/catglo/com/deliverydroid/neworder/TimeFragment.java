package catglo.com.deliverydroid.neworder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;


import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;

/**
 * Created by goblets on 2/17/14.
 */
public class TimeFragment extends DataAwareFragment implements TimePicker.OnTimeChangedListener, NumberPicker.OnValueChangeListener {

    private SharedPreferences sharedPreferences;
    private NumberPicker inHowManyMinutes;
    private TimePicker timePicker;
    private View button1;
    private View button2;
    private View button3;
    private View nextButton;
    private MutableDateTime time;
    private boolean isInit=false;
    private TextView timeLabel;
    private TextView inMinutes;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();

        time = new MutableDateTime(((NewOrderActivity)getActivity()).order.time.getTime());

        timeLabel.setText(Tools.getFormattedTimeDay(time.toGregorianCalendar()));

        nextButton.setOnClickListener(new View.OnClickListener() { public void onClick(final View v) {
            ((ButtonPadFragment.ButtonPadNextListener)getActivity()).onNextButtonPressed();
        }});

        timePicker.setOnTimeChangedListener(this);
        inHowManyMinutes.setOnValueChangedListener(this);

        setButtonClickListenerForMinutes(15,button1);
        setButtonClickListenerForMinutes(25,button2);
        setButtonClickListenerForMinutes(35,button3);

        setDatePicker();
        setNumberPicker();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    void setButtonClickListenerForMinutes(final int minutes,View button){
        button.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
            time = MutableDateTime.now();
            time.add(Minutes.minutes(minutes));
            NewOrderActivity activity = ((NewOrderActivity) getActivity());
            Order order = activity.order;
            order.time.setTime(time.getMillis());
            setDatePicker();
            setNumberPicker();
        }});
    }

    public void onPause(){
        super.onPause();  setDatePicker();
        setNumberPicker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        View view = (RelativeLayout) inflater.inflate(R.layout.new_order_time_frgament,null);

        inHowManyMinutes = (NumberPicker)view.findViewById(R.id.numberPicker);
        timePicker = (TimePicker)view.findViewById(R.id.timePicker);

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);

        nextButton = view.findViewById(R.id.next_button);

        inHowManyMinutes.setMaxValue(60);
        inHowManyMinutes.setMinValue(1);

        timeLabel = (TextView)view.findViewById(R.id.timeLabel);
        inMinutes = (TextView)view.findViewById(R.id.inMinutes);
        return view;
    }

    @Override
    protected void onDataChangedHandler() {
        time = new MutableDateTime(((NewOrderActivity)getActivity()).order.time.getTime());
        setDatePicker();
        setNumberPicker();
    }

    public void setDatePicker(){
        timePicker.setCurrentHour(time.getHourOfDay());
        timePicker.setCurrentMinute(time.getMinuteOfHour());
    }

    public void setNumberPicker(){
        DateTime now = DateTime.now();
        int minutesToOrder = Minutes.minutesBetween(now, time).getMinutes();
        if (minutesToOrder<0 || minutesToOrder>59){
            inHowManyMinutes.setVisibility(View.INVISIBLE);
        } else {
            inHowManyMinutes.setVisibility(View.VISIBLE);
            inHowManyMinutes.setValue(minutesToOrder);
        }
    }


    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        if (isInit==false){
            NewOrderActivity activity = ((NewOrderActivity) getActivity());
            Order order = activity.order;
            time.setHourOfDay(hourOfDay);
            time.setMinuteOfHour(minute);

            MutableDateTime thirtyMinutesAgo = MutableDateTime.now();
            thirtyMinutesAgo.add(Minutes.minutes(-30));
            if (time.isBefore(thirtyMinutesAgo)){
                time.add(Hours.hours(24));
            }

            MutableDateTime twentyFourHoursFromNow = MutableDateTime.now();
            twentyFourHoursFromNow.add(Hours.hours(24));
            if (time.isAfter(twentyFourHoursFromNow)){
                time.add(Hours.hours(-24));
            }


            order.time.setTime(time.getMillis());
            isInit=true;
            setNumberPicker();
            isInit=false;
            DataAwareFragment lastScreenFragment = (DataAwareFragment)  activity.getFragment(NewOrderActivity.Pages.order);
            if (lastScreenFragment!=null) lastScreenFragment.onDataChanged();
        }
        timeLabel.setText(Tools.getFormattedTimeDay(time.toGregorianCalendar()));
        inMinutes.setText("In "+Minutes.minutesBetween(DateTime.now(),time).getMinutes()+" minutes");

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (isInit==false){
            NewOrderActivity activity = ((NewOrderActivity) getActivity());
            Order order = activity.order;
            time = new MutableDateTime(DateTime.now());
            time.add(DurationFieldType.minutes(),newVal);
            order.time.setTime(time.getMillis());
            isInit=true;
            setDatePicker();
            isInit=false;
            DataAwareFragment lastScreenFragment = (DataAwareFragment)  activity.getFragment(NewOrderActivity.Pages.order);
            if (lastScreenFragment!=null) lastScreenFragment.onDataChanged();
        }
    }
}
