package catglo.com.deliverydroid.widgets;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import catglo.com.deliverydroid.R;

import androidx.appcompat.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class TwoLinesListPreference extends ListPreference {
	
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence[] mEntriesSubtitles;
    private String mValue;
    private int mClickedDialogEntryIndex;




	public TwoLinesListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

        try {
            int[] attrsArray = new int[]{R.attr.sublist};
            TypedArray a = getContext().obtainStyledAttributes(attrs, attrsArray);
            try {
                mEntriesSubtitles = a.getTextArray(0);
            } finally {
                a.recycle();
            }
        } catch (NullPointerException e){

        }

	}



//	@Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
  //      super.onPrepareDialogBuilder(builder);
        
		mEntries = getEntries();
		mEntryValues = getEntryValues();
		mEntriesSubtitles = getEntriesSubtitles();
		mValue = getValue();
		mClickedDialogEntryIndex = getValueIndex();
        
        if (mEntries == null || mEntryValues == null || mEntriesSubtitles == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }
        
        
        
        String[] mEntriesString = new String[mEntries.length];//(String[]) mEntries;
        int i = 0;
        for (CharSequence cs : mEntries){
        	mEntriesString[i++] = ""+cs;
        }
        
        
        // adapter
        ListAdapter adapter = new ArrayAdapter<String>(
                getContext(), R.layout.two_lines_list_preference_row, mEntriesString) {
	               
	        ViewHolder holder;
	
	        class ViewHolder {
	                TextView title;
	                TextView subTitle;
	                ImageView selectedIndicator;
	        }
	        
	        public View getView(int position, View convertView, ViewGroup parent) {
	                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
	                if (convertView == null) {
	                        convertView = inflater.inflate(R.layout.two_lines_list_preference_row, null);
	
	                        holder = new ViewHolder();
	                        holder.title = (TextView) convertView.findViewById(R.id.custom_list_view_row_text_view);
	                        holder.subTitle = (TextView) convertView.findViewById(R.id.custom_list_view_row_subtext_view);
	                        holder.selectedIndicator = (ImageView) convertView.findViewById(R.id.custom_list_view_row_selected_indicator);
	                        
	                        convertView.setTag(holder);
	                } else {
	                        // view already defined, retrieve view holder
	                        holder = (ViewHolder) convertView.getTag();
	                }              
		               
	                holder.title.setText(mEntries[position]);
	                holder.subTitle.setText(mEntriesSubtitles[position]);
	                holder.selectedIndicator.setVisibility(position == mClickedDialogEntryIndex ? View.VISIBLE : View.GONE);
	
	                return convertView;
	        }
	    };
        
		/*builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mClickedDialogEntryIndex = which;

			//	TwoLinesListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		});
        */
        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }



   // @Override
    protected void onDialogClosed(boolean positiveResult) {
     //   super.onDialogClosed(positiveResult);
        
        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    /**
     * Returns the index of the given value (in the entry values array).
     * 
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

	public CharSequence[] getEntriesSubtitles() {
		return mEntriesSubtitles;
	}

	public void setEntriesSubtitles(CharSequence[] mEntriesSubtitles) {
		this.mEntriesSubtitles = mEntriesSubtitles;
	}
    
}