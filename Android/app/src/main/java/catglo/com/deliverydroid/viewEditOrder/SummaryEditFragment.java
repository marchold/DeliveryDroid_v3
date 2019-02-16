package catglo.com.deliverydroid.viewEditOrder;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Utils;
import catglo.com.deliverydroid.widgets.AddressAutocomplete;


import java.text.DecimalFormat;

public class SummaryEditFragment extends SummaryBaseFragment {
	private String[] items;
	private Spinner paymentType;
	private Spinner paymentTypeB;
	protected CheckBox outOfTown1;
	protected CheckBox outOfTown2;
	protected CheckBox outOfTown3;
	protected CheckBox outOfTown4;
	protected TextView apartNumber;
	protected TextView payment;
	protected TextView paymentB;
    private CheckBox undeliverableCheckBox;


	@Override
	protected int getLayoutId() {
		return R.layout.summary_edit;
	}
	

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		paymentType = (Spinner)view.findViewById(R.id.deliveryAreaSpinner);
		paymentTypeB = (Spinner)view.findViewById(R.id.spinner2);
		
		outOfTown1 = (CheckBox) view.findViewById(R.id.OutOfTown);
		outOfTown2 = (CheckBox) view.findViewById(R.id.OutOfTown2);		
		outOfTown3 = (CheckBox) view.findViewById(R.id.OutOfTown3);
		outOfTown4 = (CheckBox) view.findViewById(R.id.OutOfTown4);
		
		apartNumber = (TextView) view.findViewById(R.id.totalCredit);
		payment = (TextView)view.findViewById(R.id.textView7);
		paymentB = (TextView)view.findViewById(R.id.hourlyPayRate);

        undeliverableCheckBox = (CheckBox)view.findViewById(R.id.undeliverableCheckBox);
		
		return view;		
	}
	
	
	@Override
	public void onPause(){
		
		order.paymentType  = convertPaymentTypeToOrder(paymentType.getSelectedItemPosition());
		order.paymentType2 = convertPaymentTypeToOrder(paymentTypeB.getSelectedItemPosition());
	
		order.address = address.getEditableText().toString();
		order.number  = orderNumber.getEditableText().toString();
		order.notes   = notes.getEditableText().toString();
		if (order.paymentType == Order.NOT_PAID) {
			order.payed = -1;
		} else {
			order.payed   = Utils.parseCurrency(payment.getEditableText().toString());
		}
	
		order.phoneNumber = phoneNumber.getText().toString();
        ((EditText)phoneNumber).addTextChangedListener(new PhoneNumberFormattingTextWatcher());

		order.payed2  = Utils.parseCurrency(paymentB.getEditableText().toString());
		// Database stores a -1 in to values for un-payed orders, but this form does not.
		if (order.payed2 == 0) {
			if (order.paymentType == Order.NOT_PAID){
				order.payed = -1;
			}
		}
		
		try {
			order.cost = Utils.parseCurrency(price.getEditableText().toString());
		} catch (NumberFormatException e){};
		
		order.outOfTown1 = outOfTown1.isChecked();
		order.outOfTown2 = outOfTown2.isChecked();
		order.outOfTown3 = outOfTown3.isChecked();
		order.outOfTown4 = outOfTown4.isChecked();
		order.apartmentNumber = apartNumber.getEditableText().toString();

        order.undeliverable = undeliverableCheckBox.isChecked();

		dataBase.edit(order);
		
		super.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		
        address.setText(order.address);
        apartNumber.setText(order.apartmentNumber);	
		
		//Payment
		items = new String[] { 
				getString(R.string.No),     //0
				getString(R.string.Cash),   //1
				getString(R.string.Check),  //2
				getString(R.string.Credit), //3
				getString(R.string.ebt)};	//4
				
		setPaymentType(paymentType ,order.payed ,getString(R.string.undelivered),0,(EditText) payment ,order.paymentType);
		setPaymentType(paymentTypeB,order.payed2,getString(R.string.not_split)  ,1,(EditText) paymentB,order.paymentType2);
		
		//Delivery Time
        deliveryTime.setOnTouchListener(new OnTouchListener(){ public boolean onTouch(View arg0, MotionEvent arg1) {
        	DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
        	activity.getTools().showTimeSliderDialog(deliveryTime,order.payedTime,null, false);
		    return true;
		}});
        
        //Order Time
		orderTime.setOnTouchListener(new OnTouchListener(){ public boolean onTouch(View arg0, MotionEvent arg1) {
			DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
			activity.getTools().showTimeSliderDialog(orderTime,order.time,null, false);
		    return true;
		}});
	
        //Address
		AddressAutocomplete addressAutocomplete = (AddressAutocomplete)address;
		addressAutocomplete.startSuggestor(dataBase);
        
		
		//Alternate Mileage Pay
		DeliveryDroidBaseActivity activity = (DeliveryDroidBaseActivity)getActivity();
		activity.getTools().initOptionalCheckBox(outOfTown1,
                "per_out_of_town_delivery",//amount
                "per_out_of_town_delivery_label1",//label
                order.outOfTown1); //default if label is empty
		
		activity.getTools().initOptionalCheckBox(outOfTown2,
				"per_out_of_town_delivery2",//amount
				"per_out_of_town_delivery_label2",//label
				order.outOfTown2); //default if label is empty	
					
		activity.getTools().initOptionalCheckBox(outOfTown3,
				"per_out_of_town_delivery3",//amount
				"per_out_of_town_delivery_label3",//label
				order.outOfTown3); //default if label is empty	
					
					
		activity.getTools().initOptionalCheckBox(outOfTown4,
				"per_out_of_town_delivery4",//amount
				"per_out_of_town_delivery_label4",//label
				order.outOfTown4); //default if label is empty

        paymentType.setEnabled(true);
        paymentTypeB.setEnabled(true);
        payment.setEnabled(true);
        paymentB.setEnabled(true);

        if (order.payed + order.payed2 > 0 && order.undeliverable==false){
            undeliverableCheckBox.setEnabled(false);
        } else {
            if (order.undeliverable){
                paymentType.setEnabled(false);
                paymentTypeB.setEnabled(false);
                payment.setEnabled(false);
                paymentB.setEnabled(false);
            }
        }
        undeliverableCheckBox.setChecked(order.undeliverable);
    }
    
    private int convertPaymentTypeToOrder(int type){
		switch (type) {
			default:
			case 0: return Order.NOT_PAID;
			case 1: return Order.CASH;
			case 2: return Order.CHECK;
			case 3: return Order.CREDIT;
			case 4: return Order.EBT;
		}
    }

    private void setPaymentType(Spinner spinner, 
    		                    float p, 
    		                    String hint, 
    		                    int threshold, 
    		                    EditText payment, 
    		                    int paymentType) 
    {
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		if (p < threshold) {
			payment.setText("");
			payment.setHint(hint);
			spinner.setSelection(0);
		} else {
			DecimalFormat currency = new DecimalFormat("#.##");
			payment.setText(currency.format(p));
			switch (paymentType) {
			case Order.EBT:
				spinner.setSelection(4);
				break;
			case Order.CHECK:
				spinner.setSelection(2);
				break;
			case Order.CREDIT:
				spinner.setSelection(3);
				break;
			default:
			case Order.CASH:
				spinner.setSelection(1);
			}
		}
	}
    
 
    

}
