package catglo.com.deliverydroid;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import catglo.com.deliveryDatabase.DataBase;


public class DeliveryDroidBaseActivity extends AppCompatActivity implements Tooled {

    @Override
    public Tools getTools() {
        return tools;
    }

    public interface OnKeyboardVisibilityListener {
        void onVisibilityChanged(boolean visible);
    }
    
    public Tools tools = new Tools();
    public DataBase dataBase = null;
    public SharedPreferences sharedPreferences;

    static boolean unlocked=false;
	public static boolean startNewRun=true;

		
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (getSupportActionBar()!=null) getSupportActionBar().hide();
        dataBase = tools.create(this);


    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

  
    }
	
	 @Override
	  public void onStart() {
	    super.onStart();


	 //   EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    
	   // EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
	  
	
    @Override
	protected void onResume() {
		//dataBase.open();
		super.onResume();
		
		BackupManager b = new BackupManager(this);
		b.dataChanged();
		
		
	}

	@Override
	protected void onPause() {
		//dataBase.close();
		super.onPause();
	}


   





}
