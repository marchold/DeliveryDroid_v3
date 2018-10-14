package catglo.com.deliverydroid.homeScreen;

import android.app.Activity;
import android.view.View.OnTouchListener;
import catglo.com.widgets.ActivityHostFragment;


public class HomeScreen_MapFragment extends ActivityHostFragment {
    
    public HomeScreen_MapFragment() {
		super();
	}


    
	@Override
    protected Class<? extends Activity> getActivityClass() {
        return HomeScreen_MapFragmentActivity.class;
    }
}
