/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd  -  DateSlider
 * 
 * DateSlider which allows for an easy selection of a time if you only
 * want to offer certain minute intervals take a look at DateTimeSlider 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package catglo.com.widgets;

import java.util.Calendar;

import catglo.com.deliverydroid.R;
import org.joda.time.MutableDateTime;



import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class TimeSlider extends DateSlider {
	Context context;

	private TextView dateLabel;

	private TextView dateValue;
	String label;
	
	public TimeSlider(Context context, OnDateSetListener l, MutableDateTime calendar, String label) {
		super(context, l, calendar);
		this.context = context;
		this.label = label;
	}
	
	/**
	 * Create the hour and the minutescroller and feed them with their labelers
	 * and place them on the layout.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// this needs to be called before everything else to set up the main layout of the DateSlider  
		super.onCreate(savedInstanceState);		
		
		
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		dateLabel = new TextView(getContext());
		mLayout.addView(dateLabel,0,lp);
		dateLabel.setText(label);
		dateLabel.setTextSize(22);
		
		dateValue = new TextView(getContext());
		dateValue.setTextSize(22);
		mLayout.addView(dateValue,1,lp);
		
		TextView hourLabel = new TextView(getContext());
		hourLabel.setText(this.getContext().getString(R.string.hour));
		mLayout.addView(hourLabel, 2,lp);
		
		// create the hour scroller and assign its labeler and add it to the layout
		ScrollLayout mHourScroller = (ScrollLayout) inflater.inflate(R.layout.scroller, null);
		mHourScroller.setLabeler(hourLabeler, mTime.getMillis(),90,60);
		mLayout.addView(mHourScroller, 3,lp);
		mScrollerList.add(mHourScroller);
		
		TextView minuteLabel = new TextView(getContext());
		minuteLabel.setText(this.getContext().getString(R.string.minute));
		mLayout.addView(minuteLabel, 4,lp);

		// create the minute scroller and assign its labeler and add it to the layout
		ScrollLayout mMinuteScroller = (ScrollLayout) inflater.inflate(R.layout.scroller, null);
		mMinuteScroller.setLabeler(minuteLabeler, mTime.getMillis(),45,60);
		mLayout.addView(mMinuteScroller, 5,lp);
		mScrollerList.add(mMinuteScroller);
		
		// this method _has_ to be called to set the onScrollListeners for all the Scrollers
		// in the mScrollerList.
		setListeners();
	}
	
	// the labeler for the hour scroller
	protected Labeler hourLabeler = new Labeler() {

		@Override
		public TimeObject add(long time, int val) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time);
			c.add(Calendar.HOUR_OF_DAY, val);
			return timeObjectfromCalendar(c);
		}
		
		@Override
		protected TimeObject timeObjectfromCalendar(Calendar c) {
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			// get the first millisecond of that hour
			c.set(year, month, day, hour, 0, 0);
			c.set(Calendar.MILLISECOND, 0);
			long startTime = c.getTimeInMillis();
			// get the last millisecond of that hour
			c.set(year, month, day, hour, 59, 59);
			c.set(Calendar.MILLISECOND, 999);
			long endTime = c.getTimeInMillis();
			String label = ""+c.get(Calendar.HOUR);
			if (c.get(Calendar.AM_PM) == Calendar.AM){
				label +="am";
			} else {
				label +="pm";
			}
			if (label.compareTo("0am")==0){
				label = context.getString(R.string.midnight);
			}
			if (label.compareTo("0pm")==0){
				label = context.getString(R.string.noon);
			}
			return new TimeObject(label, startTime, endTime);
		}
		
	};
	
	// the labeler for the minute scroller
	protected Labeler minuteLabeler = new Labeler() {

		@Override
		public TimeObject add(long time, int val) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time);
			c.add(Calendar.MINUTE, val);
			return timeObjectfromCalendar(c);
		}
		
		@Override
		protected TimeObject timeObjectfromCalendar(Calendar c) {
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			// get the first millisecond of that minute
			c.set(year, month, day, hour, minute, 0);
			c.set(Calendar.MILLISECOND, 0);
			long startTime = c.getTimeInMillis();
			// get the last millisecond of that minute
			c.set(year, month, day, hour, minute, 59);
			c.set(Calendar.MILLISECOND, 999);
			long endTime = c.getTimeInMillis();
			return new TimeObject(String.valueOf(minute), startTime, endTime);
		}
		
	};
	
	/**
	 * define our own title of the dialog
	 */
	@Override
	protected void setTitle() {
		Calendar time = mTime.toGregorianCalendar();
		if (mTitleText != null) {
			mTitleText.setText(String.format("Selected Time: %tR %tA",time,time)); 
		}
		if (dateLabel != null){
			dateValue.setText(String.format("%tl:%tM %tp %ta %tb %te %tY",time,time,time,time,time,time,time));
		}
		
	}

}
