/*
 * Copyright (C) 2011 Marc Holder Kluver - Catglo  -
 * 
 */

package catglo.com.deliverydroid.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import catglo.com.deliverydroid.R;


public class DateRangeSlider extends Dialog {
	protected List<ScrollLayout> mScrollerList2 = new ArrayList<ScrollLayout>();
	Calendar start;
	Calendar stop;
	
	public DateRangeSlider(Context context, Calendar start, Calendar stop) {
		super(context);
		this.start = start;
		this.stop = stop;
	
	}

	/**
	 * Create the year and the week and day of the week scrollers and feed them with their labelers
	 * and place them on the layout.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// this needs to be called before everything else to set up the main layout of the DateSlider  
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.date_range_dialog);
		setTitle(this.getContext().getString(R.string.selectDateRange));

		final DatePicker dp_start = (DatePicker) findViewById(R.id.datePicker2);
		dp_start.init(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH), null);
		
		final DatePicker dp_stop = (DatePicker) findViewById(R.id.datePicker1);
		dp_stop.init(stop.get(Calendar.YEAR), stop.get(Calendar.MONTH), stop.get(Calendar.DAY_OF_MONTH), null);
		
		Button ok = (Button) findViewById(R.id.setShiftTimesToOrderTimes);
		ok.setOnClickListener(new View.OnClickListener(){public void onClick(View v) {
			start.set(Calendar.YEAR,  dp_start.getYear());
			start.set(Calendar.MONTH, dp_start.getMonth());
			start.set(Calendar.DATE,  dp_start.getDayOfMonth());
			stop.set(Calendar.YEAR,  dp_stop.getYear());
			stop.set(Calendar.MONTH, dp_stop.getMonth());
			stop.set(Calendar.DATE,  dp_stop.getDayOfMonth());
			DateRangeSlider.this.dismiss();
		}});
	
	}

}
