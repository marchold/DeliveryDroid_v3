//
//  Order.swift
//  DeliveryDroid
//
//  Created by Marc Kluver on 12/8/18.
//  Copyright © 2018 Marc Kluver. All rights reserved.
//

import Foundation

class Order {  // extends NotedObject implements Comparable<Order>, Serializable{
    
    //private static final long serialVersionUID = 1L;
    static let CASH      = 0
    static let CHECK     = 1
    static let CREDIT    = 2
    static let EBT       = 3
    static let DEBIT     = 4
    static let NOT_PAID  = -1
    
    var onHold : Bool = false
    
//    static Pattern longNumber = Pattern.compile("[0-9]{1,15}");
    func GetTimeFromString(_ s: String) -> Int64 {
        /*
        try {
        return GetTimeFromString(s, "yyyy-MM-dd H:mm:ss");
        } catch (ParseException e) {
        try {
        return GetTimeFromString(s, "yyyy-MM-dd");
        } catch (ParseException e1) {
        Matcher m = longNumber.matcher(s);
        if (m.find()){
        return Long.parseLong(s);
        } else {
        Log.e("PARSE","Failed to parse "+s);
        throw new IllegalStateException("Failed to parse "+s);
        }
        }
        }*/
        return 0
    }
    
    //@Override
    //var String toString(){
    //return address;
    //}
    
    func getMinutesAgo() -> Int {
        //return (int) ((System.currentTimeMillis() - time.getTime()) / 1000) / 60;
        return 0
    }
    
    func GetTimeFromString(_ s : String, _ format : String ) -> Int64 {
        /*if (s==nil) return 0;
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        long t;
        try {
        t = formatter.parse(s).getTime();
        } catch (nilPointerException e){
        Log.e("Time","bad time "+s);
        e.printStackTrace();
        t = System.currentTimeMillis();
        }
        return t;*/
           return 0
    }
    
    // Constructor for data from the sql database
    /*var Order(final Cursor c) {
    id = c.getLong(c.getColumnIndex("ID"));
    
    number = c.getString(c.getColumnIndex(DataBase.OrderNumber));
    cost = c.getFloat(c.getColumnIndex(DataBase.Cost));
    address = c.getString(c.getColumnIndex(DataBase.Address));
    phoneNumber = c.getString(c.getColumnIndex(DataBase.PhoneNumber));
    
    notes = c.getString(c.getColumnIndex(DataBase.Notes));
    String s = c.getString(c.getColumnIndex(DataBase.Time));
    time = new Timestamp(GetTimeFromString(s));
    
    payed = c.getFloat(c.getColumnIndex(DataBase.Payed));
    payed2 = c.getFloat(c.getColumnIndex(DataBase.PayedSplit));
    extraPay = c.getFloat(c.getColumnIndex(DataBase.ExtraPay));
    
    deliveryOrder = c.getFloat(c.getColumnIndex(DataBase.DeliveryOrder));
    primaryKey = c.getInt(c.getColumnIndex("ID"));
    paymentType = c.getInt(c.getColumnIndex(DataBase.PaymentType));
    paymentType2 = c.getInt(c.getColumnIndex(DataBase.PaymentType2));
    
    apartmentNumber = c.getString(c.getColumnIndex(DataBase.AptNumber));
    if (apartmentNumber==nil)
    apartmentNumber="";
    
    int bool = c.getInt(c.getColumnIndex(DataBase.OutOfTown));
    if (bool==0){
    outOfTown1=false;
    } else {
    outOfTown1=true;
    }
    
    bool = c.getInt(c.getColumnIndex(DataBase.OutOfTown2));
    if (bool==0){
    outOfTown2=false;
    } else {
    outOfTown2=true;
    }
    
    bool = c.getInt(c.getColumnIndex(DataBase.OutOfTown3));
    if (bool==0){
    outOfTown3=false;
    } else {
    outOfTown3=true;
    }
    
    bool = c.getInt(c.getColumnIndex(DataBase.OutOfTown4));
    if (bool==0){
    outOfTown4=false;
    } else {
    outOfTown4=true;
    }
    
    bool = c.getInt(c.getColumnIndex(DataBase.OnHold));
    if (bool==0){
    onHold=false;
    } else {
    onHold=true;
    }
    
    bool = c.getInt(c.getColumnIndex(DataBase.StartsNewRun));
    if (bool==0){
    startsNewRun=false;
    } else {
    startsNewRun=true;
    }
    
    try {
    bool = c.getInt(c.getColumnIndex("delivered"));
    
    if (bool == 0) {
    delivered = false;
    } else {
    delivered = true;
    }
    } catch (IllegalStateException e){
    e.printStackTrace();
    }
    
    
    try {
    bool = c.getInt(c.getColumnIndex("undeliverable"));
    if (bool==0){
    undeliverable=false;
    } else {
    undeliverable=true;
    }
    } catch (IllegalStateException e){
    e.printStackTrace();
    }
    
    int colindex = c.getColumnIndex(DataBase.geocodeFailed);
    if (colindex>0){
    bool = c.getInt(colindex);
    if (bool==0){
    geocodeFailed=false;
    } else {
    geocodeFailed=true;
    }
    } else {
    geocodeFailed=false;
    }
    
    colindex = c.getColumnIndex(DataBase.smsCoustomer);
    if (colindex>0){
    bool = c.getInt(colindex);
    if (bool==0){
    smsCoustomer=false;
    } else {
    smsCoustomer=true;
    }
    } else {
    smsCoustomer=false;
    }
    
    float lng = c.getFloat(c.getColumnIndex("GPSLng"));
    float lat = c.getFloat(c.getColumnIndex("GPSLat"));
    geoPoint =new MyGeoPoint((double)lat,(double)lng);
    
    bool = c.getInt(c.getColumnIndex("validatedAddress"));
    if (bool==0){
    isValidated=false;
    } else {
    isValidated=true;
    }
    
    Log.i("geo","reading gps coords = ("+lat+","+lng+") isValidated="+isValidated+" for address "+address);
    
    try {
    arivialTime = new Timestamp(GetTimeFromString(c.getString(c.getColumnIndex(DataBase.ArivalTime))));
    payedTime = new Timestamp(GetTimeFromString(c.getString(c.getColumnIndex(DataBase.PaymentTime))));
    } catch (final RuntimeException e) {
    arivialTime = new Timestamp(System.currentTimeMillis());
    payedTime = new Timestamp(System.currentTimeMillis());
    }
    
    bool = c.getInt(c.getColumnIndex("StreetHail"));
    if (bool==0){
    streetHail = false;
    } else {
    streetHail = true;
    }
    
    }*/
    
    init() {
        //time =  new Timestamp(System.currentTimeMillis());
    }
    
    //private final NumberFormat    format    = new DecimalFormat("00");
    
    func  getListText() -> String{
        /*int hours = time.getHours();
        String amPm;
        if (hours > 12) {
        amPm = new String("pm");
        hours -= 12;
        } else {
        amPm = new String("am");
        }
        return String.format("%d:%s%s\t\t$%.2f\n%s", hours, format.format(time.getMinutes()), amPm, cost, address);*/
        return ""
    }
    
    //@SuppressWarnings("deprecation")
    func getTimeAsFloat() -> Float {
        //float time = 0;
        //time = this.time.getHours() + this.time.getMinutes() / 100f;
        //return time;
           return 0
    }
    
    var id : Int64? = -1
    var number : String = ""
    var phoneNumber : String = ""
  //  var Timestamp    time;
    var cost : Float = 0
    var address : String = ""
    var apartmentNumber : String = ""
    
    var payed : Float = 0
    var payed2 : Float = 0
    var deliveryOrder : Float = 0
    var primaryKey : Int = 0
    var paymentType : Int = 0
    var paymentType2 : Int = 0
   // var Timestamp    arivialTime = new Timestamp(System.currentTimeMillis());
   // var Timestamp    payedTime = new Timestamp(System.currentTimeMillis());    //For taxi droid this is the time the coustomer got in the cab (or (commited to paying)
    var outOfTown1 : Bool = false
    var outOfTown2 : Bool = false
    var outOfTown3 : Bool = false
    var outOfTown4 : Bool = false
    
    var extraPay : Float = 0
    
    var startsNewRun : Bool = false
   // var MyGeoPoint   geoPoint;
    var isValidated : Bool = false
    
    var delivered : Bool = false
    var undeliverable : Bool = false
    
    var distance : String = "" //Not saved in db
    var travelTime : String = ""
    
    
    var hasHistory : Bool = false
    //var TipTotalData tipTotalsForThisAddress = new TipTotalData();
    
    
    //var ArrayList<DropOff> dropOffs = new ArrayList<DropOff>();
    
   // var Leg legOfRoute=nil;
    var hasBeenLookedUp : Bool = false
    var geocodeFailed : Bool = false
    var smsCoustomer : Bool = false
    
    
    func compareTo(_ another:Order ) {
        //Order other = (Order)another;
        //return (int) (other.deliveryOrder-deliveryOrder);
    }
    

    //var double getLat() {
    //return geoPoint.lat;
    //}
    
    //@Override
    //var double getLng() {
    //return geoPoint.lng;
    //}
    
    //@Override
    //var Date getTime() {
    //Date d = new Date();
    //d.setTime(time.getTime());
    //return d;
    //}
    
    
    
    func phoneNumbersOnly() -> String {
        return Order.phoneNumbersOnly(phoneNumber:phoneNumber);
    }
    
    static func phoneNumbersOnly(phoneNumber : String) -> String {
    /*
    
    StringBuilder numbers = new StringBuilder();
    for (char c : phoneNumber.toCharArray()){
    switch (c) {
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
    case '#':
    case '*':
    numbers.append(c);
    break;
    case 'a':
    case 'A':
    case 'b':
    case 'B':
    case 'c':
    case 'C':
    numbers.append('2');
    break;
    case 'd':
    case 'D':
    case 'e':
    case 'E':
    case 'f':
    case 'F':
    numbers.append('3');
    break;
    case 'g':
    case 'G':
    case 'h':
    case 'H':
    case 'i':
    case 'I':
    numbers.append('4');
    break;
    case 'j':
    case 'J':
    case 'k':
    case 'K':
    case 'l':
    case 'L':
    numbers.append('5');
    break;
    case 'm':
    case 'M':
    case 'n':
    case 'N':
    case 'o':
    case 'O':
    numbers.append('6');
    break;
    case 'p':
    case 'P':
    case 'q':
    case 'Q':
    case 'r':
    case 'R':
    case 's':
    case 'S':
    numbers.append('7');
    break;
    case 't':
    case 'T':
    case 'u':
    case 'U':
    case 'v':
    case 'V':
    numbers.append('8');
    break;
    case 'w':
    case 'W':
    case 'x':
    case 'X':
    case 'y':
    case 'Y':
    case 'z':
    case 'Z':
    numbers.append('9');
    break;
    default:
    }
    }
    return numbers.toString(); */
           return ""
    }
    
    func geocode() {
        /*Geocoder geocoder = new Geocoder(context);
        try {
        LocationManager lm = (LocationManager)context.getSystemService(Activity.LOCATION_SERVICE);
        if (lm != nil) {
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), false));
 
        if (location!=nil) {
        List<Address> geocoded = geocoder.getFromLocationName(address,
        1,
        location.getLatitude() - 1,
        location.getLongitude() - 1,
        location.getLatitude() + 1,
        location.getLongitude() + 1);
        if (geocoded.size() > 0) {
        Address result = geocoded.get(0);
        geoPoint = new MyGeoPoint(result.getLatitude(), result.getLongitude());
        isValidated = true;
 
        }
        }
        else {
        Toast.makeText(context,"No Location Error",Toast.LENGTH_SHORT).show();
        }
        }
        } catch (IOException e) {
        e.printStackTrace();
        }*/
    }
}
