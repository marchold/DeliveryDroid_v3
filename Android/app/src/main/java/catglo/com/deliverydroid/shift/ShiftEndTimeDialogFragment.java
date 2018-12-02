package catglo.com.deliverydroid.shift;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;


import android.widget.TimePicker;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.Shift;
import catglo.com.deliverydroid.R;


public class ShiftEndTimeDialogFragment extends DialogFragment {
	TimePicker timePicker;
	protected DataBase dataBase = null;
	private Shift shift;
	
	Runnable onCompletionListener=null;
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
    	
    	
    	View dialogView = View.inflate(getActivity(),R.layout.shift_end_dialog, null);
    	timePicker = (TimePicker)dialogView.findViewById(R.id.timePicker1);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	shift.endTime.setHourOfDay(timePicker.getCurrentHour());
                	shift.endTime.setMinuteOfHour(timePicker.getCurrentMinute());
                	dataBase.saveShift(shift);
                    dialog.dismiss();
                    if (onCompletionListener!=null)onCompletionListener.run();
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
