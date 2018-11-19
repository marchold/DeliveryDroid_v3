package catglo.com.deliverydroid.neworder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity;
import catglo.com.deliverydroid.DeliveryDroidBaseActivity;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.widgets.OnTextChangedListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by goblets on 2/16/14.
 */
public abstract class ButtonPadFragment extends DataAwareFragment implements OnTextChangedListener, AdapterView.OnItemClickListener {


    private static int VOICE_RECOGNITION_REQUEST_CODE = 100;
    private boolean dirty=false;
    private KeyListener keyListener;
    private int code;
    public View abc;
    public View space;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = (TextView)view.findViewById(android.R.id.text1);
        edit.setText(tv.getText());
        try {
            edit.setSelection(tv.getText().length());
        }
        catch (Exception e)
        {
            edit.setSelection(tv.getText().length() - 1);
        }
    }

    /* If the activity implements this interface it will get the callback
         *
         */
    public interface ButtonPadNextListener {
        public void onNextButtonPressed();
    }

    private SharedPreferences sharedPreferences;

    public EditText edit;
    public ListView list;
    protected Context context;

    protected TextView text;
    protected View seven;
    protected View		eight;
    protected View		nine;
    protected View		four;
    protected View		five;
    protected View		six;
    protected View		one;
    protected View		two;
    protected View		three;
    protected View		dot;
    protected View		zero;
    protected View		del;
    public View			next;
    public View		    speakButton;

    public RelativeLayout numbers;

    Runnable callback = null;
    public View customButton;
    protected RelativeLayout buttons;
    public ViewGroup tooltipLayout;
    public TextView tooltipText;

    public abstract ListAdapter getListAdapter();

    public void press(final int keyVal) {
        //edit.setKeyListener(keyListener);
        if (keyVal == -1) {
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PERIOD));
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PERIOD));
        }
        if (keyVal == -2) {
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }
        if (keyVal == -3) {
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));
        } else {
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0 + keyVal));
            edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0 + keyVal));
        }
    }

    public void setAdapter(final ArrayAdapter<String> adapter) {
        //TODO: Tooltip
        list.setVisibility(View.VISIBLE);
        tooltipLayout.setVisibility(View.GONE);
        if (adapter.isEmpty()==false){
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();



        DeliveryDroidBaseActionBarActivity activity = (DeliveryDroidBaseActionBarActivity)getActivity();

        if (getListAdapter()!=null){
            list.setAdapter(getListAdapter());
        } else {
         //   list.setVisibility(View.GONE);
            //TODO: Show help for new users and focus it for no keyboard
        }
        list.setVisibility(View.VISIBLE);
      //  activity.tools.hideOnScreenKeyboard(edit);

/*
//TODO: Fix this so it works, The keyboard listener was firing when it should not
       activity.tools.setKeyboardListener(new DeliveryDroidBaseActivity.OnKeyboardVisibilityListener() {public void onVisibilityChanged(boolean visible) {
            if (visible) {
                buttons.setVisibility(View.GONE);
            }
            else {
                //No On Screen Keyboard
                buttons.setVisibility(View.VISIBLE);
            }}});*/


       edit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (edit.isFocusable() == false || edit.isFocusableInTouchMode() == false) {
                   edit.setInputType(getInputType());
                   edit.setFocusableInTouchMode(true);
                   edit.setFocusable(true);
                   edit.requestFocus();
                  // buttons.setVisibility(View.GONE);
                   DeliveryDroidBaseActionBarActivity activity = (DeliveryDroidBaseActionBarActivity) getActivity();
                   activity.tools.showOnScreenKeyboard(edit);
               }
           }
       });
    }

    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

    public void onPause(){
        super.onPause();
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data){
        if (   requestCode==Activity.RESULT_OK
            && data!=null
            && resultCode==code)
        {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Iterator<String> i = matches.iterator();
                if (i.hasNext()){
                    String s = i.next();
                    if (edit.getText().toString().length()>0){
                        s = edit.getText().toString()+" "+s;
                    }
                    edit.setText(s);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());



        int layoutResource = R.layout.button_pad;
        boolean left = sharedPreferences.getBoolean("number_keyboard_left", false);
        if (sharedPreferences.getBoolean("number_keyboard_upsidedown", false)){
            if (left){
                layoutResource = R.layout.button_pad_upsidedown_left;
            } else {
                layoutResource = R.layout.button_pad_upsidedown;
            }
        }
        else if (left){
            layoutResource = R.layout.button_pad_left;
        }


        numbers = (RelativeLayout) inflater.inflate(layoutResource,null);


        one = (View) numbers.findViewById(R.id.Button01);
        one.requestFocus();

        two = (View) numbers.findViewById(R.id.Button02);
        three = (View) numbers.findViewById(R.id.Button03);
        four = (View) numbers.findViewById(R.id.Button04);
        five = (View) numbers.findViewById(R.id.Button05);
        six = (View) numbers.findViewById(R.id.Button06);
        seven = (View) numbers.findViewById(R.id.Button07);
        eight = (View) numbers.findViewById(R.id.Button08);
        nine = (View) numbers.findViewById(R.id.Button09);

        dot = (View) numbers.findViewById(R.id.ButtonDot);
        zero = (View) numbers.findViewById(R.id.ButtonZero);
        del = (View) numbers.findViewById(R.id.ButtonDel);
        next = (View) numbers.findViewById(R.id.ButtonNext);

        edit = (EditText) numbers.findViewById(R.id.buttonPadEdit);
        edit.setTextColor(Color.WHITE);

        tooltipLayout = (ViewGroup) numbers.findViewById(R.id.tooltipLayout);
        tooltipText = (TextView) numbers.findViewById(R.id.tooltipText);

        list = (ListView) numbers.findViewById(R.id.buttonPadList);
        list.setFastScrollEnabled(true);

        speakButton = (View) numbers.findViewById(R.id.ButtonSpeech);
        customButton = (View) numbers.findViewById(R.id.setShiftTimesToOrderTimes);

        code = VOICE_RECOGNITION_REQUEST_CODE++;
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                 //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getActivity().getString(R.string.Speak_address));
                startActivityForResult(intent, code);

            }
        });


        zero.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(0);
            }
        });
        one.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(1);
            }
        });
        two.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(2);
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(3);
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(4);
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(5);
            }
        });
        six.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(6);
            }
        });
        seven.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(7);
            }
        });
        eight.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(8);
            }
        });
        nine.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(9);
            }
        });
        dot.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(-1);
            }
        });

        del.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(-2);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Activity activity = getActivity();
                if (activity instanceof ButtonPadNextListener){
                    ((ButtonPadNextListener)activity).onNextButtonPressed();
                }
            }
        });

        list.setCacheColorHint(0xEEEEEEFF);

        list.setOnItemClickListener(this);

        //Detect on screen keyboard show/hide
        buttons = (RelativeLayout)numbers.findViewById(R.id.buttonPadButtonLayout);


        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (settingInitialValues==false){
                        ButtonPadFragment.this.onTextChanged(s.toString());
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });

        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    next.performClick();
                    return true;
                }
                return false;
            }
        });

        space =  numbers.findViewById(R.id.ButtonSpace);
        space.setVisibility(View.VISIBLE);
        space.setBackgroundColor(0);
        space.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                press(-3);
                onSpace();
            }
        });


        abc =  numbers.findViewById(R.id.ButtonAbc);
        abc.setVisibility(View.VISIBLE);
        abc.setBackgroundColor(0);
        abc.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                edit.setFocusableInTouchMode(true);
                edit.setInputType(getInputType());
                edit.requestFocus();
                final InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                // only will trigger it if no physical keyboard is open
                mgr.showSoftInput(edit, InputMethodManager.SHOW_FORCED);// .SHOW_IMPLICIT)

            }
        });

        abc.setVisibility(View.INVISIBLE);
        space.setVisibility(View.INVISIBLE);

        return numbers;
    }

    protected void onSpace(){

    }
}
