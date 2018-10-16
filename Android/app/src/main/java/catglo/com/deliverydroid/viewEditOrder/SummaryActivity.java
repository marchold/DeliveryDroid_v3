package catglo.com.deliverydroid.viewEditOrder;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.homeScreen.HomeScreenActivity;

public class SummaryActivity extends DeliveryDroidBaseActivity {

	private ViewGroup fragmentContainer;
	private Fragment fragment;
	private Button editButton;
	private boolean editMode=false;
	private int key;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary_activity);
		getSupportActionBar().hide();
		Intent intent = getIntent();
        key = intent.getIntExtra("DB Key", -1);
        final boolean openEdit = intent.getBooleanExtra("openEdit", false);
        
		
		editButton = (Button)findViewById(R.id.setShiftTimesToOrderTimes);
		editButton.setOnClickListener(new OnClickListener(){public void onClick(View v) {
			if (openEdit){
				finish();
			} else
			if (editMode){
				//Switch back to view mode
				showViewFragment();
				editMode=false;
				editButton.setText(R.string.Edit);
			} else {
				//Switch to edit mode
				showEditFragment();
				editMode=true;
				editButton.setText(R.string.Done);
			}
		}});
		
		
        fragmentContainer = (ViewGroup)findViewById(R.id.fragmentContainer);
        
      
        if (openEdit){
        	showEditFragment();
        	editMode=true;
			editButton.setText(R.string.Done);
        } else {
        	showViewFragment();
        }
        
        View backButton = findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener(){public void onClick(View arg0) {
			startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			finish();
		}});
	}

	private void showViewFragment(){   
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        fragment = new SummaryViewFragment();
        Bundle args = new Bundle();
        args.putInt("DB Key", key);
		fragment.setArguments(args);
		transaction.replace(R.id.fragmentContainer, fragment);
		transaction.commit();
	}
	
	private void showEditFragment(){
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
        fragment = new SummaryEditFragment();
        Bundle args = new Bundle();
        args.putInt("DB Key", key);
		fragment.setArguments(args);
		transaction.replace(R.id.fragmentContainer, fragment);
		transaction.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
