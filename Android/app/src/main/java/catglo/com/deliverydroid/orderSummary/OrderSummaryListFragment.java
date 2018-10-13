package catglo.com.deliverydroid.orderSummary;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderSummaryListFragment extends ListFragment  {
	protected DataBase dataBase;
	protected SharedPreferences sharedPreferences;
	private ArrayList<Order> orders;
	protected ViewGroup noListAltView;
	protected Editor prefEditor;
	private Spinner listTypePicker;
	int viewingShift;
	private int currentListView;
	private BroadcastReceiver broadcastReceiver;
	private OnTouchListener gestureListener;
	
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefEditor = sharedPreferences.edit();
        
        gestureListener = ((OrderSummaryActivity)getActivity()).gestureTouchListener;
		
       
	}
	
	
	public void updateUI(){
		final ArrayList<HashMap<String, String>> listText = new ArrayList<HashMap<String, String>>();
		viewingShift = getArguments().getInt("viewingShift");
		orders = dataBase.getShiftOrderArray(viewingShift);
		Log.i("order","updateui: "+orders.size()+orders.toString()+orders);
		
		currentListView = sharedPreferences.getInt("orderListViewType", 0);
		
		//final Cursor c = dataBase.getShiftOrders(viewingShift);
		//int count = 0;
		final LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listTypePicker.setAdapter(new SpinnerAdapter(){
			public int getCount() {
				return 5;
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
				// TODO Auto-generated method stub
				return 0;
			}
			public View getView(int position, View convertView, ViewGroup parent) {
				return inflater.inflate(R.layout.check_out_list_header, null);
				
			}
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				switch (position){
					default:
					case 0: return inflater.inflate(R.layout.order_summary_list_item1, null);
					case 1: return inflater.inflate(R.layout.order_summary_list_item2, null);
					case 2: return inflater.inflate(R.layout.order_summary_list_item3, null);
					case 3: return inflater.inflate(R.layout.order_summary_list_item4, null);
					case 4: {
						View v = inflater.inflate(R.layout.order_summary_list_item5, null);
						View v2 = inflater.inflate(R.layout.order_summary_list_item5, null);
						LinearLayout ll = new LinearLayout(getActivity().getApplicationContext());
						ll.setOrientation(LinearLayout.VERTICAL);
						ll.addView(v);
						ll.addView(v2);
						return ll;
					}
				}
			}});
		
		
		listTypePicker.setSelection(currentListView);
		listTypePicker.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (currentListView!=arg2){
					Editor editor = sharedPreferences.edit();
					editor.putInt("orderListViewType", arg2);
					editor.commit();
					currentListView = arg2;
					updateUI();
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		float creditCardTotal=0;
		
		for (Order order : orders) {
			Log.i("order","looping: "+orders.size()+orders.toString());
			
			final HashMap<String, String> map = new HashMap<String, String>();
			map.put("number", order.number);
			map.put("cost", "" + Tools.getFormattedCurrency(order.cost));
			
			Float payed=0f;
			Float paid1=0f;
			Float paid2=0f;
			
			payed+=order.payed;
			paid1=order.payed2;
		
			payed+=order.payed2;
			paid2=order.payed2;
			
			if (payed==-1){
				map.put("payed", "unpaid");
				map.put("tip",   "");
				
			} else {
				map.put("payed", "" + Tools.getFormattedCurrency(payed));
				map.put("payed1", "" + Tools.getFormattedCurrency(paid1));
				map.put("tip", Tools.getFormattedCurrency(payed - order.cost));
			}
			
			map.put("notes", order.notes);
			map.put("address", order.address);

			int paymentType = order.paymentType;
			int paymentType2 = order.paymentType2;	
			map.put("paymentType",""+paymentType);
			
			if (paymentType==Order.CREDIT) {
				creditCardTotal+=paid1;
			}
			if (paymentType2==Order.CREDIT) {
				creditCardTotal+=paid2;
			}
			
			map.put("paidSplit", ""+order.payed2);
			
			map.put("paymentType2",""+paymentType2);
			
			map.put("id", ""+order.primaryKey);

            if (order.undeliverable){
                map.put("undeliverable","Y");
            }else {
                map.put("undeliverable","N");
            }
			listText.add(map);

		}
	
		Log.i("order","updateui: "+orders.size()+orders.toString());
		
		final int listSize = orders.size();
		getListView().setAdapter(new ListAdapter(){
			public int getCount() {
				return listSize;
			}
			public Object getItem(int position) {return null;}
			public long getItemId(int position) {return 0;}
			public int getItemViewType(int position) {return 0;}
			public View getView(int position, View convertView, ViewGroup parent) {
				LinearLayout layout;
				switch (currentListView) {
					default:
					case 0: layout= (LinearLayout)inflater.inflate(R.layout.order_summary_list_item1, null);
					break;
					case 1: layout= (LinearLayout)inflater.inflate(R.layout.order_summary_list_item2, null);
					break;
					case 2: layout= (LinearLayout)inflater.inflate(R.layout.order_summary_list_item3, null);
					break;
					case 3: layout= (LinearLayout)inflater.inflate(R.layout.order_summary_list_item4, null);
					break;
					case 4: layout= (LinearLayout)inflater.inflate(R.layout.order_summary_list_item5, null);
				}
				layout.setClickable(true);
				final HashMap<String, String> map = listText.get(position);
				layout.setOnTouchListener(gestureListener);
				layout.setOnClickListener(new OnClickListener(){public void onClick(View v) {
					final int orderIdForDialog = Integer.parseInt(map.get("id"));
					AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item);
					adapter.add(getString(R.string.EDITORDER));
					adapter.add(getString(R.string.Delete));
					alert.setAdapter(adapter, new DialogInterface.OnClickListener(){@SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int which) {
						if (which==0){
							final Intent myIntent = new Intent(getActivity().getApplicationContext(), SummaryActivity.class);
							myIntent.putExtra("DB Key", orderIdForDialog);
							startActivity(myIntent);
						}else {
							OrderSummaryActivity parent = (OrderSummaryActivity)getActivity();
							parent.recordToDelete = orderIdForDialog;
							parent.confirmDeleteRecordDialog();
						}
					}});
					alert.create().show();
				}});
				
				Float paidSplit = Float.parseFloat(map.get("paidSplit"));
				ImageView paymentType = (ImageView)layout.findViewById(R.id.icon_payment_type);
				if (paymentType!=null){
					switch(Integer.parseInt(map.get("paymentType"))){
						default:
						case Order.CASH:
							paymentType.setImageResource(R.drawable.icon_money);
						break;
						case Order.CHECK:
							paymentType.setImageResource(R.drawable.icon_check);
						break;
						case Order.CREDIT:
							paymentType.setImageResource(R.drawable.icon_credit);
						break;
						case Order.EBT:
							paymentType.setImageResource(R.drawable.icon_ebt);
						break;
						case Order.DEBIT:
							paymentType.setImageResource(R.drawable.icon_debit);
						break;
					}
				}
				ImageView paymentType2 = (ImageView)layout.findViewById(R.id.icon_payment_type2);
				if (paidSplit>0 && paymentType2!=null){
					paymentType2.setVisibility(View.VISIBLE);
					switch(Integer.parseInt(map.get("paymentType2"))){
						default:
						case Order.CASH:
							paymentType2.setImageResource(R.drawable.icon_money);
						break;
						case Order.CHECK:
							paymentType2.setImageResource(R.drawable.icon_check);
						break;
						case Order.CREDIT:
							paymentType2.setImageResource(R.drawable.icon_credit);
						break;
						case Order.EBT:
							paymentType2.setImageResource(R.drawable.icon_ebt);
						break;
						case Order.DEBIT:
							paymentType2.setImageResource(R.drawable.icon_debit);
						break;
					}
				}

                boolean undeliverable=false;
                if (map.get("undeliverable").equalsIgnoreCase("Y")){
                    undeliverable=true;
                }


				TextView address = (TextView)layout.findViewById(R.id.check_out_address);
				if (address!=null){
					address.setText(map.get("address"));
				}
				TextView notes = (TextView)layout.findViewById(R.id.check_out_notes);
				if (notes!=null){
					notes.setText(map.get("notes"));
				}
				TextView orderNumber = (TextView)layout.findViewById(R.id.check_out_order_number);
				if (orderNumber!=null){
					orderNumber.setText(map.get("number"));
				}
				TextView cost = (TextView)layout.findViewById(R.id.check_out_cost);
				if (cost!=null){
					cost.setText(map.get("cost"));
				}
				TextView payed = (TextView)layout.findViewById(R.id.check_out_payed);
				if (payed!=null){
                    if (undeliverable==false) {
                        if (paidSplit > 0) {
                            switch (currentListView) {
                                case 0:
                                    payed.setText(map.get("payed1") + "+" + Tools.getFormattedCurrency(paidSplit));
                                    break;
                                default:
                                case 1:
                                    payed.setText(map.get("payed"));
                                    break;
                            }
                        } else {
                            payed.setText(map.get("payed"));
                        }
                    } else {
                        payed.setText("no delivery");
                    }
				}

                TextView tip = (TextView) layout.findViewById(R.id.check_out_tip);
                if (tip != null) {
                    if (undeliverable==false) {
                        tip.setText(map.get("tip"));
                    } else {
                        tip.setText("no delivery");
                    }
                }
				
				return layout;
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
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		final OrderSummaryActivity activity = (OrderSummaryActivity)getActivity();
        activity.customizeListView.setVisibility(View.VISIBLE);
		activity.customizeListView.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			listTypePicker.performClick();
			activity.drawerLayout.closeDrawers();
		}});
		//TipTotalData tip = dataBase.getTipTotal(getActivity().getApplicationContext(),DataBase.Shift+"="+viewingShift+" AND "+DataBase.Payed+" >= 0");
	}

	
	@Override
	public void onResume(){
		super.onResume();
		if (dataBase == null) {
        	dataBase = new DataBase(getActivity().getApplicationContext());
        	dataBase.open();
        }
		broadcastReceiver = new BroadcastReceiver() {public void onReceive(Context context, Intent intent) {
    		updateUI();
    	}};
    	getActivity().registerReceiver(broadcastReceiver, new IntentFilter("catglo.com.deliverydroid.DBCHANGED"));sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		updateUI();
	}
	
	public void onPause(){
		super.onPause();
		getActivity().unregisterReceiver(broadcastReceiver);
		dataBase.close();
		dataBase=null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		View view = inflater.inflate(R.layout.order_summary_list_fragment, null);
		listTypePicker = (Spinner)view.findViewById(R.id.listTypePicker);
		
        
        return view;
	}
}
