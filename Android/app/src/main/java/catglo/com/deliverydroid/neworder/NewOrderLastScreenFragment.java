package catglo.com.deliverydroid.neworder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Utils;
import catglo.com.deliverydroid.widgets.AddressAutocomplete;
import catglo.com.deliverydroid.widgets.Tooltip;
import org.joda.time.MutableDateTime;


import java.sql.Timestamp;

/**
 * Created by goblets on 2/17/14.
 */
public class NewOrderLastScreenFragment extends DataAwareFragment {

    private EditText preTip;
    private EditText orderCost;
    private EditText totalPayed;
    private EditText orderNumber;
    private EditText orderTime;
    private TextView extraPay;
    private AddressAutocomplete orderAddress;
    private EditText orderAptNum;
    private EditText orderNotes;
    private CheckBox outOfTown1;
    private CheckBox outOfTown2;
    private CheckBox outOfTown3;
    private CheckBox outOfTown4;

    private Button addOrder;
    private EditText phoneNumber;
    private BroadcastReceiver receiver;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    boolean supressListeners = false;

    @Override
    public void onResume(){
        super.onResume();
        NewOrderActivity activity = ((NewOrderActivity) getActivity());
        Order order = activity.order;

        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATED_DELIVERY_DROID_DATABASE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //do something based on the intent's action
            }
        };
        activity.registerReceiver(receiver, filter);


        setFieldsForOrder(activity.order);



        totalPayed.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
                if (supressListeners) return;
                supressListeners = true;
                float payed = Utils.parseCurrency(arg0.toString());
                float cost = Utils.parseCurrency(orderCost.getText().toString());
                float tip = Utils.parseCurrency(preTip.getText().toString());
                if (tip != payed-cost && preTip.isFocused()==false){
                    if (payed-cost>0){  //PONG!
                        preTip.setText(Utils.getFormattedCurrency(payed - cost));
                    } else {
                        preTip.setText("");
                    }
                }
                supressListeners = false;
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        orderCost.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
                if (supressListeners) return;
                supressListeners = true;
                //If this field changes and preTip is non zero set the totalPayed field, otherwize
                //do nothing when this field is updated
                float tip = Utils.parseCurrency(preTip.getText().toString());
                float cost = Utils.parseCurrency(arg0.toString());
                float total = Utils.parseCurrency(totalPayed.getText().toString());
                if (tip!=0 && total != cost+tip && totalPayed.isFocused()==false){
                    if (cost+tip>0){
                        totalPayed.setText(Utils.getFormattedCurrency(cost + tip));
                    }
                    else {
                        totalPayed.setText("");
                    }
                }
                supressListeners = false;
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        preTip.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
                if (supressListeners) return;
                supressListeners = true;
                //Update the total payed field if this one is edited
                float preTip = Utils.parseCurrency(arg0.toString());
                float cost = Utils.parseCurrency(orderCost.getText().toString());
                float total = Utils.parseCurrency(totalPayed.getText().toString());
                if (total != cost+preTip && totalPayed.isFocused()==false) {
                    if (cost+preTip>0){//PING!
                        totalPayed.setText(Utils.getFormattedCurrency(cost + preTip));
                    } else {
                        totalPayed.setText("");
                    }
                }
                supressListeners = false;
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });


        orderAddress.startSuggestor(activity.dataBase);

        activity.getUtils().initOptionalCheckBox(outOfTown1,
                "per_out_of_town_delivery",       //amount pref key
                "per_out_of_town_delivery_label1",//label pref key
                order.outOfTown1);
        activity.getUtils().initOptionalCheckBox(outOfTown2,
                "per_out_of_town_delivery2",//amount
                "per_out_of_town_delivery_label2",//label
                order.outOfTown2);
        activity.getUtils().initOptionalCheckBox(outOfTown3,
                "per_out_of_town_delivery3",
                "per_out_of_town_delivery_label3",
                order.outOfTown3);
        activity.getUtils().initOptionalCheckBox(outOfTown4,
                "per_out_of_town_delivery4",
                "per_out_of_town_delivery_label4",
                order.outOfTown4);

        addOrder.setOnClickListener(new View.OnClickListener() { public void onClick(final View v) {

            String s = orderCost.getEditableText().toString();
            NewOrderActivity activity = ((NewOrderActivity) getActivity());
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());


            Order order = activity.order;
            try {
                order.cost  = Utils.parseCurrency(s);
            } catch (Exception e) {
                order.cost  = 0;
            }

            s = extraPay.getEditableText().toString();
            try {
                order.extraPay  = Utils.parseCurrency(s);
            } catch (Exception e) {
                order.extraPay  = 0;
            }
            order.number = orderNumber.getEditableText().toString();

            order.payed = -1;

            s = preTip.getEditableText().toString();
            try {
                order.payed2 = Utils.parseCurrency(s);
            } catch (Exception e) {
                order.payed2 = 0f;
            }

            order.address = orderAddress.getEditableText().toString();
            order.notes = orderNotes.getEditableText().toString();
            order.phoneNumber = phoneNumber.getText().toString();

            order.outOfTown1=outOfTown1.isChecked();
            order.outOfTown2=outOfTown2.isChecked();
            order.outOfTown3=outOfTown3.isChecked();
            order.outOfTown4=outOfTown4.isChecked();
            order.onHold=false;
            order.startsNewRun=false;
            order.apartmentNumber = orderAptNum.getEditableText().toString();
            order.arivialTime = new Timestamp(System.currentTimeMillis());
            order.payedTime = new Timestamp(System.currentTimeMillis());
        
        /*    if (buttonPad3.selectedPoint==null || order.address.equalsIgnoreCase(buttonPad3.selectedPoint.address)==false || buttonPad3.selectedPoint.location == null){
                order.geoPoint = null;
                order.isValidated = false;
            } else {
                order.geoPoint = new MyGeoPoint(buttonPad3.selectedPoint.location.geoPoint);
                order.isValidated = true;
            }
          */
            activity.dataBase.add(order);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lastGeneratedOrderNumberString", orderNumber.getEditableText().toString());
            editor.commit();

            activity.finish();

        }});
    }

    void setFieldsForOrder(final Order order){
        if (order.cost!=0){
            orderCost.setText(Utils.getFormattedCurrency(order.cost));
        }
        if (order.extraPay!=0){
            extraPay.setText(Utils.getFormattedCurrency(order.extraPay));
        }
        if (order.notes!=null) orderNotes.setText(order.notes);

        //Crash Here?
        if (outOfTown1!=null) outOfTown1.setChecked(order.outOfTown1);
        if (outOfTown2!=null) outOfTown2.setChecked(order.outOfTown2);
        if (outOfTown3!=null) outOfTown3.setChecked(order.outOfTown3);
        if (outOfTown4!=null) outOfTown4.setChecked(order.outOfTown4);
        if (orderTime!=null) orderTime.setText(Utils.getFormattedTime(order.time));
        if (order.address!=null) orderAddress.setText(order.address);
        if (order.apartmentNumber!=null) orderAptNum.setText(order.apartmentNumber);
        if (order.number!=null) orderNumber.setText(order.number);
        if (order.phoneNumber!=null) phoneNumber.setText(order.phoneNumber);

        if (orderTime!=null) {
            orderTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewOrderActivity activity = ((NewOrderActivity) getActivity());
                    activity.getUtils().showTimeSliderDialog(orderTime, new MutableDateTime(order.time), new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //TODO: CHECKCHECK do we need to set this
                        }
                    });
                }
            });
            orderTime.setKeyListener(null);
            orderTime.setFocusable(false);
            orderTime.setFocusableInTouchMode(false);
        }
    }

    public void onPause(){
        super.onPause();
        writeFieldsToOrder();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    protected void onDataChangedHandler() {
        NewOrderActivity activity = ((NewOrderActivity) getActivity());
        if (activity!=null){
            setFieldsForOrder(activity.order);
        }
    }

    private void writeFieldsToOrder() {
        Order order = ((NewOrderActivity)getActivity()).order;
        order.cost = Utils.parseCurrency(totalPayed.getText().toString());
        order.extraPay = Utils.parseCurrency(extraPay.getText().toString());

        order.phoneNumber = phoneNumber.getText().toString();
        order.notes = orderNotes.getText().toString();
        order.outOfTown1 = outOfTown1.isChecked();
        order.outOfTown2 = outOfTown2.isChecked();
        order.outOfTown3 = outOfTown3.isChecked();
        order.outOfTown4 = outOfTown4.isChecked();
        order.address = orderAddress.getText().toString();
        order.apartmentNumber = orderAptNum.getText().toString();
        order.number = orderNumber.getText().toString();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout lastScreen = (LinearLayout) inflater.inflate(R.layout.new_order_last_screen, null);

        preTip = (EditText)lastScreen.findViewById(R.id.preTip);
        orderCost = (EditText) lastScreen.findViewById(R.id.OrderCost);
        totalPayed = (EditText)lastScreen.findViewById(R.id.totalPayed);
        orderNumber = (EditText) lastScreen.findViewById(R.id.OrderNumber);
        orderTime = (EditText) lastScreen.findViewById(R.id.OrderTime);
        extraPay = (TextView)lastScreen.findViewById(R.id.otherPay);
        //	extraPayTooltip =

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        orderAddress = (AddressAutocomplete) lastScreen.findViewById(R.id.orderAddress);
        orderAptNum = (EditText) lastScreen.findViewById(R.id.ApartmentNumber);
        orderNotes = (EditText) lastScreen.findViewById(R.id.OrderNotes);
        phoneNumber = (EditText) lastScreen.findViewById(R.id.phoneNumber);
        phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //	orderNotesTooltip =
        addOrder = (Button) lastScreen.findViewById(R.id.AddOrder);
        outOfTown1 = (CheckBox) lastScreen.findViewById(R.id.OutOfTown);
        outOfTown2 = (CheckBox) lastScreen.findViewById(R.id.OutOfTown2);
        outOfTown3 = (CheckBox) lastScreen.findViewById(R.id.OutOfTown3);
        outOfTown4 = (CheckBox) lastScreen.findViewById(R.id.OutOfTown4);

        if (sharedPreferences.getBoolean("showHelpBubbles",true)) {
            new Tooltip(extraPay, getString(R.string.extr_pay_tooltip));
            new Tooltip(orderNotes,getString(R.string.notes_tooltip));
        }

        return lastScreen;
    }
}
