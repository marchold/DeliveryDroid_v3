package catglo.com.deliverydroid;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
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
		if (getActionBar()!=null) getActionBar().hide();
        dataBase = tools.create(this);
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
	
	 @Override
	  public void onStart() {
	    super.onStart();
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	  }
	  
	
    @Override
	protected void onResume() {
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
