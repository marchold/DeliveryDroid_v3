package catglo.com.deliverydroid.shift;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.TimePicker;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Shift;

import catglo.com.deliveryDatabase.Wage;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Tools;
import org.joda.time.DateTime;

import java.text.DecimalFormat;

/**
 * Created by mkluver on 5/9/14.
 */
public class ShiftSetWageFragment extends DialogFragment {

    protected DataBase dataBase = null;
    private Shift shift;

    Runnable onCompletionListener=null;
    private EditText input;

    public void setOnCompletionListener(Runnable r){
        onCompletionListener = r;
    }

    @Override
    public void onResume() {
        super.onResume();
        dataBase = new DataBase(getActivity());
        dataBase.open();
        Bundle args = getArguments();
        shift = dataBase.getShift(args.getInt("shiftID"));
    }

    @Override
    public void onPause() {
        dataBase.close();
        super.onPause();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View dialogView = View.inflate(getActivity(),R.layout.shift_set_wage_dialog, null);
        input = (EditText)dialogView.findViewById(R.id.editText1);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String wageString = input.getText().toString();
                        if (wageString.length()>0){
                            float wage = Tools.parseCurrency(wageString);
                            DecimalFormat df = new DecimalFormat("#.##");

                            Wage currentWage = dataBase.currentWage();
                            if (df.format(wage).equalsIgnoreCase(df.format(currentWage.wage))==false){
                                dataBase.setWage(wage,shift, DateTime.now());
                            }

                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setTitle(R.string.Clock_out);
        builder.setMessage(R.string.Set_shift_ending_time);
        builder.setIcon(R.drawable.icon);
        return builder.create();

    }

}

/*extends ButtonPadFragment {
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
*/