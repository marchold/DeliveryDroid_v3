package catglo.com.deliverydroid.shift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import catglo.com.widgets.ButtonPadFragment;

/**
 * Created by goblets on 2/25/14.
 */
public class OdometerEntryFragment extends ButtonPadFragment {
    @Override
    public ListAdapter getListAdapter() {
        OdometerEntryActivity activity = (OdometerEntryActivity)getActivity();
        ArrayAdapter<String> adapter = activity.dataBase.getOdometerPredtion();
        return adapter;
    }

    @Override
    protected void onDataChangedHandler() {
        //For multi fragment layouts, we can ignore here
    }

    @Override
    public void onTextChanged(String newText) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                //Save and close
                OdometerEntryActivity activity = (OdometerEntryActivity)getActivity();

                String editValue = edit.getEditableText().toString();
                if (editValue.length()>0) {
                    int value = Integer.valueOf(editValue);
                    if (activity.isStartShift) {
                        activity.shift.odometerAtShiftStart = value;
                    } else {
                        activity.shift.odometerAtShiftEnd = value;
                    }
                    activity.dataBase.saveShift(activity.shift);
                }
                activity.finish();

            }
        });

        return view;
    }
}