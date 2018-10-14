package catglo.com.widgets;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

public abstract class ActivityHostFragment extends LocalActivityManagerFragment {
    
   // private OnTouchListener gestureListener;

	public ActivityHostFragment() {
 //   	this.gestureListener = gestureListener;
	}

	protected abstract Class<? extends Activity> getActivityClass();
    private final static String ACTIVITY_TAG = "hosted";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = new Intent(getActivity(), getActivityClass());
        
        @SuppressWarnings("deprecation")
		final Window w = getLocalActivityManager().startActivity(ACTIVITY_TAG, intent);
        final View wd = w != null ? w.getDecorView() : null;
       
        if (wd != null) {
            ViewParent parent = wd.getParent();
            if(parent != null) {
                ViewGroup v = (ViewGroup)parent;
                v.removeView(wd);
            }
            
            wd.setVisibility(View.VISIBLE);
            wd.setFocusableInTouchMode(true);
            if(wd instanceof ViewGroup) {
                ((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            }
        }
      //  if (gestureListener!=null) {
      //  	wd.setOnTouchListener(gestureListener);
      //  }
        return wd;
    }

    /**
     * For accessing public methods of the hosted activity
     */
    public Activity getHostedActivity() {
		return getLocalActivityManager().getActivity(ACTIVITY_TAG);
	}
}