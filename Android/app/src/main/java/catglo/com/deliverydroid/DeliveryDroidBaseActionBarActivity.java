package catglo.com.deliverydroid;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import catglo.com.deliveryDatabase.DataBase;


/**
 * Created by goblets on 2/17/14.
 */
public class DeliveryDroidBaseActionBarActivity extends AppCompatActivity implements Tooled {

    @Override
    public Tools getTools() {
        return tools;
    }

    public Tools tools = new Tools();
    public DataBase dataBase = null;
    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = tools.create(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        //EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }


    @Override
    protected void onResume() {
          super.onResume();
        BackupManager b = new BackupManager(this);
        b.dataChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
