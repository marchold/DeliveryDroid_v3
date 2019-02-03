package catglo.com.deliverydroid.outTheDoor;


import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.DataBase.NoteEntry;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliveryDatabase.TipTotalData;
import catglo.com.deliverydroid.*;
import catglo.com.deliverydroid.homeScreen.HomeScreenActivity;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutTheDoorActivity extends DeliveryDroidBaseActivity implements ActionBar.TabListener {
	private static final int EDIT_ORDER = 1;
	private static final int PREVIOUS_ORDER = 2;
	private static final int CALL_STORE = 3;
	//private static final int CALL_NOTES_NUM = 4;

	private EditText paymentTotal;
	private TextView orderTimes;
	private Button next;
	private Button split;
	private RadioButton cash;
	private RadioButton check;
	private RadioButton credit;
	private TextView tipsText;

	private Cursor data;
	private final LinkedList<Order> orders = new LinkedList<Order>();
	private int orderCount = 0;
	Thread textUpdateThread;

	private int listPosition;

	// Need handler for callbacks to the UI thread
	final Handler messageHandler = new Handler();

	int orderCounter = 0;
	private View navigate;
	private View mapIt;
	private TextView mileage;
	private TextView earnings;
	private TextView currentAddress;
	private TextView currentCost;
	private TextView currentWait;

	boolean startNewRun = false;
	private RadioButton ebt;

	private EditText notesThisOrder;
	private View callThemButton;
	private View smsThemButton;
	private ViewGroup notesThisAddressLayout;
	private ViewGroup notesLayout;
	private LayoutInflater inflator;
	private RadioGroup radioGroup;

	boolean inNext = false;
	
	RadioButton cash2;
	RadioButton check2;
	RadioButton credit2;
	RadioButton ebt2;
	EditText paymentTotal2;
	RadioGroup group2;
	private View gpsNotesButton;
	private ViewGroup paymentAmountList;

	ArrayList<String> phoneNumbersThisOrder = new ArrayList<String>();
	private AdapterView<ArrayAdapter<String>> paymentAmountListView;
	private ViewGroup rootLayout;
	private EditText tipTotal;
    private View markUndeliverableButton;
    private View undeliverableOverlay;

    protected void generatePaymentSuggestionListFor(String soFar) {
		
		final int NUMBER_OF_GUESSES = 40;
		
		final String[] costGuess = new String[NUMBER_OF_GUESSES];
		cost = orders.get(orderCounter).cost;
		
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
					startValue = Utils.parseCurrency(c.toString());
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
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(OutTheDoorActivity.this, android.R.layout.simple_list_item_1, costGuess);
		paymentAmountListView.setAdapter(adapter);
        paymentAmountListView.setBackgroundDrawable(getResources().getDrawable(R.color.black));
		paymentAmountListView.setSelection(listPosition);
		paymentAmountListView.setOnItemClickListener(new OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			paymentTotal.setText(costGuess[arg2]);
			paymentTotal.setSelection(costGuess[arg2].length());
			paymentAmountList.setVisibility(View.GONE);
			next.setVisibility(View.VISIBLE);
		}});
	}
	
	protected void showPaymentAmountList() {
		paymentAmountList.setVisibility(View.VISIBLE);
		
		generatePaymentSuggestionListFor("");

	}
	//boolean undeliverable;
	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.out_the_door_new);




		inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Intent intent = getIntent();
		startNewRun = intent.getBooleanExtra("startNewRun", false);

		orderCounter = 0;

		rootLayout = (ViewGroup)findViewById(R.id.scrollView);
		rootLayout.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			paymentAmountListView.setVisibility(View.GONE);
		}});
		
		next = findViewById(R.id.arrivedAt);
		paymentAmountListView  = (AdapterView<ArrayAdapter<String>>) findViewById(R.id.paymentAmountListView);
		paymentAmountList = (ViewGroup) findViewById(R.id.paymentAmountList);
		paymentAmountList.setVisibility(View.GONE);
		paymentTotal = (EditText) findViewById(R.id.paymentTotal);
		paymentTotal2 = (EditText) findViewById(R.id.paymentTotal_2);
		tipTotal = (EditText) findViewById(R.id.tipAmount);
		if (getSharedPreferences().getBoolean("showTipField", false)==false){
			tipTotal.setVisibility(View.GONE);
		}
		paymentTotal.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			if (paymentTotal.getText().toString().length() < 2) {
				showPaymentAmountList();
			}
		}});
		paymentTotal.setOnFocusChangeListener(new OnFocusChangeListener(){public void onFocusChange(View arg0, boolean hasFocus) {
			if (hasFocus==false){
				paymentAmountList.setVisibility(View.GONE);
			}
		}});
		OnClickListener paymentTypeClickListener = new OnClickListener() {
			public void onClick(View arg0) {
				
				if (paymentTotal.getText().toString().length() < 2) {
					showPaymentAmountList();
				} else {
					next.setVisibility(View.VISIBLE);
				}
			}
		};
		paymentTotal2.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable arg0) {	
				if (inNext) return;
				try {
					if (paymentTotal2.isFocused()){
						float f = Utils.parseCurrency(paymentTotal2.getText().toString());
						f += Utils.parseCurrency(paymentTotal.getText().toString());
						f -=  orders.get(orderCounter).cost;
						tipTotal.setText(Utils.getFormattedCurrency(f));
					}
				} catch (IndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		paymentTotal.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable arg0) {
				if (inNext) return;
				next.setVisibility(View.VISIBLE);
				if (arg0.toString().length()>2) {
					paymentAmountList.setVisibility(View.GONE);
					todaysTips();
					
					if (paymentTotal.isFocused()){
						float f = Utils.parseCurrency(arg0.toString());
						f -=  orders.get(orderCounter).cost;
						tipTotal.setText(Utils.getFormattedCurrency(f));
					}
					
				} else {
					if (paymentTotal.isFocused() && paymentTotal2.getVisibility()!=View.VISIBLE){
						generatePaymentSuggestionListFor(arg0.toString());
					} else {
						paymentAmountList.setVisibility(View.GONE);
					}
				}
			}
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		tipTotal.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable arg0) {
				if (inNext) return;
				try {
					if (tipTotal.isFocused()){
						float f = Utils.parseCurrency(arg0.toString());
						f += orders.get(orderCounter).cost;
						paymentTotal.setText(Utils.getFormattedCurrency(f));
					}
				} catch (IndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
		});
		tipTotal.setOnEditorActionListener(new OnEditorActionListener() {
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	            if (actionId == EditorInfo.IME_ACTION_NEXT) {
	            	next.performClick();
	                return true;
	            }
	            return false;
	        }
	    });
		
		// currentText = (TextView) findViewById(R.id.current);
		orderTimes = (TextView) findViewById(R.id.orderTimes);
		cash = (RadioButton) findViewById(R.id.cash_button);
		check = (RadioButton) findViewById(R.id.check_button);
		credit = (RadioButton) findViewById(R.id.credit_button);
		ebt = (RadioButton) findViewById(R.id.ebt_button);

		

		radioGroup = (RadioGroup) findViewById(R.id.cash_check_card);

		navigate = findViewById(R.id.Navigate);
		mapIt = findViewById(R.id.MapIt);
		tipsText = (TextView) findViewById(R.id.TipsText);
		mileage = (TextView) findViewById(R.id.MileageEarned);
		earnings = (TextView) findViewById(R.id.DriverEarnings);
		split = (Button) findViewById(R.id.split_order);

		currentAddress = (TextView) findViewById(R.id.currentAddress);
		currentCost = (TextView) findViewById(R.id.currentCost);
		currentWait = (TextView) findViewById(R.id.currentWait);
		undeliverableOverlay = findViewById(R.id.undeliverableOverlay);
		notesThisOrder = (EditText) findViewById(R.id.notesThisOrder);

		callThemButton =  findViewById(R.id.callClickable);
		smsThemButton = findViewById(R.id.textMessageClickable);
		
		
		notesThisAddressLayout = (ViewGroup) findViewById(R.id.notesThisAddressLayout);
		notesLayout = (ViewGroup) findViewById(R.id.notesLayout);

		gpsNotesButton = findViewById(R.id.gpsNotesButton);
		gpsNotesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), GpsNotes.class));
			}
		});

		Editor prefeditor = getSharedPreferences().edit();
		prefeditor.putBoolean("justClearedBank", false);
		prefeditor.commit();

		if (getSharedPreferences().getBoolean("disableGPS", false)) {
			navigate.setVisibility(View.GONE);
			mapIt.setVisibility(View.GONE);
		} else {
			navigate.setOnClickListener(new OnClickListener() {
				public void onClick(final View v) {
					String addressToNavTo;
					if (orders.get(orderCounter).isValidated) {
						addressToNavTo = orders.get(orderCounter).geoPoint.toString();
					} else {
						addressToNavTo = orders.get(orderCounter).address;
					}
					getTools().navigateTo(addressToNavTo, OutTheDoorActivity.this);
				}
			});
			if (mapIt != null) {
				mapIt.setOnClickListener(new OnClickListener() {
					public void onClick(final View v) {
						String addressToNavTo = orders.get(orderCounter).address;
						getTools().mapTo(addressToNavTo, OutTheDoorActivity.this);
					}
				});
			}
		}

	

		

		data = getDataBase().getUndeliveredOrders();
		if (data != null) {
			if (data.moveToFirst()) {
				orderCount = 1;
				orders.add(new Order(data));
				while (data.moveToNext() && orderCount < 20) {
					orders.add(new Order(data));
					orderCount++;
				}
			}
			data.close();
		}
		if (orders.size() == 0) {
			// This happens upon restart from icon
			startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
			finish();
			return;
		}

        //undeliverable = orders.get(orderCounter).undeliverable;

        if (orders.get(0).payed2 != 0) {
			next.setVisibility(View.INVISIBLE);
		}
		if (orderCounter == orderCount - 1) {
			next.setText(R.string.Done);
		}

		// We store the pre-tip value in the payed2 field
		if (orders.get(orderCounter).payed2 != 0) {
			float total = orders.get(orderCounter).payed2 + orders.get(orderCounter).cost;
			paymentTotal.setText(new DecimalFormat("#.##").format(total));
			next.setVisibility(View.VISIBLE);
		} else {
			paymentTotal.setText("");
		}
		orders.get(orderCounter).payed2 = 0;

		notesThisOrder.setText(orders.get(0).notes);

        markUndeliverableButton = findViewById(R.id.markUndeliverable);
        markUndeliverableButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                next.setVisibility(View.VISIBLE);
                Order order = orders.get(orderCounter);
                if (order.undeliverable){
                    order.undeliverable=false;
                    undeliverableOverlay.setVisibility(View.GONE);
                } else {
                    order.undeliverable=true;
                    undeliverableOverlay.setVisibility(View.VISIBLE);
                }
                setPaymentEnabledState();
            }
        });
        View editOrder = findViewById(R.id.editOrder);
        editOrder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getApplicationContext();
                final Intent myIntent = new Intent(context, SummaryActivity.class);
                myIntent.putExtra("DB Key", orders.get(orderCounter).primaryKey);
                myIntent.putExtra("openEdit", true);
                startActivityForResult(myIntent, 0);
            }
        });

        View callStore =  findViewById(R.id.callStore);
        callStore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getSharedPreferences().getString("storePhoneNumber", "");
                if (phoneNumber.length() < 1) {
                    Toast.makeText(getApplicationContext(), R.string.missing_phone_number, Toast.LENGTH_LONG).show();
                } else {
                    String uri = "tel:" + phoneNumber;
                    if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()) {
                        uri = phoneNumber;
                    }
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });



        View previousOrder = findViewById(R.id.previousOrder);
		if (orderCounter ==0) {
			previousOrder.setVisibility(View.GONE);
		}

        previousOrder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderCounter != 0) {
                    try {
                        orders.get(orderCounter).payed = Utils.parseCurrency(paymentTotal.getText().toString());
                    } catch (final NumberFormatException e) {
                        orders.get(orderCounter).payed = 0;
                    }
                    orderCounter--;
                    messageHandler.post(updateOrderTimers);
                    paymentTotal.setText("" + orders.get(orderCounter).payed);
                    tipTotal.setText(""+(orders.get(orderCounter).payed-orders.get(orderCounter).cost));
                    next.setText("Next");
                    next.setVisibility(View.VISIBLE);
                    if (orderCounter ==0) {
                        previousOrder.setVisibility(View.GONE);
                    }
                }
            }
        });


        cash2 = (RadioButton) findViewById(R.id.cash_button2);
		check2 = (RadioButton) findViewById(R.id.check_button2);
		credit2 = (RadioButton) findViewById(R.id.credit_button2);
		ebt2 = (RadioButton) findViewById(R.id.ebt_button2);
		group2 = (RadioGroup) findViewById(R.id.cash_check_card2);

		if (getSharedPreferences().getBoolean("usePaymentCash", true) == false) {
			cash.setVisibility(View.GONE);
			cash2.setVisibility(View.GONE);
		}
		if (getSharedPreferences().getBoolean("usePaymentCheck", true) == false) {
			check.setVisibility(View.GONE);
			check2.setVisibility(View.GONE);
		}
		if (getSharedPreferences().getBoolean("usePaymentCredit", true) == false) {
			credit.setVisibility(View.GONE);
			credit2.setVisibility(View.GONE);
		}
		if (getSharedPreferences().getBoolean("usePaymentEbt", true) == false) {
			ebt.setVisibility(View.GONE);
			ebt2.setVisibility(View.GONE);
		}

		View backButton = findViewById(R.id.backButton);
	
		backButton.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			finish();
		}});
		
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				synchronized (OutTheDoorActivity.this) {
					inNext = true;
					
					tipTotal.setFocusable(true);
					tipTotal.setFocusableInTouchMode(true);

					//Hide soft keyboard 2 different ways
					getTools().hideOnScreenKeyboard();
					
					float payment = 0;
					float payment2 = 0;
					try {
						payment = Utils.parseCurrency(paymentTotal.getText().toString());
					} catch (final NumberFormatException e) {
					}
					try {
						payment2 = Utils.parseCurrency(paymentTotal2.getText().toString());
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

					getDataBase().setOrderPayment(orders.get(orderCounter).primaryKey, payment, paymentType, payment2, paymentType2, startNewRun, notes, orders.get(orderCounter).undeliverable);
					startNewRun = false;


					orders.get(orderCounter).payed = payment;
                    //undeliverable = orders.get(orderCounter).undeliverable;
					orderCounter++;
                    previousOrder.setVisibility(View.VISIBLE);
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
		});

		paymentTotal.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
				if (paymentTotal.getText().toString().length() > 0) {
					next.setVisibility(View.VISIBLE);
					todaysTips();
				} else {
					next.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});

		split.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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
				    paymentTotal.setText(""+orders.get(orderCounter).cost);
				}
			}
		});

		
		cash.setOnClickListener(paymentTypeClickListener);
		check.setOnClickListener(paymentTypeClickListener);
		credit.setOnClickListener(paymentTypeClickListener);
		ebt.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				paymentTotal.setText("" + orders.get(orderCounter).cost);
				next.setVisibility(View.VISIBLE);
			}
		});

	}

    private void setPaymentEnabledState() {
        try {
            Order order = orders.get(orderCounter);
            if (order.undeliverable == false) {
				((TextView)findViewById(R.id.markUndeliverableText)).setText(R.string.mark_nl_undeliverable);
                undeliverableOverlay.setVisibility(View.GONE);
                paymentTotal.setEnabled(true);
                cash.setEnabled(true);
                check.setEnabled(true);
                credit.setEnabled(true);
                ebt.setEnabled(true);
                split.setEnabled(true);
                paymentTotal.setHint("Payment");

            } else {
                undeliverableOverlay.setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.markUndeliverableText)).setText(R.string.mark_nl_deliverable);
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
	protected void onResume() {
		super.onResume();
		textUpdateThread = new Thread(new everyMinute());
		textUpdateThread.start();
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(HomeScreenActivity.DELIVERY_NOTIFICATION);
	}

	@Override
	protected void onPause() {

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (orderCounter < orderCount) {
			String chanelId = (((DeliveryDroidApplication)getApplication()).getNotificationChannelId());
		    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,chanelId)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Pending Deliveries")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentText(orders.get(orderCounter).address)
                    .setChannelId(chanelId);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(OutTheDoorActivity.class);
			stackBuilder.addNextIntent(new Intent(this, OutTheDoorActivity.class));
			mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
                NotificationChannel mChannel = new NotificationChannel(chanelId, name, NotificationManager.IMPORTANCE_LOW );
                mNotificationManager.createNotificationChannel(mChannel);
            }
			mNotificationManager.notify(HomeScreenActivity.DELIVERY_NOTIFICATION, mBuilder.build());
		} else {
			mNotificationManager.cancel(HomeScreenActivity.DELIVERY_NOTIFICATION);
		}

		super.onPause();
		textUpdateThread.interrupt();
	}

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    class everyMinute implements Runnable {
		// @Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				messageHandler.post(updateOrderTimers);
				try {
					Thread.sleep(60000);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	float cost;

    // Create runnable for posting
	final Runnable updateOrderTimers = new Runnable() {
		public void run() {
			synchronized (OutTheDoorActivity.this) {
				// fix a race condition bug, I think the 1 minute mark lines up
				// with exit and we try to refresh past the last order
				// at java.util.LinkedList.get(LinkedList.java:461)

				if (orders == null || orderCounter >= orders.size())
					return;

				int minutesAgo = orders.get(orderCounter).getMinutesAgo();// (int)
																			// ((System.currentTimeMillis()
																			// -
																			// orders.get(orderCounter).time.getTime())
																			// /
																			// 1000)
																			// /
																			// 60;

				currentAddress.setText(orders.get(orderCounter).address + " " + orders.get(orderCounter).apartmentNumber);
				currentWait.setText("" + minutesAgo);
				currentCost.setText(Utils.currencySymbol() + orders.get(orderCounter).cost);

				String pending = "";// new
									// String(getString(R.string.MinutesAddress));
				for (int i = orderCounter + 1; i < orders.size(); i++) {
					minutesAgo = (int) ((System.currentTimeMillis() - orders.get(i).time.getTime()) / 1000) / 60;
					pending = pending + minutesAgo + "min\t" + orders.get(i).address + "\n";
				}
				orderTimes.setText(pending);

				

				//final ArrayAdapter<String> adapter = new ArrayAdapter<String>(This, android.R.layout.simple_dropdown_item_1line, costGuess);
				// paymentTotal.setAdapter(adapter);
				// paymentTotal.setThreshold(1);
				// paymentAmountList.setAdapter(adapter);

				todaysTips();

				// ViewGroup NotesListItems = (ViewGroup)inflator.inflate(
				// R.layout.note_list_item, null);

				ArrayList<NoteEntry> s = getDataBase().getPastNotesForAddressAndAptNo(orders.get(orderCounter).address, orders.get(orderCounter).apartmentNumber);
				notesThisAddressLayout.removeAllViews();
				for (NoteEntry ne : s) {
					ViewGroup view = makeViewFromNoteEntry(ne);
					notesThisAddressLayout.addView(view);
				}

				// notesBody2.setText(s);
				s = getDataBase().getPastNotesForAddress(orders.get(orderCounter).address);
				// notesBody.setText(s);
				notesLayout.removeAllViews();
				phoneNumbersThisOrder.clear();
				for (NoteEntry ne : s) {
					ViewGroup view = makeViewFromNoteEntry(ne);
					notesLayout.addView(view);
				}

                final String phoneNumber = orders.get(orderCounter).phoneNumber;
                if (phoneNumber!=null && phoneNumber.length()>0){
                    callThemButton.setVisibility(View.VISIBLE);
                    callThemButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()){
                                try {
                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber)));
                                } catch (ActivityNotFoundException e){
                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                                }
                            }else {
                                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                            }
                        }
                    });
                    smsThemButton.setVisibility(View.VISIBLE);
                    smsThemButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i;
                            if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()) {
                                i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",  phoneNumber, null));
                            } else {
                                i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + phoneNumber, null));
                            }
                            String messageBody = getSharedPreferences().getString("customerDefaultSMS", getApplicationContext().getString(R.string.customerDefaultSMS));
                            String cost = "" + Utils.getFormattedCurrency(orders.get(orderCounter).cost);
                            messageBody = messageBody.replace("###", cost);

                            i.putExtra("sms_body", messageBody);
                            startActivity(i);
                        }
                    });
                } else {
                    //Legacy stuff from phone in notes thing
                    if (getSharedPreferences().getBoolean("callPhoneInNotes", false) && phoneNumbersThisOrder.size() > 0) {

                        callThemButton.setVisibility(View.VISIBLE);
                        callThemButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (phoneNumbersThisOrder.size() > 1) {
                                    DialogFragment dialog = new DialogFragment() {
                                        @Override
                                        public Dialog onCreateDialog(Bundle savedInstanceState) {

                                            CharSequence[] list = phoneNumbersThisOrder.toArray(new CharSequence[phoneNumbersThisOrder.size()]);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(R.string.Call).setItems(list, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()){
                                                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse( phoneNumbersThisOrder.get(which))));
                                                    } else {
                                                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumbersThisOrder.get(which))));
                                                    }
                                                }
                                            });
                                            return builder.create();
                                        }
                                    };
                                    dialog.show(getSupportFragmentManager(), "set_pay_rate");
                                } else {
                                    if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()) {
                                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse( phoneNumbersThisOrder.get(0))));
                                    } else {
                                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumbersThisOrder.get(0))));
                                    }
                                }
                            }
                        });
                        smsThemButton.setVisibility(View.VISIBLE);
                        smsThemButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (phoneNumbersThisOrder.size() > 1) {
                                    DialogFragment dialog = new DialogFragment() {
                                        @Override
                                        public Dialog onCreateDialog(Bundle savedInstanceState) {

                                            final CharSequence[] list = phoneNumbersThisOrder.toArray(new CharSequence[phoneNumbersThisOrder.size()]);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(R.string.textMessage).setItems(list, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + list[which], null));

                                                    String messageBody = getSharedPreferences().getString("customerDefaultSMS", getApplicationContext().getString(R.string.customerDefaultSMS));
                                                    String cost = "" + Utils.getFormattedCurrency(orders.get(orderCounter).cost);
                                                    messageBody = messageBody.replace("###", cost);

                                                    i.putExtra("sms_body", messageBody);
                                                    startActivity(i);
                                                }
                                            });
                                            return builder.create();
                                        }
                                    };
                                    dialog.show(getSupportFragmentManager(), "set_pay_rate");
                                } else {
                                    Intent i;
                                    if (new Settings(getApplicationContext()).omitTelFromPhoneNumbers()) {
                                        i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumbersThisOrder.get(0), null));
                                    } else {
                                        i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "tel:" + phoneNumbersThisOrder.get(0), null));
                                    }
                                    String messageBody = getSharedPreferences().getString("customerDefaultSMS", getApplicationContext().getString(R.string.customerDefaultSMS));
                                    String cost = "" + Utils.getFormattedCurrency(orders.get(orderCounter).cost);
                                    messageBody = messageBody.replace("###", cost);

                                    i.putExtra("sms_body", messageBody);
                                    startActivity(i);
                                }
                            }
                        });
                    } else {
                        callThemButton.setVisibility(View.GONE);
                        smsThemButton.setVisibility(View.GONE);
                    }
                }
			}
		}
	};

	void todaysTips() {
		final DecimalFormat currency = new DecimalFormat(Utils.currencySymbol()+"#0.00");

		// Get tip totals from database
		TipTotalData t = getDataBase().getTipTotal(this, DataBase.Shift + " = " + getDataBase().getCurShift() + " AND " + DataBase.Payed + ">0",
				"WHERE shifts.ID="+DataBase.TodaysShiftCount);

		// Calculate tips from this run which have not yet been updated in the
		// database
		float thisOrderTip = 0;
		float thisOrderPayed = 0;
		try {
			thisOrderPayed = Utils.parseCurrency(paymentTotal.getText().toString());
			thisOrderTip = thisOrderPayed - orders.get(orderCounter).cost;
			if (thisOrderTip < 0)
				thisOrderTip = 0;
		} catch (Exception e) {
		}

		float thisTripMileage = 0;

		// Calculate Mileage pay for the order just entered

		final String MilagePayPerTrip = getSharedPreferences().getString("per_delivery_pay", "-1");
		final String MilagePayPercent = getSharedPreferences().getString("percent_order_price", "-1");
		//final String MilagePayPerMile = sharedPreferences.getString("odometer_per_mile", "-1");
		final String MilagePayPerOutOfTownDelivery = getSharedPreferences().getString("per_out_of_town_delivery", "0");
		final String MilagePayPerRun = getSharedPreferences().getString("per_run_pay", "0");

		try {
			float f = Utils.parseCurrency(MilagePayPerTrip);
			if (f > 0) {
				thisTripMileage += f;
			}
		} catch (final NumberFormatException e) {
		}

		// Calculate mileage pay as % of order total
		try {
			float f = Float.parseFloat(MilagePayPercent);
			if (f > 0) {
				f = (f / 100) * (orders.get(orderCounter).cost);
				thisTripMileage += f;
			}
		} catch (final NumberFormatException e) {
		}

		if (orders.get(orderCounter).outOfTown1) {
			try {
				float f = Utils.parseCurrency(MilagePayPerOutOfTownDelivery);
				if (f > 0) {
					thisTripMileage += f;
				}
			} catch (final NumberFormatException e) {
			}
		}

		if (startNewRun) {
			try {
				float f = Utils.parseCurrency(MilagePayPerRun);
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

	static Pattern phoneNumberPattern = Pattern.compile("([0-9]{0,4})\\){0,1}\\s{0,1}\\-{0,1}([0-9]{3,4})\\s{0,1}\\-{0,1}\\s{0,1}([0-9]{4})");

	protected ViewGroup makeViewFromNoteEntry(NoteEntry ne) {
		ViewGroup notesListItems = (ViewGroup) inflator.inflate(R.layout.note_list_item, null);
		((TextView) notesListItems.findViewById(R.id.note)).setText(ne.note);
		((TextView) notesListItems.findViewById(R.id.date)).setText(ne.date);
		
		if (getSharedPreferences().getBoolean("callPhoneInNotes", false)) {
			Matcher matcher = phoneNumberPattern.matcher(ne.note);
			if (matcher.find()) {
				String phoneNumberAreaCode = matcher.group(1);
				String phoneNumberPrefix = matcher.group(2);
				String phoneNumberNumber = matcher.group(3);
				final String phoneNumber = phoneNumberAreaCode + phoneNumberPrefix + phoneNumberNumber;

				phoneNumbersThisOrder.add(phoneNumber);

			
			} 
		}
		return notesListItems;
	}

	static final int DIALOG_NEW_RUN_OR_BACK = 1;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		String perRunPay = getSharedPreferences().getString("per_run_pay", "0");
		float prp = 0;
		try {
			prp = Utils.parseCurrency(perRunPay);
		} catch (NumberFormatException e) {
		}

		if (prp > 0) {
			if (orderCounter == 0) {
				setResult(500);// Exit outTheDoor and start new Run
				finish();
			} else {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					showDialog(DIALOG_NEW_RUN_OR_BACK);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {

		case DIALOG_NEW_RUN_OR_BACK:
			AlertDialog b = new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(R.string.StartRunDialogTitle).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					setResult(500);// Exit outTheDoor and start new Run
					finish();
				}
			}).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int whichButton) {
					setResult(501);// Exit outTheDoor and don't start new Run
					finish();
				}
			}).setMessage(R.string.StartRunDialogMessage).create();
			b.setCancelable(false);
			return b;
		}
		return null;
	}

	/* Creates the menu items
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, EDIT_ORDER, 0, R.string.EDITORDER).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, PREVIOUS_ORDER, 0, R.string.PreviousDelivery).setIcon(android.R.drawable.ic_menu_revert);

		return true;

	} */
/*
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (sharedPreferences.getString("storePhoneNumber", "").length() > 0) {
			if (menu.findItem(CALL_STORE) == null) {
				menu.add(0, CALL_STORE, 0, getString(R.string.call_store)).setIcon(android.R.drawable.ic_menu_call);
			}
		} else {
			menu.removeItem(CALL_STORE);
		}
		//

		 * if (sharedPreferences.getBoolean("callPhoneInNotes", false)){ if
		 * (menu.findItem(CALL_NOTES_NUM)==null){ //orders.get(orderCounter).
		 * //String s =
		 * dataBase.getPastNotesForAddressAndAptNo(orders.get(orderCounter
		 * ).address,orders.get(orderCounter).apartmentNumber); String s =
		 * dataBase
		 * .getPhoneNumberFromNotes(orders.get(orderCounter).address,orders
		 * .get(orderCounter).apartmentNumber);
		 * 
		 * String phoneNumToCall = "";
		 * 
		 * menu.add(0, CALL_NOTES_NUM, 0,
		 * getString(R.string.Call)+" "+phoneNumToCall
		 * ).setIcon(android.R.drawable.ic_menu_call); } } else {
		 * menu.removeItem(CALL_NOTES_NUM); }

		return true;
	} */

	/* Handles item selections
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_ORDER: {
			final Context context = getApplicationContext();
			final Intent myIntent = new Intent(context, SummaryActivity.class);
			myIntent.putExtra("DB Key", orders.get(orderCounter).primaryKey);
			myIntent.putExtra("openEdit", true);
			startActivityForResult(myIntent, 0);

			return true;
		}
		case PREVIOUS_ORDER: {
			if (orderCounter != 0) {
				try {
					orders.get(orderCounter).payed = Float.parseFloat(paymentTotal.getText().toString());
				} catch (final NumberFormatException e) {
					orders.get(orderCounter).payed = 0;
				}
				orderCounter--;
				messageHandler.post(updateOrderTimers);
				paymentTotal.setText("" + orders.get(orderCounter).payed);
				next.setText("Next");
				next.setVisibility(View.VISIBLE);
			}
			return true;
		}
		case CALL_STORE:
			String phoneNumber = sharedPreferences.getString("storePhoneNumber", "");
			if (phoneNumber.length() < 1) {
				Toast.makeText(getApplicationContext(), R.string.missing_phone_number, Toast.LENGTH_LONG).show();
			} else {
				String uri = "tel:" + phoneNumber;
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(uri));
				startActivity(intent);
			}
			break;
		}
		return false;
	}*/
}