package catglo.com.deliverydroid.widgets;

import android.content.Context;
import android.widget.ViewFlipper;

public class FixedViewFlipper extends ViewFlipper {

	
	public FixedViewFlipper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	

	protected void onDetachedFromWindow() {
	    try {
	        super.onDetachedFromWindow();
	    }
	    catch (IllegalArgumentException e) {
	        stopFlipping();
	    }
	}
}
