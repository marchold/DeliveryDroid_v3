package catglo.com.deliverydroid.neworder

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListAdapter
import catglo.com.GoogleAddressSuggester.AddressResultListener
import catglo.com.deliveryDatabase.AddressInfo
import catglo.com.deliveryDatabase.AddressSuggester
import catglo.com.deliverydroid.R
import java.util.*
import java.util.regex.Pattern

/**
 * Created by goblets on 2/16/14.
 */
class AddressEntryFragment : ButtonPadFragment() {
    var addressSuggestior: AddressSuggester? = null
    var pattern: Pattern? = null
    private var inputStage = 0
    var addressList: ArrayList<AddressInfo>? = null
    var selectedPoint: AddressInfo? = null
    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) { //Super is going to set the text view but we need to also pull the GPS location
        super.onItemClick(parent, view, position, id)
        val address = addressList!![position]
        val activity = activity as NewOrderActivity?
        if (activity != null) {
            if (!activity.order.isValidated) {
                activity.order.geocode(activity)
            }
        }
    }

    override val listAdapter: ListAdapter?
        get() = null


//    override fun getListAdapter(): ListAdapter {
//        return null
//    }

    override fun onDataChangedHandler() {
        val activity = activity as NewOrderActivity?
        edit?.setText(activity!!.order.address)
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as NewOrderActivity?
        addressSuggestior =
            AddressSuggester(getContext(), activity!!.dataBase, object : AddressResultListener {
                override fun commit(
                    addressList: ArrayList<AddressInfo>,
                    searchString: String
                ) {
                    if (addressList == null || getActivity() == null || getActivity()!!.isFinishing == true
                    ) {
                        return
                    }
                    val streets = ArrayAdapter(
                        getActivity(),
                        R.layout.out_the_door_address_list_item,
                        addressList
                    )
                    if (streets.isEmpty == false) {
                        list?.adapter = streets
                        list?.visibility = View.VISIBLE
                        tooltipLayout?.visibility = View.GONE
                        this@AddressEntryFragment.addressList = addressList
                    }
                }
            })
        edit?.setText(activity.order.address)
        val addressStrings =
            ArrayList<String>()
        activity.dataBase.getAddressSuggestionsFor("", addressStrings)
        addressList = ArrayList()
        for (address in addressStrings) {
            addressList!!.add(AddressInfo(address, null))
        }
        val streets = ArrayAdapter(
            activity,
            R.layout.out_the_door_address_list_item,
            addressList
        )
        if (streets.isEmpty == false) {
            list?.adapter = streets
            list?.visibility = View.VISIBLE
            tooltipLayout?.visibility = View.GONE
        }
    }

    //override var inputType(): get() {
    //    return InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
    //}

    override val inputType: Int
        get() = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<ImageView>(R.id.poweredByGoogleImage)?.visibility = View.VISIBLE
        pattern = Pattern.compile("([0-9\\-\\#\\@\\*_]*\\s)(.*)")
        /* setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE){
                        onSpace();
                    }
                }
                return false;
            }}
        );*/
        abc?.visibility = View.VISIBLE
        space?.visibility = View.VISIBLE
        //  edit.clearFocus();
//  one.requestFocus();
//  list.requestFocus();
        return view
    }

    override fun onSpace() {
        when (inputStage) {
            0 -> list?.visibility = View.VISIBLE
            1 -> {
                list?.visibility = View.VISIBLE
                //text.setText("Address - Suffix");
                val sufixList = arrayOf(
                    "Apt. ", "Suite.", "Ave", "St", "Pl", "Dr",
                    "N", "S", "E", "W", "NW", "NE", "SW", "SE"
                )
                val adapter = ArrayAdapter(
                    context,
                    R.layout.out_the_door_address_list_item, sufixList
                )
                setAdapter(adapter)
                inputStage = 2
            }
        }
    }

    override fun onTextChanged(newText: String) {
        val activity = activity as NewOrderActivity?
        activity!!.order.address = edit?.text.toString()
        val fragment = activity.getFragment(NewOrderActivity.Pages.order)
        fragment?.onDataChanged()
        if (edit?.isFocused == true) {
            selectedPoint = null
        }
        if (addressSuggestior != null) addressSuggestior!!.lookup(newText)
    }
}