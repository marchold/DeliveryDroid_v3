package catglo.com.deliverydroid.neworder;

import android.text.InputType;
import android.widget.ListAdapter;


/**
* Created by goblets on 2/23/14.
*/
public class OrderNumberButtonPadFragment extends ButtonPadFragment {
    @Override
    protected void onDataChangedHandler() {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        edit.setText( activity.order.number);
    }

    @Override
    public ListAdapter getListAdapter(){
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        if ( activity.sharedPreferences.getString("lastGeneratedOrderNumberString", "-1").equalsIgnoreCase("-1")==false){
            return  activity.dataBase.getOrderNumberAdapter(getActivity());
        }
        return null;
    }

    @Override
    public void onTextChanged(String newText) {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        activity.order.number = edit.getText().toString();
        DataAwareFragment lastScreenFragment = (DataAwareFragment)  activity.getFragment(NewOrderActivity.Pages.order);
        if (lastScreenFragment!=null) lastScreenFragment.onDataChanged();
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}
