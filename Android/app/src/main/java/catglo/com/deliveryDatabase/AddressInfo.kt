package catglo.com.deliveryDatabase

import catglo.com.deliverydroid.data.MyGeoPoint

import java.io.Serializable


class AddressInfo(var address: String,
                  var location: MyGeoPoint? = null,
                  var placeId: String? = null,
                  var associatedOrder: Order? = null)
    : Serializable
{
    var phoneNumber: String = ""


    override fun toString(): String {
        return if (!address.isBlank()) address else phoneNumber
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}