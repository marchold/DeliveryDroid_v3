package catglo.com.deliverydroid.viewEditOrder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Utils;

public class SummaryViewFragment extends SummaryBaseFragment {
	
	private TextView mileageLabel1;
	private TextView mileageLabel2;
	private TextView mileageLabel3;
	private TextView mileageLabel4;
	private TextView mileagePay1;
	private TextView mileagePay2;
	private TextView mileagePay3;
	private TextView mileagePay4;
	private TextView paymentAmount;
	private TextView paymentType;
	private TextView paymentAmountSplit;
	private TextView paymentTypeSplit;
    protected TextView phoneNumber;


	@Override
	protected int getLayoutId() {
		return R.layout.summary_view;
	}
	
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
	
		mileageLabel1 = (TextView)view.findViewById(R.id.altMileagePayLabel1);
		mileageLabel2 = (TextView)view.findViewById(R.id.altMileagePayLabel2);
		mileageLabel3 = (TextView)view.findViewById(R.id.altMileagePayLabel3);
		mileageLabel4 = (TextView)view.findViewById(R.id.altMileagePayLabel4);
		
		mileagePay1 = (TextView)view.findViewById(R.id.altMileagePay1);
		mileagePay2 = (TextView)view.findViewById(R.id.altMileagePay2);
		mileagePay3 = (TextView)view.findViewById(R.id.altMileagePay3);
		mileagePay4 = (TextView)view.findViewById(R.id.altMileagePay4);
		
		paymentAmount = (TextView)view.findViewById(R.id.paymentAmount);
		paymentType = (TextView)view.findViewById(R.id.paymentType);
		
		paymentAmountSplit = (TextView)view.findViewById(R.id.paymentAmountSplit);
		paymentTypeSplit = (TextView)view.findViewById(R.id.paymentTypeSplit);

        phoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
		
		return view;		
	}
	
	void formatAltPay(String labelPrefKey, String amountPrefKey, TextView mileageLabel, TextView mileagePay, boolean isSet){
		try {
			String label = sharedPreferences.getString(labelPrefKey,getString(R.string.MileagePay));
			float amount = Float.parseFloat(sharedPreferences.getString(amountPrefKey,"0"));
			
			if (amount == 0 || isSet==false){
				mileageLabel.setVisibility(View.INVISIBLE);
				mileagePay.setVisibility(View.INVISIBLE);
			} else {
				mileageLabel.setVisibility(View.VISIBLE);
				mileagePay.setVisibility(View.VISIBLE);
				mileageLabel.setText(label);
				mileagePay.setText(Utils.getFormattedCurrency(amount));
			}
			
		} catch (NumberFormatException e){
			
		}
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		
		formatAltPay("per_out_of_town_delivery_label1","per_out_of_town_delivery" ,mileageLabel1,mileagePay1,order.outOfTown1);
		formatAltPay("per_out_of_town_delivery_label2","per_out_of_town_delivery2",mileageLabel2,mileagePay2,order.outOfTown2);
		formatAltPay("per_out_of_town_delivery_label3","per_out_of_town_delivery3",mileageLabel3,mileagePay3,order.outOfTown3);
		formatAltPay("per_out_of_town_delivery_label4","per_out_of_town_delivery4",mileageLabel4,mileagePay4,order.outOfTown4);
	
		String fullAddress = order.address;
		if (order.apartmentNumber.length()>0) {
			fullAddress = fullAddress+" Apt. "+order.apartmentNumber;
		}
        address.setText(fullAddress);

        if (order.notes==null || order.notes.length()==0){
        	notes.setVisibility(View.GONE);
        }
        
        String[] items = new String[] { 
  				getString(R.string.No),     //0
  				getString(R.string.Cash),   //1
  				getString(R.string.Check),  //2
  				getString(R.string.Credit), //3
  				getString(R.string.ebt)};	//4
        
        //Payment
        if (order.payed>=0 && order.paymentType>=0 && order.paymentType <= 4){
        	paymentAmount.setText(Utils.getFormattedCurrency(order.payed));
        	paymentType.setText(items[order.paymentType+1]);
        	paymentType.setVisibility(View.VISIBLE);
        } else {
        	paymentAmount.setText(R.string.undelivered);
        	paymentType.setVisibility(View.INVISIBLE);
        }
        if (order.payed2>0 && order.paymentType2>=0 && order.paymentType2 <= 4){
        	paymentAmountSplit.setText(Utils.getFormattedCurrency(order.payed2));
        	paymentTypeSplit.setText(items[order.paymentType2+1]);
        } else {
        	paymentAmountSplit.setVisibility(View.INVISIBLE);
        	paymentTypeSplit.setVisibility(View.INVISIBLE);
        }
		
	}
	
}
