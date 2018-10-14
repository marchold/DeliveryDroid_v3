package catglo.com.deliverydroid.settings;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SettingsLocationsOptionsList extends ListPreference {


    public SettingsLocationsOptionsList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsLocationsOptionsList(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	ListView view = new ListView(getContext());
        int currentLocalatyCount = sharedPreferences.getInt("currentLocalatyCount", 0);
        
        view.setAdapter(adapter());
        if (currentLocalatyCount==0){
        	setEntries(entries());
        	setEntryValues(entryValues());
        	
        	LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String bestProvider = locationManager.getBestProvider(criteria, false);
			Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);//TODO: DEAL with null (if not google api's)
			if (lastKnownLocation==null){
				//TODO:Display error-Missing Google API's OR Location Disabled
			} else {
				final Editor prefEditor = sharedPreferences.edit();
			/*	new WebServiceUpdateLocalePreferances(sharedPreferences,(float) lastKnownLocation.getLatitude(),(float) lastKnownLocation.getLongitude(),new WebServiceUpdateLocalePreferances.LocalityListener(){public void result(LocalityInfo info) {
					prefEditor.putString("addressFilterComponents", info.filter);
					prefEditor.commit();
					setEntries(entries());
		        	setEntryValues(entryValues());
				}}).lookup();*/
			}
        } else {
        	setEntries(entries());
        	setEntryValues(entryValues());
        }
        return view;
    }
  
    
    private ListAdapter adapter() {
        return new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	int currentLocalatyCount = sharedPreferences.getInt("currentLocalatyCount", 0);
        CharSequence[] retVal = new CharSequence[currentLocalatyCount];
        for (int i = 0; i < currentLocalatyCount; i++){
        	retVal[i] = sharedPreferences.getString("localatyName_"+i,"");
        }
        return retVal;
    }

    private CharSequence[] entryValues() {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	int currentLocalatyCount = sharedPreferences.getInt("currentLocalatyCount", 0);
        CharSequence[] retVal = new CharSequence[currentLocalatyCount];
        for (int i = 0; i < currentLocalatyCount; i++){
        	retVal[i] = sharedPreferences.getString("localatyQuery_"+i,"");
        }
        return retVal;
    }
}