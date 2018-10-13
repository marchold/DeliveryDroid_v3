package catglo.com.deliverydroid.outTheDoor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by mkluver on 5/7/14.
 */
public class OutTheDoorFragment extends Fragment implements LocationListener {

    private EditText paymentTotal;
  //  private TextView orderTimes;
 //   private Button next;
    private Button split;
    private RadioButton cash;
    private RadioButton check;
    private RadioButton credit;
    private TextView tipsText;
    private LinearLayout notesList;


    Order order;
    private Button callStore;
    private RadioGroup radioGroup;
    private LocationManager locationManager;
    private double longitude;
    private double latitude;
    private LinearLayout gpsNotesList;
    private View newNoteButton;

    public static OutTheDoorFragment create(Order order) {
        OutTheDoorFragment fragment = new OutTheDoorFragment();
        Bundle args = new Bundle();
        args.putSerializable("order", order);
        fragment.setArguments(args);
        return fragment;
    }

    Handler messageHandler;

    @Override
    public void onPause() {
        saveFields();
        super.onPause();
    }

    public void saveFields(){
        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
        if (activity!=null) {

            messageHandler.removeCallbacks(updateOrderTimers);


            if (paymentTotal.getText().toString().length()>0) {
                try {
                    order.payed = Float.valueOf(paymentTotal.getText().toString());
                } catch (final NumberFormatException e) {
                }
            }

            if (paymentTotal2.getText().toString().length()>0) {
                try {
                    order.payed2 = Float.valueOf(paymentTotal2.getText().toString());
                } catch (final NumberFormatException e) {
                }
            }



            if (cash.isChecked()) {
                order.paymentType = Order.CASH;
            }
            if (check.isChecked()) {
                order.paymentType = Order.CHECK;
            }
            if (credit.isChecked()) {
                order.paymentType = Order.CREDIT;
            }
            if (ebt.isChecked()) {
                order.paymentType = Order.EBT;
            }

            if (cash2.isChecked()) {
                order.paymentType2 = Order.CASH;
            }
            if (check2.isChecked()) {
                order.paymentType2 = Order.CHECK;
            }
            if (credit2.isChecked()) {
                order.paymentType2 = Order.CREDIT;
            }
            if (ebt2.isChecked()) {
                order.paymentType2 = Order.EBT;
            }
        //    String notes = notesThisOrder.getText().toString();

            //TODO: set undeliverable

            activity.dataBase.setOrderPayment(order.primaryKey, order.payed, order.paymentType, order.payed2, order.paymentType2, startNewRun, order.notes, order.undeliverable);
            startNewRun = false;

            activity.updateDoneButtonState(order);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
        order = (Order)getArguments().getSerializable("order");

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation!=null){
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
        } else {
            latitude=0;
            longitude=0;
        }

        messageHandler = new Handler();
        messageHandler.post(updateOrderTimers);


        if (activity.sharedPreferences.getBoolean("showTipField", false)==false){
            tipTotal.setVisibility(View.GONE);
        }
        paymentTotal.setOnClickListener(new View.OnClickListener(){public void onClick(View arg0) {
            OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
            if (activity==null || activity.isFinishing()) return;
            if (paymentTotal.getText().toString().length() < 2) {
                showPaymentAmountList();
            }
            paymentTotal.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL|EditorInfo.TYPE_CLASS_NUMBER);
            activity.tools.showOnScreenKeyboard(paymentTotal);
        }});
        View.OnClickListener paymentTypeClickListener = new View.OnClickListener() {
            public void onClick(View arg0) {
                OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
                if (activity==null || activity.isFinishing()) return;

                if (paymentTotal.getText().toString().length() < 2) {
                    showPaymentAmountList();
                    activity.tools.showOnScreenKeyboard(paymentTotal);
                }
                //else {
                //    next.setVisibility(View.VISIBLE);
                //}
            }
        };
        rootLayout.setOnClickListener(new View.OnClickListener(){public void onClick(View arg0) {
            paymentAmountListView.setVisibility(View.GONE);
        }});
        paymentTotal.setOnFocusChangeListener(new View.OnFocusChangeListener(){public void onFocusChange(View arg0, boolean hasFocus) {
            if (hasFocus==false){
                paymentAmountList.setVisibility(View.GONE);
            }
        }});

        paymentTotal2.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
             //   if (inNext) return;
                try {
                    if (paymentTotal2.isFocused()){
                        float f = Tools.parseCurrency(paymentTotal2.getText().toString());
                        f += Tools.parseCurrency(paymentTotal.getText().toString());
                        f -=  order.cost;
                        tipTotal.setText(Tools.getFormattedCurrency(f));
                    }
                } catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                saveFields();
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        paymentTotal.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
                if (getActivity()==null || getActivity().isFinishing()) return;
//                next.setVisibility(View.VISIBLE);
                if (arg0.toString().length()>2) {
                    paymentAmountList.setVisibility(View.GONE);
                    todaysTips();

                    if (paymentTotal.isFocused()){
                        float f = Tools.parseCurrency(arg0.toString());
                        f -=  order.cost;
                        tipTotal.setText(Tools.getFormattedCurrency(f));
                    }

                } else {
                    if (paymentTotal.isFocused() && paymentTotal2.getVisibility()!=View.VISIBLE){
                        generatePaymentSuggestionListFor(arg0.toString());
                    } else {
                        paymentAmountList.setVisibility(View.GONE);
                    }
                }
                saveFields();
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        tipTotal.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable arg0) {
                if (getActivity()==null || getActivity().isFinishing()) return;
                try {
                    if (tipTotal.isFocused()){
                        float f = Tools.parseCurrency(arg0.toString());
                        f += order.cost;
                        paymentTotal.setText(Tools.getFormattedCurrency(f));
                    }
                } catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                saveFields();
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });
        tipTotal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //hideOnScreenKeyboard(tipTotal);
                    //next.performClick();
                    return true;
                }
                return false;
            }
        });

     //   notesThisOrder.setText(order.notes);

        SharedPreferences.Editor prefeditor = activity.sharedPreferences.edit();
        prefeditor.putBoolean("justClearedBank", false);
        prefeditor.commit();


     //   if (orders.get(0).payed2 != 0) {
     //       next.setVisibility(View.INVISIBLE);
     //   }
     //   if (orderCounter == orderCount - 1) {
     //       next.setText(R.string.Done);
     //   }

        // We store the pre-tip value in the payed2 field
        if (order.payed2 != 0) {
            float total = order.payed2 + order.cost;
            paymentTotal.setText(new DecimalFormat("#.##").format(total));
            //next.setVisibility(View.VISIBLE);
        } else {
            paymentTotal.setText("");
        }
        order.payed2 = 0;

        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (activity.sharedPreferences.getBoolean("usePaymentCash", true) == false) {
            cash.setVisibility(View.GONE);
            cash2.setVisibility(View.GONE);
        }
        if (activity.sharedPreferences.getBoolean("usePaymentCheck", true) == false) {
            check.setVisibility(View.GONE);
            check2.setVisibility(View.GONE);
        }
        if (activity.sharedPreferences.getBoolean("usePaymentCredit", true) == false) {
            credit.setVisibility(View.GONE);
            credit2.setVisibility(View.GONE);
        }
        if (activity.sharedPreferences.getBoolean("usePaymentEbt", true) == false) {
            ebt.setVisibility(View.GONE);
            ebt2.setVisibility(View.GONE);
        }


        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.kind_of_note, null));
                        return builder.create();
                    }
                };
                dialogFragment.show(getFragmentManager(),"New Note Chooser");
            }
        });

        //backButton.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
        //    startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        //    finish();
        //}});

   /*     next.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                synchronized (getActivity()) {
                    inNext = true;

                    tipTotal.setFocusable(true);
                    tipTotal.setFocusableInTouchMode(true);


                    //Hide soft keyboard 2 different ways
                    tools.hideOnScreenKeyboard(paymentTotal);

                    float payment = 0;
                    float payment2 = 0;
                    try {
                        payment = Float.valueOf(paymentTotal.getText().toString());
                    } catch (final NumberFormatException e) {
                    }
                    try {
                        payment2 = Float.valueOf(paymentTotal2.getText().toString());
                    } catch (final NumberFormatException e) {
                    }

                    int paymentType;
                    int paymentType2 = 0;
                    // if (payment2>0){

                    paymentType = Order.NOT_PAID;
                    if (cash.isChecked()) {
                        paymentType = Order.CASH;
                    }
                    if (check.isChecked()) {
                        paymentType = Order.CHECK;
                    }
                    if (credit.isChecked()) {
                        paymentType = Order.CREDIT;
                    }
                    if (ebt.isChecked()) {
                        paymentType = Order.EBT;
                    }

                    paymentType2 = Order.NOT_PAID;
                    if (cash2.isChecked()) {
                        paymentType2 = Order.CASH;
                    }
                    if (check2.isChecked()) {
                        paymentType2 = Order.CHECK;
                    }
                    if (credit2.isChecked()) {
                        paymentType2 = Order.CREDIT;
                    }
                    if (ebt2.isChecked()) {
                        paymentType2 = Order.EBT;
                    }

                    String notes = notesThisOrder.getText().toString();

                    dataBase.setOrderPayment(orders.get(orderCounter).primaryKey, payment, paymentType, payment2, paymentType2, startNewRun, notes);
                    startNewRun = false;

                    orders.get(orderCounter).payed = payment;
                    orderCounter++;
                    if (orderCounter < orders.size()) {
                        notesThisOrder.setText(orders.get(orderCounter).notes);
                    }
                    if (orderCounter < orders.size()) {
                        if (orders.get(orderCounter).payed > 0) {
                            paymentTotal.setText("" + orders.get(orderCounter).payed);
                            next.setVisibility(View.VISIBLE);
                        } else
                            // The preTip is stored in payed2 add it here
                            if (orders.get(orderCounter).payed2 != 0) {
                                float total = orders.get(orderCounter).payed2 + orders.get(orderCounter).cost;
                                paymentTotal.setText(new DecimalFormat("#.##").format(total));
                                next.setVisibility(View.VISIBLE);
                            } else {
                                next.setVisibility(View.INVISIBLE);
                                paymentTotal.setText("");
                            }
                        orders.get(orderCounter).payed2 = 0;
                    }

                    if (orderCounter == orderCount) {
                        setResult(300);
                        startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        finish();
                    } else {
                        if (orderCounter == orderCount - 1) {
                            next.setText(R.string.Done);
                        }
                        messageHandler.post(updateOrderTimers);
                    }

                    tipTotal.setText("");
                    paymentTotal2.setVisibility(View.GONE);
                    paymentTotal2.setText("");
                    group2.setVisibility(View.GONE);
                    split.setVisibility(View.VISIBLE);

                    radioGroup.clearCheck();
                    setPaymentEnabledState();
                    inNext = false;

                }
            }
        });*/

        paymentTotal.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (paymentTotal.getText().toString().length() > 0) {
                    //next.setVisibility(View.VISIBLE);
                    todaysTips();
                } else {
                    //next.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        split.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
                if (activity==null || activity.isFinishing()) return;

                paymentTotal2.setVisibility(View.VISIBLE);

                tipTotal.setFocusable(false);
                tipTotal.setFocusableInTouchMode(false);

                //if the payment type check boxes are blank set the default values
                if (!cash.isChecked()
                        && !check.isChecked()
                        && !credit.isChecked()
                        && !ebt.isChecked()
                        && !cash2.isChecked()
                        && !check2.isChecked()
                        && !credit2.isChecked()
                        && !ebt2.isChecked()) {
                    credit.setChecked(true);
                    cash2.setChecked(true);
                }
                group2.setVisibility(View.VISIBLE);
                split.setVisibility(View.GONE);
                paymentTotal2.requestFocus();

                if (paymentTotal.getText().toString().length() == 0) {
                    paymentTotal.setText(""+order.cost);
                }

                activity.tools.showOnScreenKeyboard(paymentTotal2);
            }
        });


        cash.setOnClickListener(paymentTypeClickListener);
        check.setOnClickListener(paymentTypeClickListener);
        credit.setOnClickListener(paymentTypeClickListener);
        ebt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                paymentTotal.setText("" + order.cost);
                //next.setVisibility(View.VISIBLE);
            }
        });

        updateOrderTimers.run();

    }

    private void setPaymentEnabledState() {
        try {
            if (order.undeliverable == false) {
                //markUndeliverableButton.setText("Mark Undeliverable");
                paymentTotal.setEnabled(true);
                cash.setEnabled(true);
                check.setEnabled(true);
                credit.setEnabled(true);
                ebt.setEnabled(true);
                split.setEnabled(true);
                paymentTotal.setHint("Payment");

            } else {
               // markUndeliverableButton.setText("Mark Deliverable");
                ebt.setEnabled(false);
                split.setEnabled(false);
                paymentTotal.setEnabled(false);
                cash.setEnabled(false);
                check.setEnabled(false);
                credit.setEnabled(false);
                paymentTotal.setHint("Undeliverable");
                paymentTotal.setText("");

            }
        } catch (IndexOutOfBoundsException e){
            //We can ignore these
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        order = (Order)getArguments().getSerializable("order");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.out_the_door_form_body, null);

        rootLayout = (ViewGroup)view.findViewById(R.id.scrollView);

        cash2 = (RadioButton) view.findViewById(R.id.cash_button2);
        check2 = (RadioButton) view.findViewById(R.id.check_button2);
        credit2 = (RadioButton) view.findViewById(R.id.credit_button2);
        ebt2 = (RadioButton) view.findViewById(R.id.ebt_button2);
        group2 = (RadioGroup) view.findViewById(R.id.cash_check_card2);
      //  markUndeliverableButton = (Button)view.findViewById(R.id.markUndeliverable);
        editOrder = (Button) view.findViewById(R.id.editOrder);
        callStore = (Button) view.findViewById(R.id.callStore);
        //previousOrder = (Button) view.findViewById(R.id.previousOrder);
       // backButton = view.findViewById(R.id.backButton);

       // next = (Button) view.findViewById(R.id.arrivedAt);
        paymentAmountListView  = (AdapterView<ArrayAdapter<String>>) view.findViewById(R.id.paymentAmountListView);
        paymentAmountList = (ViewGroup) view.findViewById(R.id.paymentAmountList);
        paymentAmountList.setVisibility(View.GONE);
        paymentTotal = (EditText) view.findViewById(R.id.paymentTotal);
        paymentTotal2 = (EditText) view.findViewById(R.id.paymentTotal_2);
        tipTotal = (EditText) view.findViewById(R.id.tipAmount);
        paymentTotal.setInputType(EditorInfo.TYPE_NULL);

        cash = (RadioButton) view.findViewById(R.id.cash_button);
        check = (RadioButton) view.findViewById(R.id.check_button);
        credit = (RadioButton) view.findViewById(R.id.credit_button);
        ebt = (RadioButton) view.findViewById(R.id.ebt_button);

        radioGroup = (RadioGroup) view.findViewById(R.id.cash_check_card);
        tipsText = (TextView) view.findViewById(R.id.TipsText);
        mileage = (TextView) view.findViewById(R.id.MileageEarned);
        earnings = (TextView) view.findViewById(R.id.DriverEarnings);
        split = (Button) view.findViewById(R.id.split_order);

        currentAddress = (TextView) view.findViewById(R.id.currentAddress);
        currentCost = (TextView) view.findViewById(R.id.currentCost);
        currentWait = (TextView) view.findViewById(R.id.currentWait);

        notesList = (LinearLayout)view.findViewById(R.id.notesList);
        gpsNotesList = (LinearLayout)view.findViewById(R.id.gpsNotesList);
        newNoteButton = view.findViewById(R.id.newNoteButton);
        return view;
    }


    // Create runnable for posting
    final Runnable updateOrderTimers = new Runnable() {
        public void run() {
            final OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
            if (activity==null || activity.isFinishing()) return;

            synchronized (activity) {


                int minutesAgo = order.getMinutesAgo();// (int)
                // ((System.currentTimeMillis()
                // -
                // orders.get(orderCounter).time.getTime())
                // /
                // 1000)
                // /
                // 60;

                currentAddress.setText(order.address + " " + order.apartmentNumber);
                currentWait.setText("" + minutesAgo);
                currentCost.setText("$" + order.cost);

            //    String pending = "";// new
            //    for (int i = orderCounter + 1; i < orders.size(); i++) {
            //        minutesAgo = (int) ((System.currentTimeMillis() - orders.get(i).time.getTime()) / 1000) / 60;
            //        pending = pending + minutesAgo + "min\t" + orders.get(i).address + "\n";
            //    }
            //    orderTimes.setText(pending);



                //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(This, android.R.layout.simple_dropdown_item_1line, costGuess);
                // paymentTotal.setAdapter(adapter);
                // paymentTotal.setThreshold(1);
                // paymentAmountList.setAdapter(adapter);

                todaysTips();

                // ViewGroup NotesListItems = (ViewGroup)inflator.inflate(
                // R.layout.note_list_item, null);

                ArrayList<DataBase.NoteEntry> s = activity.dataBase.getPastNotesForAddressAndAptNo(order.address, order.apartmentNumber);
                notesList.removeAllViews();
                for (DataBase.NoteEntry ne : s) {
                    View view = View.inflate(getActivity(), R.layout.this_address_note_list_item, notesList);
                    ((TextView)view.findViewById(R.id.dateTime)).setText(ne.date);
                    ((TextView)view.findViewById(R.id.note)).setText(ne.note);
                }
//
//                // notesBody2.setText(s);
//                s = activity.dataBase.getPastNotesForAddress(order.address);
//                // notesBody.setText(s);
//                notesLayout.removeAllViews();
//                phoneNumbersThisOrder.clear();
//                for (DataBase.NoteEntry ne : s) {
//                    ViewGroup view = makeViewFromNoteEntry(ne);
//                    notesLayout.addView(view);
//                }

                messageHandler.postDelayed(updateOrderTimers,30000);

                final String phoneNumber = order.phoneNumber;
//                if (phoneNumber!=null && phoneNumber.length()>0){
//                    callThemButton.setVisibility(View.VISIBLE);
//                    callThemButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
//                        }
//                    });
//                    smsThemButton.setVisibility(View.VISIBLE);
//                    smsThemButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//                            if (activity==null || activity.isFinishing()) return;
//
//
//                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + phoneNumber, null));
//                            String messageBody = activity.sharedPreferences.getString("customerDefaultSMS", activity.getString(R.string.customerDefaultSMS));
//                            String cost = "" + Tools.getFormattedCurrency(order.cost);
//                            messageBody = messageBody.replace("###", cost);
//
//                            i.putExtra("sms_body", messageBody);
//                            startActivity(i);
//                        }
//                    });
//                } else {
//                    //Legacy stuff from phone in notes thing
//                    if (activity.sharedPreferences.getBoolean("callPhoneInNotes", false) && phoneNumbersThisOrder.size() > 0) {
//
//                        callThemButton.setVisibility(View.VISIBLE);
//                        callThemButton.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//                                if (phoneNumbersThisOrder.size() > 1) {
//                                    DialogFragment dialog = new DialogFragment() {
//                                        @Override
//                                        public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//                                            CharSequence[] list = phoneNumbersThisOrder.toArray(new CharSequence[phoneNumbersThisOrder.size()]);
//
//                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                            builder.setTitle(R.string.Call).setItems(list, new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumbersThisOrder.get(which))));
//                                                }
//                                            });
//                                            return builder.create();
//                                        }
//                                    };
//                                    dialog.show(getFragmentManager(), "set_pay_rate");
//                                } else {
//                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumbersThisOrder.get(0))));
//                                }
//                            }
//                        });
//                        smsThemButton.setVisibility(View.VISIBLE);
//                        smsThemButton.setOnClickListener(new View.OnClickListener() {
//                            public void onClick(View v) {
//
//                                if (phoneNumbersThisOrder.size() > 1) {
//                                    DialogFragment dialog = new DialogFragment() {
//                                        @Override
//                                        public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//                                            final CharSequence[] list = phoneNumbersThisOrder.toArray(new CharSequence[phoneNumbersThisOrder.size()]);
//
//                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                                            builder.setTitle(R.string.textMessage).setItems(list, new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//                                                    if (activity==null || activity.isFinishing()) return;
//
//                                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + list[which], null));
//
//                                                    String messageBody = activity.sharedPreferences.getString("customerDefaultSMS", activity.getString(R.string.customerDefaultSMS));
//                                                    String cost = "" + Tools.getFormattedCurrency(order.cost);
//                                                    messageBody = messageBody.replace("###", cost);
//
//                                                    i.putExtra("sms_body", messageBody);
//                                                    startActivity(i);
//                                                }
//                                            });
//                                            return builder.create();
//                                        }
//                                    };
//                                    dialog.show(getFragmentManager(), "set_pay_rate");
//                                } else {
//                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + phoneNumbersThisOrder.get(0), null));
//
//                                    String messageBody = activity.sharedPreferences.getString("customerDefaultSMS", activity.getString(R.string.customerDefaultSMS));
//                                    String cost = "" + Tools.getFormattedCurrency(order.cost);
//                                    messageBody = messageBody.replace("###", cost);
//
//                                    i.putExtra("sms_body", messageBody);
//                                    startActivity(i);
//                                }
//                            }
//                        });
//                    } else {
//                        callThemButton.setVisibility(View.GONE);
//                        smsThemButton.setVisibility(View.GONE);
//                    }
//                }

            }
        }
    };

    static Pattern phoneNumberPattern = Pattern.compile("([0-9]{0,4})\\){0,1}\\s{0,1}\\-{0,1}([0-9]{3,4})\\s{0,1}\\-{0,1}\\s{0,1}([0-9]{4})");

//    protected ViewGroup makeViewFromNoteEntry(DataBase.NoteEntry ne) {
//        ViewGroup notesListItems = (ViewGroup) View.inflate(getActivity(),R.layout.note_list_item, null);
//
//        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
//        if (activity==null || activity.isFinishing()) return notesListItems;
//
//        ((TextView) notesListItems.findViewById(R.id.note)).setText(ne.note);
//        ((TextView) notesListItems.findViewById(R.id.date)).setText(ne.date);
//
//        if (activity.sharedPreferences.getBoolean("callPhoneInNotes", false)) {
//            Matcher matcher = phoneNumberPattern.matcher(ne.note);
//            if (matcher.find()) {
//                String phoneNumberAreaCode = matcher.group(1);
//                String phoneNumberPrefix = matcher.group(2);
//                String phoneNumberNumber = matcher.group(3);
//                final String phoneNumber = phoneNumberAreaCode + phoneNumberPrefix + phoneNumberNumber;
//
//                phoneNumbersThisOrder.add(phoneNumber);
//
//
//            }
//        }
//        return notesListItems;
//    }


    View editOrder;

    private TextView mileage;
    private TextView earnings;
    private TextView currentAddress;
    private TextView currentCost;
    private TextView currentWait;

    boolean startNewRun = false;
    private RadioButton ebt;

    RadioButton cash2;
    RadioButton check2;
    RadioButton credit2;
    RadioButton ebt2;
    EditText paymentTotal2;
    RadioGroup group2;

    private ViewGroup paymentAmountList;

    ArrayList<String> phoneNumbersThisOrder = new ArrayList<String>();
    private AdapterView<ArrayAdapter<String>> paymentAmountListView;
    private ViewGroup rootLayout;
    private EditText tipTotal;
   // private Button markUndeliverableButton;
    float cost;
    private int listPosition;

    protected void generatePaymentSuggestionListFor(String soFar) {

        final int NUMBER_OF_GUESSES = 40;

        final String[] costGuess = new String[NUMBER_OF_GUESSES];
        cost = order.cost;

        float startValue = cost;

        if (soFar.length()>0){
            float typedValue;
            try {
                typedValue = Float.parseFloat(soFar);
            } catch (NumberFormatException e){
                typedValue = 0;
            }
            if (typedValue<cost){
                //In this case we need to assume they are typing in the first number of the value and we need
                StringBuilder c = new StringBuilder();
                c.append(String.valueOf(cost));
                c.replace(0, soFar.length(), soFar);
                try {
                    startValue = Float.parseFloat(c.toString());
                } catch (NumberFormatException e){
                    startValue = 0;
                }
            } else {
                startValue = typedValue;
            }

        }


        final int dollars = (int) startValue;
        if (startValue-dollars==0){
            final float tip = cost * 1.13f;
            listPosition = -1;
            DecimalFormat df = new DecimalFormat("#.00");
            for (int i = 0; i < NUMBER_OF_GUESSES; i++) {
                costGuess[i ] = df.format(startValue + i);
                if (startValue + i > tip && listPosition == -1) {
                    listPosition = i;
                }
            }
        } else {

            final float tip = cost * 1.13f;
            listPosition = -1;
            DecimalFormat df = new DecimalFormat("#.00");
            for (int i = 0; i < (NUMBER_OF_GUESSES/2); i++) {
                costGuess[i * 2 + 0] = df.format(startValue + i);
                costGuess[i * 2 + 1] = (dollars + i + 1)+".";
                if (startValue + i > tip && listPosition == -1) {
                    listPosition = i;
                }
            }
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, costGuess);
        paymentAmountListView.setAdapter(adapter);
        paymentAmountListView.setBackgroundDrawable(getResources().getDrawable(R.color.black));
        paymentAmountListView.setSelection(listPosition);
        paymentAmountListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            paymentTotal.setText(costGuess[arg2]);
            paymentTotal.setSelection(costGuess[arg2].length());
            paymentAmountList.setVisibility(View.GONE);
           // next.setVisibility(View.VISIBLE);
        }});


    }

    protected void showPaymentAmountList() {
        paymentAmountList.setVisibility(View.VISIBLE);

        generatePaymentSuggestionListFor("");

    }
    void todaysTips() {
        final DecimalFormat currency = new DecimalFormat("$#0.00");
        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
        if (activity==null || activity.isFinishing()) return;

        // Get tip totals from database
        TipTotalData t = activity.dataBase.getTipTotal(activity,
                DataBase.Shift + " = " + activity.dataBase.getCurShift() + " AND " + DataBase.Payed + ">0",
                "WHERE shifts.ID="+DataBase.TodaysShiftCount);

        // Calculate tips from this run which have not yet been updated in the
        // database
        float thisOrderTip = 0;
        float thisOrderPayed = 0;
        try {
            thisOrderPayed = Float.parseFloat(paymentTotal.getText().toString());
            thisOrderTip = thisOrderPayed - order.cost;
            if (thisOrderTip < 0)
                thisOrderTip = 0;
        } catch (Exception e) {
        }

        float thisTripMileage = 0;

        // Calculate Mileage pay for the order just entered

        final String MilagePayPerTrip = activity.sharedPreferences.getString("per_delivery_pay", "-1");
        final String MilagePayPercent = activity.sharedPreferences.getString("percent_order_price", "-1");
        //final String MilagePayPerMile = sharedPreferences.getString("odometer_per_mile", "-1");
        final String MilagePayPerOutOfTownDelivery = activity.sharedPreferences.getString("per_out_of_town_delivery", "0");
        final String MilagePayPerRun = activity.sharedPreferences.getString("per_run_pay", "0");

        try {
            float f = Float.parseFloat(MilagePayPerTrip);
            if (f > 0) {
                thisTripMileage += f;
            }
        } catch (final NumberFormatException e) {
        }

        // Calculate mileage pay as % of order total
        try {
            float f = Float.parseFloat(MilagePayPercent);
            if (f > 0) {
                f = (f / 100) * (order.cost);
                thisTripMileage += f;
            }
        } catch (final NumberFormatException e) {
        }

        if (order.outOfTown1) {
            try {
                float f = Float.parseFloat(MilagePayPerOutOfTownDelivery);
                if (f > 0) {
                    thisTripMileage += f;
                }
            } catch (final NumberFormatException e) {
            }
        }

        if (startNewRun) {
            try {
                float f = Float.parseFloat(MilagePayPerRun);
                if (f > 0) {
                    thisTripMileage += f;
                }
            } catch (final NumberFormatException e) {
            }
        }

        tipsText.setText(currency.format(t.payed - t.cost + thisOrderTip));
        mileage.setText(currency.format(t.mileageEarned + thisTripMileage));
        earnings.setText(currency.format(t.total + thisOrderTip + thisTripMileage));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        if (order.phoneNumber != null && order.phoneNumber.length()>0){
            // Inflate menu resource file.
            inflater.inflate(R.menu.call_text_menus, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        OutTheDoorActivity activity = (OutTheDoorActivity)getActivity();
        if (activity==null || activity.isFinishing()) return false;

        switch (item.getItemId()){
            case R.id.textMessageClickable:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + order.phoneNumber, null));
                String messageBody = activity.sharedPreferences.getString("customerDefaultSMS", activity.getString(R.string.customerDefaultSMS));
                String cost = "" + Tools.getFormattedCurrency(order.cost);
                messageBody = messageBody.replace("###", cost);
                i.putExtra("sms_body", messageBody);
                startActivity(i);
                return true;

            case R.id.callClickable:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + order.phoneNumber)));
                return true;

            case R.id.MapIt:
                activity.tools.mapTo(order.address, activity);
                return true;

            case R.id.Navigate:
                activity.tools.navigateTo(order.address, activity);
                return true;

            case R.id.markUndeliverable:
                if (order.undeliverable==true){
                    order.undeliverable=false;
                } else {
                    order.undeliverable=true;
                }
                setPaymentEnabledState();
                return true;

            case R.id.editOrder:
                final Intent myIntent = new Intent(activity, SummaryActivity.class);
                myIntent.putExtra("DB Key", order.primaryKey);
                myIntent.putExtra("openEdit", true);
                startActivityForResult(myIntent, 0);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getActivity()==null || getActivity().isFinishing())
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        final GpsNotesListAdapter adapter = new GpsNotesListAdapter(getActivity());
        if (adapter.refresh(((OutTheDoorActivity)getActivity()).dataBase,latitude,longitude,true)==false){
            //TODO: Hide list header
        } else {
            gpsNotesList.removeAllViews();
            int count = adapter.getCount();
            if (count > 7) count=7;
            for (int i = 0; i < count; i++){
                View view = adapter.getView(i,null,null);
                gpsNotesList.addView(view);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
