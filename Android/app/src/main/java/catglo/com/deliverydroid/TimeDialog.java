package catglo.com.deliverydroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.MutableDateTime;

public class TimeDialog extends DialogFragment {
    public static final String DATE_TIME_ARGUMENT = "DateTime";
    private TimePicker timePicker;
    private int currentHour=0;
    private int previousHour=Integer.MAX_VALUE;

    int dayOffset = 0;

    private DateTime initialTime;

    public interface OnTimeChangedListener {
        void onTimeChanged(DateTime newTime, DialogInterface arg0);
    }
    private OnTimeChangedListener timeChangedListener;
    public void setOnTimeChangedListener(OnTimeChangedListener timeChangedListener){
        this.timeChangedListener = timeChangedListener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View dialogView = View.inflate(getActivity(), R.layout.time_pick_dialog, null);
        timePicker = (TimePicker)dialogView.findViewById(R.id.timePicker1);

        final Bundle args = getArguments();
        initialTime = (DateTime)args.getSerializable(DATE_TIME_ARGUMENT);
        if (initialTime!=null) {
            timePicker.setCurrentHour(initialTime.hourOfDay().get());
            timePicker.setCurrentMinute(initialTime.minuteOfHour().get());
        }
        final TextView timeLabel = (TextView)dialogView.findViewById(R.id.timeLabel);
        timeLabel.setText(Utils.getFormattedTimeDay(initialTime.toGregorianCalendar()));


        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                MutableDateTime newTime = new MutableDateTime(initialTime);
                currentHour = timePicker.getCurrentHour();
                newTime.setHourOfDay(currentHour);
                int currentMinute = timePicker.getCurrentMinute();
                newTime.setMinuteOfHour(currentMinute);

                Log.i("TIME","In time change listener"+timePicker.getCurrentMinute());

                if (currentHour<2 && previousHour > 22){
                    //next day
                    dayOffset++;
                    Log.i("TIME","Adding 24hours");
                }
                if (currentHour>22 && previousHour < 2){
                    //previous day
                    dayOffset--;
                    Log.i("TIME","Subtracting 24hours");
                }
                previousHour = currentHour;

                newTime.add(Hours.hours(24*dayOffset));

                timeLabel.setText(Utils.getFormattedTimeDay(newTime.toGregorianCalendar()));
            }
        });

        alertDialogBuilder.setView(dialogView);

        alertDialogBuilder.setNeutralButton(R.string.Now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DateTime now = DateTime.now();
                timePicker.setCurrentHour(now.hourOfDay().get());
                timePicker.setCurrentMinute(now.minuteOfHour().get());
                onDismiss(dialog);
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.Save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onDismiss(dialog);
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        super.onDismiss(dialogInterface);

        MutableDateTime newTime = new MutableDateTime(initialTime);

        newTime.add(Days.days(dayOffset));

        newTime.setHourOfDay(timePicker.getCurrentHour());
        newTime.setMinuteOfHour(timePicker.getCurrentMinute());

        if (timeChangedListener!=null) timeChangedListener.onTimeChanged(newTime.toDateTime(),dialogInterface);
    }
}
