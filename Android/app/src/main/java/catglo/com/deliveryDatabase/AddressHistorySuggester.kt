package catglo.com.deliveryDatabase

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.regex.Pattern

import android.content.Context
import catglo.com.GoogleAddressSuggester


class AddressHistorySuggester(context: Context, private val prefKey: String, commit: AddressResultListener) :
    GoogleAddressSuggester(context, null) {
    internal var range: Float = 0.toFloat()
    private var recientStreetNames: ArrayList<AddressInfo>? = null
    var addressList: ArrayList<AddressInfo>? = null
    private var commitor: AddressSuggestionCommitor? = null

    internal val fileNamePrefix = "history____"

    internal var initialNumebrs = Pattern.compile("^([0-9]+)")


    init {

        val fis: FileInputStream
        try {
            fis = context.openFileInput(fileNamePrefix + prefKey)
            val `is` = ObjectInputStream(fis)
            recientStreetNames = `is`.readObject() as ArrayList<AddressInfo>
            `is`.close()
        } catch (e: Exception) {
            recientStreetNames = ArrayList()
        }

        this.resultListener = commit
    }

    interface AddressSuggestionCommitor {
        fun commit(addressList: ArrayList<AddressInfo>, searchString: String)
    }

    internal fun init() {
        commitor = object : AddressSuggestionCommitor {
            override fun commit(addresses: ArrayList<AddressInfo>, originalSearchString: String) {

                val list = ArrayList<AddressInfo>()

                val streetNameAfterNumber = Pattern.compile("^([0-9]+\\s{0,2})(\\w+)")

                val m = streetNameAfterNumber.matcher(originalSearchString)

                if (m.find()) {//If the address starts with a number and has street letters
                    for (address in addresses) {
                        list.add(address)
                    }
                } else {
                    try {
                        for (address in recientStreetNames!!) {
                            if (address.address.startsWith(originalSearchString)) {
                                list.add(address)
                            }
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }

                }

                this@AddressHistorySuggester.addressList = list
                resultListener?.commit(list,originalSearchString)
            }
        }
    }

    override fun lookup(addressSoFar: String) {
        if (commitor == null) {
            init()
        }

        super.lookup(addressSoFar)
    }

    fun saveResult(value: AddressInfo) {
        var exists = false
        for (s in recientStreetNames!!) {
            try {
                if (s.address.equals(value.address, ignoreCase = true)) {
                    exists = true
                    break
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

        }
        if (!exists) {
            recientStreetNames!!.add(value)
        }

        Collections.sort(recientStreetNames, Comparator { lhs, rhs ->
            try {
                return@Comparator lhs.address.compareTo(rhs.address)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                return@Comparator -1
            }
        })
        try {
            val fos = context.openFileOutput(fileNamePrefix + prefKey, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(recientStreetNames)
            os.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

}
