package catglo.com.deliverydroid.widgets;




import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import catglo.com.deliverydroid.R;

public class PopupTooltip extends RelativeLayout {

	private int anchorId;
	private View anchorView;

	public PopupTooltip(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.PopupTooltip, 0, 0);
            anchorId = a.getResourceId(R.styleable.PopupTooltip_edit_text_id,0);
            
            
           
		}
		init();
	}
	
	public PopupTooltip(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		//setVisibility(View.GONE);
	}
	
	@Override
	protected void onAttachedToWindow(){
		super.onAttachedToWindow();
		
		anchorView = ((ViewGroup)getParent()).findViewById(anchorId);
        anchorView.setOnFocusChangeListener(new OnFocusChangeListener(){public void onFocusChange(View v, boolean hasFocus) {
         	if (hasFocus){
         		PopupTooltip.this.setVisibility(View.VISIBLE);
         	} else {
         		PopupTooltip.this.setVisibility(View.GONE);
         	}
		}});
	}
}













