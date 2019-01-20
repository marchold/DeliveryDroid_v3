package catglo.com.deliveryDatabase


import org.joda.time.MutableDateTime

class Shift {
    var startTime = MutableDateTime(MutableDateTime.now())
    var endTime = MutableDateTime(MutableDateTime.now())
    var odometerAtShiftStart: Int = 0
    var odometerAtShiftEnd: Int = 0
    var primaryKey: Int = 0
    var noEndTime = false
}
