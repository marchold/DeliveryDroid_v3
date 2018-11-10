package catglo.com.deliveryDatabase;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.*;
import android.util.Log;
import catglo.com.deliverydroid.data.Leg;
import catglo.com.deliverydroid.data.MyGeoPoint;


import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order extends NotedObject implements Comparable<Order>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int	CASH	= 0;
	public static final int	CHECK	= 1;
	public static final int	CREDIT	= 2;
	public static final int	EBT	    = 3;
	public static final int	DEBIT	= 4;
	public static final int	NOT_PAID	= -1;
	
	public static final int PAYMENTSTATUS_NOTPAID  = -1; //For taxi droid the payment field is a status field
	public static final int PAYMENTSTATUS_PAID     =  0;
	public static final int PAYMENTSTATUS_NOSHOW   =  1;
	public static final int PAYMENTSTATUS_CANCELED =  2;
	
	public boolean	onHold;

	static Pattern longNumber = Pattern.compile("[0-9]{1,15}");
	public static long GetTimeFromString(final String s) {
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
		}
	}

    @Override
    public String toString(){
        return address;
    }
	
	public int getMinutesAgo(){
		return (int) ((System.currentTimeMillis() - time.getTime()) / 1000) / 60;
	}

	static long GetTimeFromString(final String s, final String format) throws ParseException {
		if (s==null) return 0;
		final SimpleDateFormat formatter = new SimpleDateFormat(format);
		long t;
		try {
			t = formatter.parse(s).getTime();
		} catch (NullPointerException e){
			Log.e("Time","bad time "+s);
			e.printStackTrace();
			t = System.currentTimeMillis();
		}
		return t;
	}

	// Constructor for data from the sql database
	public Order(final Cursor c) {
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
		if (apartmentNumber==null)
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
		
	}

	public Order() {
		time =  new Timestamp(System.currentTimeMillis());
	}

	private final NumberFormat	format	= new DecimalFormat("00");

	@SuppressWarnings("deprecation")
	public String getListText() {
		int hours = time.getHours();
		String amPm;
		if (hours > 12) {
			amPm = new String("pm");
			hours -= 12;
		} else {
			amPm = new String("am");
		}
		return String.format("%d:%s%s\t\t$%.2f\n%s", hours, format.format(time.getMinutes()), amPm, cost, address);
	}

	@SuppressWarnings("deprecation")
	public float getTimeAsFloat() {
		float time = 0;
		time = this.time.getHours() + this.time.getMinutes() / 100f;
		return time;
	}

    public long         id;
	public String		number;
	public String       phoneNumber;
	public Timestamp	time;
	public float		cost;
	public String		address;
	public String       apartmentNumber="";

	public float		payed;
	public float		payed2;
	public float		deliveryOrder;
	public int			primaryKey;
	public int			paymentType;
	public int			paymentType2;
	public Timestamp	arivialTime = new Timestamp(System.currentTimeMillis());
	public Timestamp	payedTime = new Timestamp(System.currentTimeMillis());    //For taxi droid this is the time the coustomer got in the cab (or commited to paying)
	public boolean      outOfTown1;
	public boolean      outOfTown2;
	public boolean      outOfTown3;
	public boolean      outOfTown4;
	
	public float        extraPay;
	
	public boolean		startsNewRun=false;
	public MyGeoPoint   geoPoint;
	public boolean		isValidated;

    public boolean		delivered;
    public boolean		undeliverable;

	public String 	distance; //Not saved in db
	public String	travelTime;
	
	public boolean streetHail;
	
	boolean hasHistory=false;
	public TipTotalData tipTotalsForThisAddress = new TipTotalData();

	
	public ArrayList<DropOff> dropOffs = new ArrayList<DropOff>();
	
	public Leg legOfRoute=null;
	public boolean hasBeenLookedUp=false;
	public boolean geocodeFailed;
	public boolean smsCoustomer;


	public int compareTo(Order another) {	
		Order other = (Order)another;
		return (int) (other.deliveryOrder-deliveryOrder);
	}

	@Override
	public double getLat() {
		return geoPoint.lat;
	}

	@Override
	public double getLng() {
		return geoPoint.lng;
	}

	@Override
	public Date getTime() {
		Date d = new Date();
		d.setTime(time.getTime());
		return d;
	}



    public String phoneNumbersOnly() {
        return phoneNumbersOnly(phoneNumber);
    }

    public static String phoneNumbersOnly(String phoneNumber){
        if (phoneNumber==null) return "";

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
        return numbers.toString();
    }

	public void geocode(Context context) {
        Geocoder geocoder = new Geocoder(context);
        try {
            LocationManager lm = (LocationManager)context.getSystemService(Activity.LOCATION_SERVICE);
            if (lm != null) {
                @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), false));


                List<Address> geocoded = geocoder.getFromLocationName(address,
                        1,
                        location.getLatitude() - 0.1,
                        location.getLongitude() - 0.1,
                        location.getLatitude() + 0.1,
                        location.getLongitude() + 0.1);
                if (geocoded.size() > 0) {
                    Address result = geocoded.get(0);
                    geoPoint = new MyGeoPoint(result.getLatitude(), result.getLongitude());
                    isValidated = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
