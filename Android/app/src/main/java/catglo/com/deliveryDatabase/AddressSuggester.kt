package catglo.com.deliveryDatabase

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import catglo.com.GoogleAddressSuggester
import java.util.*
import java.util.regex.Pattern

class AddressSuggester(
    context: Context,
    val addressListListener: AddressResultListener? ) : GoogleAddressSuggester(context, object : AddressResultListener {


    //This is the callback from the google api if it gets called
    override fun commit( addresses: ArrayList<AddressInfo>, originalSearchString: String )
    {
        val db = DataBase(context)
        db.open()
        db.saveAddressInfoForString(addresses,originalSearchString)

        val streetNameAfterNumber =  Pattern.compile("^([0-9]+\\s{0,2})(\\w+)")
        var m = streetNameAfterNumber.matcher(originalSearchString)
        var list: ArrayList<AddressInfo>? = null
        if (m.find()) { //If the address starts with a number and has street letters


            val numberPart = m.group(1)
            val streetPart = m.group(2)
            //Filter out addresses that do not have the same numerical prefix
            list = ArrayList()
            m = initialNumbers.matcher(originalSearchString)
            if (m.find()) {
                val numberPartOfSearchString = m.group(1)
                for (addressInfo in addresses) {
                    val address = addressInfo.address
                    if (address.startsWith(numberPartOfSearchString)) {
                        list.add(addressInfo)
                    }
                }
            }

        } else

        { //We never get here if its just a number so this is for text first search the notes and append to the google list
            list = ArrayList()
            for (address in addresses) {
                list.add(address)
            }
            val resultsFromDB = ArrayList<AddressInfo>()
            db.searchAddressSuggestionsFor(originalSearchString, resultsFromDB)
            for (ai in resultsFromDB) {
                list.add(ai)
            }
        }
        addressListListener?.commit(addresses, originalSearchString)
    }
}) {
    //Listener for results sent back to caller

    val taskHandler = Handler()


    private val commitLookup: AddressResultListener?
    var range = 0f
    private val streetList: StreetList
    var addressList: ArrayList<AddressInfo>? = null
    override fun lookup(addressSoFar: String) {


        if (useAlternateLocalLookup(addressSoFar)) {
            alternateLocalLookup(addressSoFar)
        } else {
            super.lookup(addressSoFar)
        }
    }

    public val dataBase = DataBase(context)


    override fun completeLookup(searchAddress: String, locationManager : LocationManager, location: Location) {
        //Called by lookup(addressSoFar) with gps coordinates of user searching

        //Try this to see if we can get the address from local history and return that if we need
        var localAddressList = dataBase.getAddressInfoForString(searchAddress);
        if (localAddressList.size > 0)
        {
            addressListListener?.commit(localAddressList, searchAddress);
        }
        else {
            //Wait 2 or 3 seconds before calling the google API.
            //We can call searchAddressSuggestionsFor and also do a text search on the geocode table right away and if we find stuff

            taskHandler.removeCallbacksAndMessages(null)
            taskHandler.postDelayed({
                super.completeLookup(searchAddress, locationManager, location)
            },2000)

        }

    }

    protected fun useAlternateLocalLookup(addressSoFar: String?): Boolean {
        val streetNumberOnly =
            Pattern.compile("^[0-9]+\\s{0,2}$")
        return streetNumberOnly.matcher(addressSoFar).find()
    }

    protected fun alternateLocalLookup(addressSoFar: String?) {
        val resultsFromDB = ArrayList<AddressInfo>()
        addressList = ArrayList()
        val streetNumberOnly = Pattern.compile("^[0-9]+$")
        if (streetNumberOnly.matcher(addressSoFar).find()) {
            dataBase.searchAddressSuggestionsFor(addressSoFar, resultsFromDB)
            for (ai in resultsFromDB) {
                addressList!!.add(ai)
            }
            commitLookup?.commit(addressList!!, addressSoFar!!)
        }

    }

    companion object {
        val initialNumbers = Pattern.compile("^([0-9]+)")
    }

    // public GoogleAddressSuggester(Tooled tooled, AddressResultListener addressListListener) {
    init {
        dataBase.open()
        streetList = StreetList.LoadState(context)
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        commitLookup = addressListListener
    }
}