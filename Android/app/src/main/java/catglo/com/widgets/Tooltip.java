package catglo.com.widgets;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.PopupWindow;
import android.widget.TextView;

import catglo.com.deliverydroid.R;

public class Tooltip implements OnClickListener {
	protected final View anchor;
	private final PopupWindow window;
	private View root;
	private Drawable background = null;
	
	@Override
	public void onClick(View v) {
	        this.dismiss();
	}
	
	/**
	 * Create a BetterPopupWindow
	 * 
	 * @param anchor  the view that the BetterPopupWindow will be displaying 'from'
	 * 
	 */
	public Tooltip(View anchor,String message) {
	    this.anchor = anchor;
	    this.window = new PopupWindow(anchor.getContext());
	
		// when a touch even happens outside of the window
		// make the window go away
	    this.window.setTouchInterceptor(new OnTouchListener() {public boolean onTouch(View v, MotionEvent event) {
	    	Tooltip.this.window.dismiss();
	        return true;
	    }});
	   
	
	   	LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	   	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.simple_tooltip_arrow_top, null);
	   	((TextView)root.findViewById(R.id.textView1)).setText(message);
	   	this.setContentView(root);  
	   	anchor.setOnFocusChangeListener(new View.OnFocusChangeListener() {public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus && isShowen==false){
				show();
				isShowen=true;
			}	
		}});
	}
	boolean isShowen=false;
	
	/**
	 * In case there is stuff to do right before displaying.
	 */
	protected void onShow() {}
	
	private void preShow() {
	    if(this.root == null) {
	    	throw new IllegalStateException("setContentView was not called with a view to display.");
	    }
	    onShow();
	
	    if(this.background == null) {
	    	this.window.setBackgroundDrawable(new BitmapDrawable());
	    } else {
	        this.window.setBackgroundDrawable(this.background);
	    }
	
	    // if using PopupWindow#setBackgroundDrawable this is the only values of the width and height that make it work
	    // otherwise you need to set the background of the root viewgroup
	    // and set the popup window background to an empty BitmapDrawable
	    this.window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
	    this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	    this.window.setTouchable(true);
	    this.window.setFocusable(false);
	    this.window.setOutsideTouchable(true);
	    this.window.setContentView(this.root);
	}
	
	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
	}
	
	/**
	 * Sets the content view. Probably should be called from {@link onCreate}
	 * 
	 * @param root
	 *            the view the popup will display
	 */
	public void setContentView(View root) {
	    this.root = root;
	    this.window.setContentView(root);
	}
	
	/**
	 * Will inflate and set the view from a resource id
	 * 
	 * @param layoutResID
	 */
	public void setContentView(int layoutResID) {
	    LayoutInflater inflator = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    this.setContentView(inflator.inflate(layoutResID, null));
	}
	
	/**
	 * If you want to do anything when {@link dismiss} is called
	 * 
	 * @param listener
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
	    this.window.setOnDismissListener(listener);
	}
	
	/**
	 * Show the tooltip
	 * 
	 */
	public void show() {
		try {
	        this.preShow();
	        this.window.setAnimationStyle(R.style.Animations_PopDownMenu);
	        this.window.showAsDropDown(this.anchor, 0, 0);
		} catch (BadTokenException e){
			//Orientation change
		}
	}
	
	public void dismiss() {
		this.window.dismiss();
		anchor.setOnFocusChangeListener(null);
	}
}