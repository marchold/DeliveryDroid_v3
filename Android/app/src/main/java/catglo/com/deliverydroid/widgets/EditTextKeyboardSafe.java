package catglo.com.deliverydroid.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class EditTextKeyboardSafe extends EditText {
    public EditTextKeyboardSafe(Context context) {
        super(context);
        initClass();
    }

    public EditTextKeyboardSafe(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        initClass();
    }

    public EditTextKeyboardSafe(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClass();
    }

    private void initClass() {
        this.setFocusableInTouchMode(false);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusableInTouchMode(true);
                v.requestFocusFromTouch();
            }
        });
        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    v.setFocusableInTouchMode(false);
                }
            }
        });
    }

}