package catglo.com.widgets;

import android.app.TimePickerDialog;
import android.content.Context;

public class DeliveryTimePickerDialog extends TimePickerDialog {

	public DeliveryTimePickerDialog(Context context, int theme,
			OnTimeSetListener callBack, int hourOfDay, int minute,
			boolean is24HourView) {
		super(context, theme, callBack, hourOfDay, minute, is24HourView);
		// TODO Auto-generated constructor stub
	}

}
