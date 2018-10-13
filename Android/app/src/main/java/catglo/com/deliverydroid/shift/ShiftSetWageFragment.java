package catglo.com.deliverydroid.shift;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.R;

import catglo.com.deliverydroid.Tools;
import catglo.com.deliverydroid.widgets.ButtonPadFragment;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by mkluver on 5/9/14.
 */
public class ShiftSetWageFragment extends ButtonPadFragment {
    @Override
    public ListAdapter getListAdapter() {
        ShiftSetWageActivity activity = (ShiftSetWageActivity)getActivity();
        if (activity==null || activity.isFinishing()) return null;

        ArrayList<String> wageHistory = activity.dataBase.wageHistory();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,wageHistory);
        return adapter;
    }

    @Override
    protected void onDataChangedHandler() {
        //Unused: this is for when a 2nd fragment can update the data
    }

    @Override
    public void onTextChanged(String newText) {
        //Unused: we don't filter the list
    }

    @Override
    public void onPause(){
        ShiftSetWageActivity activity = (ShiftSetWageActivity)getActivity();
        String wageString = edit.getEditableText().toString();
        if (wageString.length()>0){
            float wage = Tools.parseCurrency(wageString);
            DecimalFormat df = new DecimalFormat("#.##");

            if (activity.dataBase.isTodaysShift(activity.shift)){
                Wage currentWage = activity.dataBase.currentWage();
                if (df.format(wage).equalsIgnoreCase(df.format(currentWage.wage))==false){
                    activity.dataBase.setWage(wage,activity.shift, DateTime.now());
                }
            } else {
                throw new IllegalStateException("set wage for past shift not supported");
            }

        }
        super.onPause();
    }
}
