package catglo.com.deliverydroid;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import java.util.ArrayList;


public class ListAddressHistoryActivity extends DeliveryDroidBaseActivity {

    private String searchString;
    private String apartmentSearchString="";
	private ArrayList<Order> orders;
	private ListView list;
	private TextView searchField;
	boolean showAddress;
	private EditText apartmentNumber;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_address_history);
        
        list = (ListView)findViewById(R.id.orderList);
        ((FrameLayout)findViewById(R.id.backButton)).setOnClickListener(new OnClickListener(){public void onClick(View v) {
			finish();
		}});
        
        apartmentNumber = (EditText)findViewById(R.id.aptNumberField);
        apartmentNumber.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				apartmentSearchString = s.toString();
				updateList();
			}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
		});
        
        searchField = (TextView)findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				searchString = s.toString();
				updateList();
			}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
		});
        
        
        Intent intent = getIntent();
        int dbKey = intent.getIntExtra("DB Key",-1);
        if (dbKey>0){
        	Order order = getDataBase().getOrder(dbKey);
        	searchString = order.address;
        	apartmentSearchString = order.apartmentNumber;
        	apartmentNumber.setText(apartmentSearchString);
        	TextView actionBarHeader = (TextView)findViewById(R.id.actionBarTopRightText);
        	actionBarHeader.setText("");
        	searchField.setText(searchString);
        	showAddress=false;
        } else {
        	showAddress=true;
        }
    }
	
	protected void onResume(){
		super.onResume();
		updateList();
	}
	
	void updateList(){
		
    	orders = getDataBase().searchForOrders(searchString,apartmentSearchString);
    	
		final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list.setAdapter(new ListAdapter(){

			@Override
			public int getCount() {
				return orders.size();
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getItemViewType(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.order_summary_list_item2, null);
				layout.setClickable(true);
				ImageView paymentType = (ImageView)layout.findViewById(R.id.icon_payment_type);
				ImageView paymentType2 = (ImageView)layout.findViewById(R.id.icon_payment_type2);
				
				final Order order = orders.get(position);
				switch (order.paymentType){
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
					
				if (order.payed2>0){
					switch (order.paymentType2){
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
				
				
				TextView notes = (TextView)layout.findViewById(R.id.check_out_address); //Hack we use address field for notes and order #
				if (notes!=null){
					if (showAddress){
						String addressUpToComma;
						try {
							addressUpToComma = order.time.toLocaleString().substring(0, order.time.toLocaleString().indexOf(','));
						} catch (StringIndexOutOfBoundsException e){
							addressUpToComma = order.time.toLocaleString();
						}
						notes.setText(addressUpToComma +": "+order.address);
					} else {
						notes.setText(order.time.toLocaleString());
					}
				}
				TextView cost = (TextView)layout.findViewById(R.id.check_out_cost);
				if (cost!=null){
					cost.setText(Utils.getFormattedCurrency(order.cost));
				}
				TextView payed = (TextView)layout.findViewById(R.id.check_out_payed);
				payed.setText(Utils.getFormattedCurrency(order.payed + order.payed2));
				
				TextView tip = (TextView)layout.findViewById(R.id.check_out_tip);
				tip.setText(Utils.getFormattedCurrency(order.payed + order.payed2 - order.cost));
				
				TextView extra_notes = (TextView)layout.findViewById(R.id.extra_notes);
				if (order!=null && extra_notes!=null && order.notes.length()>0){
					extra_notes.setVisibility(View.VISIBLE);
					extra_notes.setText(order.notes);
				}
			
				layout.setOnClickListener(new OnClickListener(){public void onClick(View v) {
					// 1. Instantiate an AlertDialog.Builder with its constructor
					AlertDialog.Builder builder = new AlertDialog.Builder(ListAddressHistoryActivity.this);

					// 2. Chain together various setter methods to set the dialog characteristics
					builder.setItems(new CharSequence[]{getString(R.string.Edit),getString(R.string.Delete)}, new DialogInterface.OnClickListener() {
				               public void onClick(DialogInterface dialog, int which) {
				                   switch (which){
				                   default:
				                   case 0:
				                	   startActivity(new Intent(getApplicationContext(), SummaryActivity.class).putExtra("DB Key", order.primaryKey));
				                	   break;
				                   case 1:
				                	   getDataBase().delete(order.primaryKey);
				   				       updateList();
				                	   break;
				                   }
				               }});

					// 3. Get the AlertDialog from create()
					AlertDialog dialog = builder.create();
					dialog.show();
					
				}});
				
				return layout;
			}

			@Override
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 1;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return true;
			}});
		//list.setOnItemClickListener(new OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			
		//}});
	}
}
