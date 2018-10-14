package catglo.com.deliverydroid.neworder;

import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import catglo.com.widgets.ButtonPadFragment;
import catglo.com.widgets.DataAwareFragment;


/**
* Created by goblets on 2/23/14.
*/
public class OrderPriceButtonPadFragment extends ButtonPadFragment {
    @Override
    protected void onDataChangedHandler() {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        if ( activity.order.cost!=0) edit.setText( activity.tools.getFormattedCurrency( activity.order.cost));
        else edit.setText("");
    }

    ArrayAdapter<String> adapter;

    @Override
    public ListAdapter getListAdapter(){
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        adapter = activity.dataBase.getCostAdapter(getActivity());
        return  adapter;
    }

    @Override
    public void onTextChanged(String newText) {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        activity.order.cost =  activity.tools.parseCurrency(edit.getText().toString());
        adapter.getFilter().filter(newText);

        DataAwareFragment lastScreenFragment = (DataAwareFragment) activity.getFragment(NewOrderActivity.Pages.order);
        if (lastScreenFragment!=null) lastScreenFragment.onDataChanged();
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }
}
