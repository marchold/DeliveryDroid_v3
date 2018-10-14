package catglo.com.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public MyScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public interface ScrollViewListener {
	    void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy);
	}
	
	private ScrollViewListener scrollViewListener = null;
	public void setScrollListener(ScrollViewListener scrollViewListener){
		this.scrollViewListener = scrollViewListener;
	}
	@Override
	protected void onScrollChanged (int l, int t, int oldl, int oldt){
		super.onScrollChanged(l, t, oldl, oldt);
		if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
	}
}
