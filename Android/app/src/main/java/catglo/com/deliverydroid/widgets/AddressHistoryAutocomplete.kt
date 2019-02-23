package catglo.com.deliverydroid.widgets

import java.util.ArrayList

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import android.widget.Filterable
import android.widget.ListAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import catglo.com.GoogleAddressSuggester
import catglo.com.deliveryDatabase.AddressHistorySuggester
import catglo.com.deliveryDatabase.AddressInfo


class AddressHistoryAutocomplete : AppCompatAutoCompleteTextView {

    private var textWatcher: TextWatcher? = null
    private var suggestor: AddressHistorySuggester? = null
    internal var context: Context? = null
    private var prefKey = ""
    var selectedAddress: AddressInfo? = null
        internal set


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.context = context
    }

    constructor(context: Context) : super(context) {
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet, prefKey: String) : super(context, attrs) {
        this.context = context
        this.prefKey = prefKey
    }

    override fun setOnItemClickListener(itemSelectedListener: AdapterView.OnItemClickListener) {
        super.setOnItemClickListener { arg0, arg1, arg2, arg3 ->
            itemSelectedListener.onItemClick(arg0, arg1, arg2, arg3)
            val streets = arg0.adapter as ArrayAdapter<AddressInfo>
            selectedAddress = streets.getItem(arg2)
        }
    }


    fun startSuggestor() {
        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 1) {
                    suggestor?.lookup("" + s)
                }
            }
        }

        //TODO: Address history suggester needs to return GPS coordinates as well as the address so we can save them in the settings and use them for geol
        context?.let {
            suggestor =
                AddressHistorySuggester(it, prefKey, object : GoogleAddressSuggester.AddressResultListener {
                    override fun commit(results: ArrayList<AddressInfo>, searchString: String) {
                        post {
                            val streets = ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line, results)
                            this@AddressHistoryAutocomplete.setAdapter(streets)
                            this@AddressHistoryAutocomplete.showDropDown()
                        }
                    }
                })
        }


        addTextChangedListener(textWatcher)
    }
}
