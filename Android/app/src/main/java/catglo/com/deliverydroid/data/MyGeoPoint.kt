package catglo.com.deliverydroid.data


import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class MyGeoPoint : Serializable {
    var lat: Double = 0.toDouble()
    var lng: Double = 0.toDouble()

    constructor(lat: Float, lng: Float) {
        this.lng = lng.toDouble()
        this.lat = lat.toDouble()
    }

    constructor(lat: Double, lng: Double) {
        this.lng = lng
        this.lat = lat
    }

    override fun toString(): String {
        return lat.toString()+","+lng.toString()
    }

}

