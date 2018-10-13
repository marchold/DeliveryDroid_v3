package catglo.com.deliverydroid.orderSummary;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.DataBase.ShiftCounts;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.BaseDeliveryDroidFragment;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;

import java.text.DecimalFormat;
import java.util.Calendar;

public class OrderSummaryTotalsFragment extends BaseDeliveryDroidFragment {
	
	private TextView	moneyCollected;
	private TextView	tipsMade;
	private TextView	mileageEarned;
	private TextView	driverEarnings;
	private TextView	allOrders;
	private TextView	cashCollected;
	private TextView	cashTips;
	private TextView	cardCheckTips;
	private TextView 	cashOwedToStore;
	private TextView 	hoursWorked;
	private TextView 	hourlyPay;
		
	
	
	public OrderSummaryTotalsFragment() {
		super();
	}
	
	int viewingShift;
	private Spinner cashOwedHeader;
	private ViewGroup mileageEarnedLayout;
	private float cashCollectedAmount;
	private float cashTipsAmount;
	private float bankMinusDrops;
	private float driversEarningsAmount;
	private float mileageAmount;
	private float allTipsAmount;
	private float creditCardsInclTips;
	private float creditTipsAmount;
	private float totalCollectedAmount;
	private TextView otherCollected;
	private float creditCardTotal;
	private View view;
	
	
	synchronized public void updateUI(){
		viewingShift = getArguments().getInt("viewingShift");
		if (dataBase==null) return;
		final Cursor c = dataBase.getShiftOrders(viewingShift);
	
		creditCardTotal=0;
		if (c != null) {
			if (c.moveToFirst()) {
				final Calendar calendar = Calendar.getInstance();
							
				calendar.setTimeInMillis(Order.GetTimeFromString(c.getString(c.getColumnIndex(DataBase.Time))));
			
				do {
					Order order = new Order(c);
					
					Float payed=0f;
					Float paid1=0f;
					Float paid2=0f;
					payed +=order.payed;
					paid1 = order.payed;
				
					payed += order.payed2;
					paid2 =  order.payed2;
			
					if (order.paymentType==Order.CREDIT) {
						creditCardTotal+=paid1;
					}
					if (order.paymentType2==Order.CREDIT) {
						creditCardTotal+=paid2;
					}
					

				} while (c.moveToNext());
			} 
		}
		c.close();

		TipTotalData tip = dataBase.getTipTotal(getActivity(),DataBase.Shift+"="+viewingShift+" AND "+DataBase.Payed+" >= 0",
				"WHERE shifts.ID ="+viewingShift);
		
		
		
		DecimalFormat df = new DecimalFormat("#.#");
		int wholeHours = (int)tip.hours;
		float minutes = ((tip.hours-((float)wholeHours))*60);
		
		
		hoursWorked.setText(""+wholeHours+"h "+df.format(minutes)+"m ");
		hourlyPay.setText(Tools.getFormattedCurrency(tip.hourlyPay));
		
		
		moneyCollected.setText(Tools.getFormattedCurrency(tip.payed));
		cashCollected.setText(Tools.getFormattedCurrency(tip.payedCash));
		otherCollected.setText(Tools.getFormattedCurrency(tip.payed - tip.payedCash));
		
		final float totalTipsMade = tip.payed-tip.cost;
		tipsMade.setText(Tools.getFormattedCurrency(totalTipsMade));
		
		
		if (tip.mileageEarned!=0){
			mileageEarned.setText(Tools.getFormattedCurrency(tip.mileageEarned));// Mileage Earned:
			mileageEarnedLayout.setVisibility(View.VISIBLE);
		} else {
			mileageEarnedLayout.setVisibility(View.GONE);
		}
		//cashOwedIncludingMilage.setText(DeliveryDroidBaseActivity.getFormattedCurrency(tip.payedCash-tip.cashTips+bankAmount-totalDrops-tip.mileageEarned));
		
		cashTips.setText(Tools.getFormattedCurrency(tip.cashTips));
		cardCheckTips.setText(Tools.getFormattedCurrency(tip.reportableTips));
		
		driverEarnings.setText(Tools.getFormattedCurrency(totalTipsMade + tip.mileageEarned));
		
		
		allOrders.setText(Tools.getFormattedCurrency(tip.cost));
		
		//Setup member variables for cash owed to store configurable field
		cashCollectedAmount = tip.payedCash;
		totalCollectedAmount = tip.payed;
		cashTipsAmount = tip.cashTips;
		ShiftCounts counts = dataBase.getShiftCounts(viewingShift);
		if (counts.next<=0){
			int dropRowCount = sharedPreferences.getInt("DropRowCount", 1);
			float totalDrops = 0;
			for (int i = 0; i < dropRowCount; i++){
				totalDrops += sharedPreferences.getFloat("drop_val_"+i,0f); 
		    }
			float bankAmount = sharedPreferences.getFloat("BankAmount", 0);
			bankMinusDrops = bankAmount - totalDrops;
		} else {
			bankMinusDrops = 0;
		}
		driversEarningsAmount = totalTipsMade + tip.mileageEarned;
		mileageAmount = tip.mileageEarned;
		allTipsAmount = tip.payed-tip.cost;
		creditCardsInclTips = creditCardTotal;
		creditTipsAmount = tip.reportableTips;
		
		float cashOwedToStoreAmount = getCashOwedToStoreForKind(sharedPreferences.getInt("cashOwedKind", 0));
		cashOwedToStore.setText(Tools.getFormattedCurrency(cashOwedToStoreAmount));
	}
	
	float getCashOwedToStoreForKind(int kind){
		switch (kind) {
		default:
		case 0:return cashCollectedAmount - cashTipsAmount + bankMinusDrops;
		case 1:return cashCollectedAmount - cashTipsAmount - creditTipsAmount + bankMinusDrops;//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
		case 2:return cashCollectedAmount - driversEarningsAmount + bankMinusDrops;//Cash Collected - Drivers Earning + Bank - Drops
		case 3:return cashCollectedAmount - cashTipsAmount - mileageAmount + bankMinusDrops;//Cash Collected - Cash Tips - Mileage + Bank - Drops 
		case 4:return cashCollectedAmount - allTipsAmount + bankMinusDrops;//Cash Collected- All Tips + Bank - Drops
		case 5:return cashCollectedAmount - allTipsAmount - mileageAmount + bankMinusDrops;//Cash Collected- All Tips - MILEAGE + Bank - Drops
		case 6:return totalCollectedAmount - mileageAmount - creditCardsInclTips + bankMinusDrops;//TOTAL SALES - MILEAGE - CREDIT CARDS (incl. tips) + Bank - Drops
		case 7:return cashCollectedAmount - cashTipsAmount - creditCardTotal + bankMinusDrops;////Cash Collected - Cash Tips - Credit Cards - Credit Cards Tips + Bank - Drops
		case 8:return bankMinusDrops - creditTipsAmount - mileageAmount;//Bank - Drops - Credit Cards Tips - MILEAGE
		}
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.order_summary_totals_fragment, null);
		
		moneyCollected 	= (TextView) view.findViewById(R.id.osMoneyCollected);
		cashCollected 	= (TextView) view.findViewById(R.id.osCashCollected);
		otherCollected 	= (TextView) view.findViewById(R.id.otherCollected);
		tipsMade 		= (TextView) view.findViewById(R.id.osTipsMade);
		mileageEarned   = (TextView) view.findViewById(R.id.osMileageEarned);
		driverEarnings  = (TextView) view.findViewById(R.id.osDriverEarnings);
		cashTips 		= (TextView)view.findViewById(R.id.CashTips);
		cardCheckTips 	= (TextView)view.findViewById(R.id.osCreditTips);
		allOrders		= (TextView)view.findViewById(R.id.allOrders);
		cashOwedToStore	= (TextView)view.findViewById(R.id.creditCardMinusTip);
		cashOwedHeader  = (Spinner)view.findViewById(R.id.cashOwedToStoreHeader);
		hoursWorked     = (TextView)view.findViewById(R.id.hoursWorked);
		hourlyPay       = (TextView)view.findViewById(R.id.hourlyPay);
		mileageEarnedLayout = (ViewGroup)view.findViewById(R.id.mileageEarnedContainer);
		
		
		
        
		cashOwedHeader.setAdapter(new SpinnerAdapter(){
			public int getCount() {
				// TODO Auto-generated method stub
				return 9;
			}
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			public int getItemViewType(int position) {
				return R.layout.cash_owed_list_item;
			}
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView tv = new TextView(getActivity().getApplicationContext());
				tv.setText(R.string.Cash_owed_to_store);
				tv.setTextColor(ColorStateList.valueOf(0xFFFFFFFF));
				tv.setTextSize(12);
				return tv;
				
			}
			public int getViewTypeCount() {
				return 1;
			}
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
			public void registerDataSetObserver(DataSetObserver observer) {}
			public void unregisterDataSetObserver(DataSetObserver observer) {}
			public View getDropDownView(int position, View convertView,ViewGroup parent) {
				View view = inflater.inflate(R.layout.cash_owed_list_item, null);
				TextView title = (TextView)view.findViewById(R.id.textView1);
				TextView detail = (TextView)view.findViewById(R.id.textView2);
				switch (position){
				case 0:
					break;
				case 1:
					detail.setText(R.string.Cash_owed_1);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;	
				case 2:
					detail.setText(R.string.Cash_owed_2);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;
				case 3:
					detail.setText(R.string.Cash_owed_3);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;
				case 4:
					detail.setText(R.string.Cash_owed_4);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;
				case 5:
					detail.setText(R.string.Cash_owed_5);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;
				case 6:
					detail.setText(R.string.Cash_owed_6);//Cash Collected - Cash Tips - Credit Tips + Bank - Drops
					break;
				case 7:
					detail.setText(R.string.Cash_owed_7);//Cash Collected - Cash Tips - Credit Cards - Credit Cards Tips + Bank - Drops
					break;
				case 8:
					detail.setText(R.string.Cash_owed_8);//bank - credit tips - mileage
					break;
				
				}
				title.setText(Tools.getFormattedCurrency(getCashOwedToStoreForKind(position)));
				return view;
			}
		});
		cashOwedHeader.setOnItemSelectedListener(new  OnItemSelectedListener(){public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			prefEditor.putInt("cashOwedKind", arg2);
			prefEditor.commit();
			updateUI();
		}
		public void onNothingSelected(AdapterView<?> arg0) {}});


        OrderSummaryActivity activity = (OrderSummaryActivity) getActivity();
        activity.configCashOwedToStore.setVisibility(View.VISIBLE);
        activity.configCashOwedToStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashOwedHeader.performClick();
            }
        });


        return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		gestureListener = ((OrderSummaryActivity)getActivity()).gestureTouchListener;
		
        moneyCollected.setOnTouchListener(gestureListener);
        cashCollected.setOnTouchListener(gestureListener);
        otherCollected.setOnTouchListener(gestureListener);
        tipsMade.setOnTouchListener(gestureListener);
        mileageEarned.setOnTouchListener(gestureListener);
        driverEarnings.setOnTouchListener(gestureListener);
        cashTips.setOnTouchListener(gestureListener);
        cardCheckTips.setOnTouchListener(gestureListener);
        allOrders.setOnTouchListener(gestureListener);
        cashOwedToStore.setOnTouchListener(gestureListener);
        cashOwedHeader.setOnTouchListener(gestureListener);
        hoursWorked.setOnTouchListener(gestureListener);
        hourlyPay.setOnTouchListener(gestureListener);
        mileageEarnedLayout.setOnTouchListener(gestureListener);
        view.setOnTouchListener(gestureListener);
	}
	
}
