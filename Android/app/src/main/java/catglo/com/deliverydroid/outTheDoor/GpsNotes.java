package catglo.com.deliverydroid.outTheDoor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.GpsNote;
import catglo.com.deliveryDatabase.NotedObject;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.viewEditOrder.SummaryActivity;

import java.util.Calendar;

public class GpsNotes extends DeliveryDroidBaseActivity implements LocationListener {

	private ImageButton addButton;
	private ListView listView;
	private View helpView;
	private LayoutInflater inflator;
	private View helpIcon;
	private CheckBox includeOrdersCheckbox;
	private boolean loadOrders = false;
	private View backButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_notes_activity);
		getSupportActionBar().hide();

		loadOrders = sharedPreferences.getBoolean("loadOrders_gps_notes", false);
		
	    addButton = (ImageButton)findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			DialogFragment dialogFragment = new DialogFragment(){
				@Override   
				public Dialog onCreateDialog(Bundle savedInstanceState) {AlertDialog.Builder alert = new AlertDialog.Builder(GpsNotes.this);  
					View view = View.inflate(GpsNotes.this, R.layout.gps_notes_add_dialog, null);
					alert.setView(view);	        
					final EditText inputName = (EditText)view.findViewById(R.id.editText1);  
					final CheckBox notify = (CheckBox)view.findViewById(R.id.notification);
					final CheckBox alarm = (CheckBox)view.findViewById(R.id.alarm);
			        alert.setPositiveButton(R.string.Save, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int whichButton) {  
			        	GpsNote newNote = new GpsNote();
			        	newNote.notes = inputName.getText().toString();
			        	newNote.lat = latitude;
			        	newNote.lng = longitude;
			        	newNote.time = Calendar.getInstance();
			        	newNote.notification = notify.isChecked();
			        	newNote.alarm = alarm.isChecked();
			        	dataBase.saveGpsNote(newNote);  	
			        	updateList();
			        }}); 
			        alert.setNegativeButton(R.string.Discard, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int whichButton) {}}); 
			        return alert.create();
				}
			};
			dialogFragment.show(getFragmentManager(), "add_gps_note_dialog");
		}});
		
		listView = (ListView)findViewById(R.id.listView1);
		helpView = findViewById(R.id.SpeechBubbleHelp);
		helpIcon = findViewById(R.id.helpIcon);
		includeOrdersCheckbox = (CheckBox)findViewById(R.id.toggleOrdersButton);
		includeOrdersCheckbox.setChecked(loadOrders);
		
		
		backButton = findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			finish();
		}});
        
	}

	LocationManager locationManager;
	DataBase dataBase;
	@Override
	public void onResume(){
		super.onResume();
		dataBase = new DataBase(this);
		dataBase.open();	
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation!=null){
			latitude = lastKnownLocation.getLatitude();
			longitude = lastKnownLocation.getLongitude();
		} else {
			latitude=0;
			longitude=0;
		}
		
		updateList();	
	}
	
	@Override
	public void onPause(){
		super.onPause();
		locationManager.removeUpdates(this);
		dataBase.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.gps_notes, menu);
		return false;
	}

	double longitude=0; //TODO: initialize these with the  the last known location on startup
    double latitude=0;
   // ArrayList<NotedObject> notes;
	@Override
	public void onLocationChanged(Location location) {
		longitude = location.getLongitude();
		latitude = location.getLatitude(); 
		updateList();  
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	void updateList(){
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("loadOrders_gps_notes", loadOrders);
		editor.commit();
		if (loadOrders) {
  		    includeOrdersCheckbox.setTextColor(getResources().getColor(R.color.android_blue));
  		    
  	    } else {
  		    includeOrdersCheckbox.setTextColor(getResources().getColor(R.color.white));
  	    }

        final GpsNotesListAdapter adapter = new GpsNotesListAdapter(this);
        if (adapter.refresh(dataBase,latitude,longitude,loadOrders)==false){
	    	  listView.setVisibility(View.GONE);
	    	  helpView.setVisibility(View.VISIBLE);
	    	  helpIcon.setVisibility(View.VISIBLE);
	      } else {
	    	  listView.setVisibility(View.VISIBLE);
	    	  helpView.setVisibility(View.GONE);
	    	  helpIcon.setVisibility(View.GONE);
              listView.setAdapter(adapter);
	      }
	      listView.setOnItemClickListener(new OnItemClickListener(){public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	    	  NotedObject note = (NotedObject)adapter.getItem(arg2);
	    	  if (note instanceof GpsNote){
	    		  showOptionsDialog((GpsNote)note);
	    	  }
	    	  else {
	    		  startActivity(new Intent(getApplicationContext(), SummaryActivity.class).putExtra("DB Key", ((Order)note).primaryKey));
	    	  }
	      }});
	      includeOrdersCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    	  loadOrders = isChecked;
	    	  
	    	  updateList();
	      }});
	}
	
	

	protected void showOptionsDialog(final GpsNote gpsNote) {
		
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
		popupBuilder.setIcon(R.drawable.icon);
	
			popupBuilder.setItems(R.array.gps_notes_list_options, new DialogInterface.OnClickListener(){public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case 0: //Edit Note
				DialogFragment dialogFragment = new DialogFragment(){
					@Override   
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						AlertDialog.Builder alert = new AlertDialog.Builder(GpsNotes.this);  
						View view = View.inflate(GpsNotes.this, R.layout.gps_notes_add_dialog, null);
						alert.setView(view);	        
						final EditText inputName = (EditText)view.findViewById(R.id.editText1);  
						final CheckBox notify = (CheckBox)view.findViewById(R.id.notification);
						final CheckBox alarm = (CheckBox)view.findViewById(R.id.alarm);
				        inputName.setText(gpsNote.notes);
				        notify.setChecked(gpsNote.notification);
				        alarm.setChecked(gpsNote.alarm);
				        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
				        public void onClick(DialogInterface dialog, int whichButton) {  
				        	gpsNote.notes = inputName.getText().toString();		
				        	gpsNote.notification = notify.isChecked();
				        	gpsNote.alarm = alarm.isChecked();
				        	dataBase.saveGpsNote(gpsNote);
				        	updateList();
				        }}); 
				        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int whichButton) {
				        	
				        }}); 
				        return alert.create();	   
					}
				};
				dialogFragment.show(getFragmentManager(), "edit_gps_note");
		        
				break;
			case 1: //Delete Note
				AlertDialog.Builder delBuilder = new AlertDialog.Builder(GpsNotes.this);
				delBuilder.setIcon(R.drawable.icon);
				delBuilder.setIcon(R.drawable.icon).setTitle(R.string.Delete_this_note);
				delBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int whichButton) {
							dataBase.delete(gpsNote);
							updateList();
						}
					}).setNegativeButton(android.R.string.no, null);
					delBuilder.create().show();
				break;
			}
		}});
		popupBuilder.create().show();
	}

}
