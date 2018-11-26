package catglo.com.deliveryDatabase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import catglo.com.deliveryDatabase.TipTotalData.PayRatePieriod;
import catglo.com.deliverydroid.BuildConfig;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.Utils;
import catglo.com.deliverydroid.backup.TableValues;


import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataBase extends Object  {
    // public static final String KEY_ROWID = "_id";
    // public static final String KEY_ISBN = "isbn";
    // public static final String KEY_TITLE = "title";
    // public static final String KEY_PUBLISHER = "publisher";
    //private static final String			TAG					= "DataBase";

    public static final String			OrderNumber			= "OrderNumber";
    public static final String			Address				= "Address";
    public static final String			Cost				= "Cost";
    public static final String			Time				= "Time";
    public static final String			Notes				= "Notes";
    public static final String			Payed				= "Payed";
    public static final String			PayedSplit			= "PayedSplit";
    public static final String			DeliveryOrder		= "DeliveryOrder";
    public static final String			Shift				= "Shift";
    public static final String			PaymentType			= "PaymentType";
    public static final String			PaymentType2		= "PaymentType2";
    public static final String			ArivalTime			= "ArivialTime";
    public static final String			PaymentTime			= "PaymentTime";
    public static final String			RunNumber			= "RunNumber";
    public static final String			OutOfTown			= "OutOfTown";
    public static final String			OnHold   			= "OnHold";
    public static final String			OutOfTown2          = "OutOfTown2";
    public static final String			OutOfTown3          = "OutOfTown3";
    public static final String			OutOfTown4          = "OutOfTown4";
    public static final String			AptNumber           = "AptNumber";
    public static final String			geocodeFailed 		= "geocodeFailed";
    public static final String			smsCoustomer 		= "smsCoustomer";
    public static final String			manualGPS    		= "manualGPS";


    public static final String			ODO_START			= "OdomoterStart";
    public static final String			ODO_END				= "OdomoterEnd";
    public static final String			TIME_START			= "TimeStart";
    public static final String			TIME_END			= "TimeEnd";
    public static final String			StartsNewRun        = "StartsNewRun";
    public static final String			PAY_RATE            = "PAY_RATE";
    public static final String			PAY_RATE_ON_RUN     = "PAY_RATE_ON_RUN";

    //New for version 8 of the dataBase
    public static final String			OrderNotes			= "OrderNotes"; //Private notes vs. Notes which is for all addresses
    public static final String			ExtraPay			= "ExtraPay";   //Field for extra pay, like for sodas or weird mileage pay
    public static final String			PickUpAddress		= "PickUpAddress"; //Some users pick up orders at various stores
    public static final String			CuponDiscount		= "CuponDiscount";

    public static final String			PhoneNumber			= "PhoneNumber";

    public static final String			DATABASE_NAME		= BuildConfig.DATABASE_NAME;
    private static final String			DATABASE_TABLE		= "orders";
    private static final int			DATABASE_VERSION	= 11;


    public static int					TodaysShiftCount	= -1;


    void dataDidJustChange(){
        Intent i = new Intent("com.catglo.deliverydroid.DBCHANGED");
        context.sendBroadcast(i);
     /*
        Log.i("API","called dataDidJustChange");
        new WebServiceGetBackupTimestamp(context,new BackupTimeListener(){public void onRecievedBackupTime(DateTime lastBackupTime) {
        	synchronized(DataBase.class) {
        		boolean needsClosing=false;
	        	if (db == null || !db.isOpen()){
	        		open();
	        		
	        	}
	        	
	        	Log.i("API","got backup time "+lastBackupTime);
	        	
	        	DateTime curentDataTime = new DateTime(lastModified().getTimeInMillis());
	        	ArrayList<TableValues> tableFields = getBackupStrings(lastBackupTime.getMillis());
	        	
	        	Log.i("API","got "+tableFields.size()+" fields from the database ");
	        	
	        	new WebServicePostTableValues(context,tableFields).lookup();
	        	
	        	if (needsClosing){
	        		close();
	        	}
	        	
        	}
		}}).lookup(); */
    }

    static boolean justCreated=false;

    private static final String			DATABASE_CREATE		=
            "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ("
                    + "ID integer primary key autoincrement,"
                    + OrderNumber + "   VARCHAR, "
                    + Address     + "   VARCHAR, "
                    + Cost   + "        FLOAT, "
                    + Time + "          TIMESTAMP, "
                    + Notes+ "          VARCHAR, "
                    + Payed + "         FLOAT,"
                    + PayedSplit + "    FLOAT,"
                    + DeliveryOrder + " FLOAT,"
                    + Shift + "         INT,"
                    + PaymentType + "   INT,"
                    + PaymentType2 + "  INT,"
                    + ArivalTime + "    TIMESTAMP,"
                    + PaymentTime + "   TIMESTAMP,"
                    + RunNumber + "     INT,"
                    + OutOfTown +"      BOOLEAN,"
                    + StartsNewRun +"   BOOLEAN,"
                    + OutOfTown2+"      BOOLEAN,"
                    + OutOfTown3+"      BOOLEAN,"
                    + OutOfTown4+"      BOOLEAN,"
                    + AptNumber+"       VARCHAR,"
                    +"GPSLat            FLOAT, "
                    +"GPSLng            FLOAT, "
                    +"validatedAddress  BOOLEAN,"
                    +"geocodeFailed     BOOLEAN,"
                    +"manualGPS         BOOLEAN,"
                    +"smsCoustomer      BOOLEAN,"
                    +"StreetHail        BOOLEAN,"
                    + OrderNotes+ "     VARCHAR, "
                    + PickUpAddress+ "  VARCHAR, "
                    + PhoneNumber+   "  VARCHAR, "
                    + "delivered        BOOLEAN,"
                    + "undeliverable    BOOLEAN,"
                    + ExtraPay + "      FLOAT,"
                    + CuponDiscount + " FLOAT,"
                    +"lastModificationTime              TIMESTAMP, "
                    + OnHold +"         BOOLEAN);";

    private final Context				context;

    //	private final DatabaseHelper		databaseHelper;
    private SQLiteDatabase				db = null;

    private SharedPreferences	        prefs;

//	private static int					dataBaseInitLock	= 0;

    static File							path;

    public void init(File path){

        synchronized (DataBase.class)
        {
            try {
                db = SQLiteDatabase.openDatabase(path.toString(), null, SQLiteDatabase.OPEN_READWRITE);
                justCreated=false;
            } catch (final SQLiteException ex) {
                db = SQLiteDatabase.openDatabase(path.toString(), null, SQLiteDatabase.CREATE_IF_NECESSARY
                        | SQLiteDatabase.OPEN_READWRITE);
                onCreate(db);
            } finally {
                if (db != null) {
                    db.close();
                    db=null;
                }
            }
        }
    }


    public DataBase(final Context ctx) {synchronized (DataBase.class){
        this.context = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
            path = Environment.getExternalStorageDirectory();
        } else {
            path = context.getFilesDir();
        }

        path = new File(path, DATABASE_NAME);

        init(path);
    }}


    public DataBase(final Context context,String file){synchronized (DataBase.class){
        this.context=context;
        path = Environment.getExternalStorageDirectory();
        path = new File(path, file);
        init(path);
    }}

    public void onCreate(final SQLiteDatabase database) {synchronized (DataBase.class){
        database.execSQL(DATABASE_CREATE);
        database.execSQL("CREATE TABLE IF NOT EXISTS shifts (ID integer primary key autoincrement,"
                + ODO_START + "  INTEGER, "
                + ODO_END + "    INTEGER, "
                + PAY_RATE + "   FLOAT,"
                + PAY_RATE_ON_RUN+" FLOAT,"
                +"lastModificationTime              TIMESTAMP, "
                + TIME_START + " INTEGER, "
                + TIME_END + "   INTEGER);");

        database.execSQL("CREATE TABLE IF NOT EXISTS dropOffs (ID integer primary key autoincrement,"
                + "dropOffAddress	VARCHAR, "
                + "pickupId         INTEGER, "
                + "payment          FLOAT, "
                + "paymentType      INT,"
                + "meterAmount      FLOAT,"
                + "account          VARCHAR,"  //Account or credit card authorization
                + "authorization    VARCHAR,"
                +"lastModificationTime              TIMESTAMP, "
                + "dropOffTime      TIMESTAMP);");

        database.execSQL("CREATE TABLE IF NOT EXISTS streetNames (ID integer primary key autoincrement,"
                +"lastModificationTime              TIMESTAMP, "
                + "streetName		VARCHAR);");

        database.execSQL("CREATE TABLE IF NOT EXISTS expenses (ID integer primary key autoincrement,"
                + "description   	VARCHAR, "
                + "category         VARCHAR, "
                + "amount           FLOAT, "
                + "reimbursable     BOOLEAN,"
                + "reimbursed       BOOLEAN,"
                + "shiftId          INT,"
                +"lastModificationTime              TIMESTAMP, "
                + "expenseTime      TIMESTAMP);");

        database.execSQL("CREATE TABLE IF NOT EXISTS gps_notes (ID integer primary key autoincrement,"
                + "note        		VARCHAR, "
                + "GPSLat           FLOAT, "
                + "GPSLng           FLOAT, "
                + "orderId     		INT,"
                + "shiftId          INT,"
                + "notification     BOOLEAN,"
                + "alarm            BOOLEAN,"
                +"lastModificationTime              TIMESTAMP, "
                + "time             TIMESTAMP);");

        database.execSQL("CREATE TABLE IF NOT EXISTS hours_worked (ID integer primary key autoincrement,"
                + "start            TIMESTAMP, "
                + "end              TIMESTAMP, "
                + "rate             FLOAT, "
                +"lastModificationTime              TIMESTAMP, "
                + "shiftId          INT);");

        database.execSQL(createTimestampTriggerSql("orders","ID"));
        database.execSQL(createTimestampTriggerSql("shifts","ID"));
        database.execSQL(createTimestampTriggerSql("dropOffs","ID"));
        database.execSQL(createTimestampTriggerSql("streetNames","ID"));
        database.execSQL(createTimestampTriggerSql("expenses","ID"));
        database.execSQL(createTimestampTriggerSql("gps_notes","ID"));
        database.execSQL(createTimestampTriggerSql("hours_worked","ID"));

        justCreated=true;
        database.setVersion(DATABASE_VERSION);
    }}

    public void MergeDatabase(DataBase otherDatabase){ synchronized (DataBase.class){
        //Next import all the Shift records as is and record the 1st id number to offset the Orders shift ID
        String query = "SELECT * FROM shifts";
        Cursor c = otherDatabase.db.rawQuery(query, null);
        final int NOTHING=-1;
        int firstInsertedShift=NOTHING;
        int firstInsertedOrder=NOTHING;
        if (c != null && c.moveToFirst()) {
            BackupManager.dataChanged(context.getPackageName());
            do {
                final ContentValues init = new ContentValues();

                init.put(ODO_START,c.getInt(c.getColumnIndex(ODO_START)));
                init.put(ODO_END  ,c.getInt(c.getColumnIndex(ODO_END)));
                init.put(TIME_END ,c.getInt(c.getColumnIndex(TIME_END)));
                init.put(TIME_START,c.getInt(c.getColumnIndex(TIME_START)));
                if (c.getColumnIndex(PAY_RATE)!=-1) init.put(PAY_RATE ,c.getFloat(c.getColumnIndex(PAY_RATE)));
                if (c.getColumnIndex(PAY_RATE_ON_RUN)!=-1) init.put(PAY_RATE_ON_RUN, c.getFloat(c.getColumnIndex(PAY_RATE_ON_RUN)));

                int insertedKey = (int) db.insertOrThrow("shifts", null, init);
                if (firstInsertedShift==NOTHING){
                    firstInsertedShift=insertedKey;
                }

            } while (c.moveToNext());
        }
        c.close();
        if (firstInsertedShift==NOTHING){
            return; //TODO: Toast the user about the error
        }

        //Next import all the Orders offsetting the Shift id
        query = "SELECT * FROM "+DATABASE_TABLE;
        c = otherDatabase.db.rawQuery(query, null);

        if (c != null && c.moveToFirst()) {
            do {
                final ContentValues init = new ContentValues();
                init.put(OrderNumber,c.getString(c.getColumnIndex(OrderNumber)));
                init.put(Address,c.getString(c.getColumnIndex(Address)));
                init.put(Cost,c.getFloat(c.getColumnIndex(Cost)));
                init.put(Time,c.getString(c.getColumnIndex(Time)));
                init.put(Notes,c.getString(c.getColumnIndex(Notes)));
                init.put(Payed, c.getFloat(c.getColumnIndex(Payed)));
                if (c.getColumnIndex(PayedSplit)!=-1) init.put(PayedSplit, c.getFloat(c.getColumnIndex(PayedSplit)));
                init.put(DeliveryOrder, c.getFloat(c.getColumnIndex(DeliveryOrder)));
                int shift = c.getInt(c.getColumnIndex(Shift));
                shift+=firstInsertedShift;
                init.put(Shift, shift);
                init.put(PaymentType, c.getInt(c.getColumnIndex(PaymentType)));
                if (c.getColumnIndex(PaymentType2)!=-1) init.put(PaymentType2, c.getInt(c.getColumnIndex(PaymentType2)));
                init.put(ArivalTime,c.getString(c.getColumnIndex(ArivalTime)));
                init.put(PaymentTime,c.getString(c.getColumnIndex(PaymentTime)));
                if(c.getColumnIndex(RunNumber)!=-1) init.put(RunNumber,c.getString(c.getColumnIndex(RunNumber)));
                if(c.getColumnIndex(OutOfTown)!=-1) init.put(OutOfTown, c.getInt(c.getColumnIndex(OutOfTown)));
                if(c.getColumnIndex(StartsNewRun)!=-1) init.put(StartsNewRun, c.getInt(c.getColumnIndex(StartsNewRun)));
                if(c.getColumnIndex(OutOfTown2)!=-1) init.put(OutOfTown2, c.getInt(c.getColumnIndex(OutOfTown2)));
                if(c.getColumnIndex(OutOfTown3)!=-1) init.put(OutOfTown3, c.getInt(c.getColumnIndex(OutOfTown3)));
                if(c.getColumnIndex(OutOfTown4)!=-1) init.put(OutOfTown4, c.getInt(c.getColumnIndex(OutOfTown4)));
                if(c.getColumnIndex(AptNumber)!=-1) init.put(AptNumber,c.getString(c.getColumnIndex(AptNumber)));
                if(c.getColumnIndex("GPSLat")!=-1) init.put("GPSLat",c.getFloat(c.getColumnIndex("GPSLat")));
                if(c.getColumnIndex("GPSLat")!=-1) init.put("GPSLat",c.getFloat(c.getColumnIndex("GPSLat")));
                if(c.getColumnIndex("StreetHail")!=-1) init.put("StreetHail",c.getFloat(c.getColumnIndex("StreetHail")));
                if (c.getColumnIndex("validatedAddress")!=-1) init.put("validatedAddress",  c.getInt(c.getColumnIndex("validatedAddress")));
                if (c.getColumnIndex(OnHold)!=-1) init.put(OnHold,  c.getInt(c.getColumnIndex(OnHold)));

                //New for version 8 of the DB
                if (c.getColumnIndex(ExtraPay)!=-1) init.put(ExtraPay,  c.getFloat(c.getColumnIndex(ExtraPay)));
                if (c.getColumnIndex(OrderNotes)!=-1) init.put(OrderNotes,  c.getString(c.getColumnIndex(OrderNotes)));
                if (c.getColumnIndex(PickUpAddress)!=-1) init.put(PickUpAddress,  c.getString(c.getColumnIndex(PickUpAddress)));
                if (c.getColumnIndex(CuponDiscount)!=-1) init.put(CuponDiscount,  c.getString(c.getColumnIndex(CuponDiscount)));
                if (c.getColumnIndex(geocodeFailed)!=-1) init.put(geocodeFailed,  c.getInt(c.getColumnIndex(geocodeFailed)));
                if (c.getColumnIndex(smsCoustomer)!=-1) init.put(smsCoustomer,  c.getInt(c.getColumnIndex(smsCoustomer)));
                if (c.getColumnIndex(manualGPS)!=-1) init.put(manualGPS,  c.getInt(c.getColumnIndex(manualGPS)));



                int insertedKey = (int) db.insertOrThrow(DATABASE_TABLE, null, init);
                if (firstInsertedOrder==NOTHING){
                    firstInsertedOrder=insertedKey;
                }

            } while (c.moveToNext());
        }
        c.close();
    }}



    private static String[]	orderCosts		= null;
    //private static int		orderCostLock	= 0;
    private static String[]	orderNumbers	= null;
    public static String	orderNumberPrefix;

    private void generateOrderNumbers() {synchronized (DataBase.class){
        String nnn = prefs.getString("lastGeneratedOrderNumberString", "");
        int number=0;
        try {
            number= Integer.parseInt(nnn);
        } catch (NumberFormatException e){

        }

        orderNumbers = new String[50];
        synchronized (this) {
            for (int j = 0; j < 50; j++) {
                orderNumbers[j] = String.format("%d", number + j + 1);
            }
        }
    }}



    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {synchronized (DataBase.class){
        if (oldVersion<2 && oldVersion!=0){ //Version 3 is sometimes called version 0
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+PayedSplit+" FLOAT");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+PaymentType2+" INT");
        }
        if (oldVersion<3){
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ OutOfTown +"      BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ OnHold +"         BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ StartsNewRun +"   BOOLEAN");
        }
        if (oldVersion<4){
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ OutOfTown2+"      BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ OutOfTown3+"      BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ OutOfTown4+"      BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ AptNumber+"       VARCHAR");

            database.execSQL("ALTER TABLE shifts ADD "+ PAY_RATE + "   FLOAT");
            database.execSQL("ALTER TABLE shifts ADD "+ PAY_RATE_ON_RUN+" FLOAT");
        }
        if (oldVersion<5){
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD GPSLat            FLOAT");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " Add GPSLng            FLOAT");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD validatedAddress  BOOLEAN");
        }
        if (oldVersion<6){
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD StreetHail         BOOLEAN");
        }
        if (oldVersion<7){
            database.execSQL("CREATE TABLE IF NOT EXISTS expenses (ID integer primary key autoincrement,"
                    + "description   	VARCHAR, "
                    + "category         VARCHAR, "
                    + "amount           FLOAT, "
                    + "reimbursable     BOOLEAN,"
                    + "reimbursed       BOOLEAN,"
                    + "shiftId          INT,"
                    + "expenseTime      TIMESTAMP);");
        }
        if (oldVersion<8){
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ExtraPay+"      FLOAT");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " Add "+OrderNotes+"    VARCHAR");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " Add "+PickUpAddress+" VARCHAR");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+CuponDiscount+" FLOAT");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+geocodeFailed+" BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+smsCoustomer+"  BOOLEAN");
            database.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD " + manualGPS + " 	BOOLEAN");
            database.execSQL("CREATE TABLE IF NOT EXISTS gps_notes (ID integer primary key autoincrement,"
                    + "note        		VARCHAR, "
                    + "GPSLat           FLOAT, "
                    + "GPSLng           FLOAT, "
                    + "orderId     		INT,"
                    + "shiftId          INT,"
                    + "time             TIMESTAMP);");
        }

        if (oldVersion<9){
            //Go through all existing shifts and create a hours_worked record for each shift by copying the TIME_START and TIME_END and filling in the rate with the current pay rate in the settings
            database.execSQL("CREATE TABLE IF NOT EXISTS hours_worked (ID integer primary key autoincrement,"
                    + "start            TIMESTAMP, "
                    + "end              TIMESTAMP, "
                    + "rate             FLOAT, "
                    + "shiftId          INT);");
        }
        if (oldVersion<10){

            database.execSQL("ALTER TABLE  orders                 ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  shifts                 ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  dropOffs               ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  streetNames            ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  expenses               ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  gps_notes              ADD lastModificationTime TIMESTAMP");
            database.execSQL("ALTER TABLE  hours_worked           ADD lastModificationTime TIMESTAMP");

            database.execSQL(createTimestampTriggerSql("orders","ID"));
            database.execSQL(createTimestampTriggerSql("shifts","ID"));
            database.execSQL(createTimestampTriggerSql("dropOffs","ID"));
            database.execSQL(createTimestampTriggerSql("streetNames","ID"));
            database.execSQL(createTimestampTriggerSql("expenses","ID"));
            database.execSQL(createTimestampTriggerSql("gps_notes","ID"));
            database.execSQL(createTimestampTriggerSql("hours_worked","ID"));

            BackupManager.dataChanged(context.getPackageName());

        }
        if (oldVersion<11){
            database.execSQL("ALTER TABLE  orders          ADD  "+ PhoneNumber+"  VARCHAR");
            database.execSQL("ALTER TABLE  orders          ADD  delivered         BOOLEAN");
            database.execSQL("ALTER TABLE  orders          ADD  undeliverable     BOOLEAN");

            database.execSQL("ALTER TABLE  gps_notes       ADD  notification     BOOLEAN");
            database.execSQL("ALTER TABLE  gps_notes       ADD  alarm            BOOLEAN");

        }
        //TODO: these are for oldVersion < 11


        database.setVersion(newVersion);
    }}

    protected static String createTimestampTriggerSql(String table, String primaryKey){
        return ""
                +"CREATE TRIGGER IF NOT EXISTS add_date_for_"+table+" "
                +"    AFTER INSERT ON "+table+" "
                +"    BEGIN "
                +"      UPDATE "+table+" SET lastModificationTime = datetime('now') WHERE "+primaryKey+" = new."+primaryKey+"; "
                +"    END; ";
    }

    public void write(TableValues tableValues){
        for (ArrayList<String> values : tableValues.fieldValues){ //Loop through data rows
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < tableValues.fieldNames.size(); i++){
                String field =  tableValues.fieldNames.get(i);
                String value = values.get(i);
                contentValues.put(field, value);
            }
            db.insertWithOnConflict(tableValues.tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public Calendar lastModified() {
        Calendar calendar = null;
        Cursor c = db.rawQuery(""
                +"SELECT * "
                +"FROM sqlite_master "
                +"WHERE type='table' "
                , null);
        ArrayList<String> tables = new ArrayList<String>(c.getCount());
        if (c.moveToFirst()){

            do {
                String table = c.getString(c.getColumnIndex("name"));
                if (table.contains("android_")==false && table.contains("sqlite_")==false){
                    tables.add(table);
                }
            } while (c.moveToNext());
        }
        c.close();


        for (String table : tables){

            c = db.rawQuery(""
                            +"SELECT lastModificationTime FROM "+table+" ORDER BY lastModificationTime DESC LIMIT 1",
                    null);
            Pattern mysqlDateTimePattern = Pattern.compile("([0-9]{4})\\-([0-9]{2})\\-([0-9]{2})\\s([0-9]{2}):([0-9]{2}):([0-9]{2})");
            if (c.moveToFirst()){
                try {
                    String s =  c.getString(0);
                    //2013-09-07 19:06:15
                    calendar = Calendar.getInstance();
                    Matcher matcher = mysqlDateTimePattern.matcher(s);
                    if (matcher.find()){
                        int year   = Integer.parseInt(matcher.group(1));
                        int month  = Integer.parseInt(matcher.group(2))-1;
                        int day    = Integer.parseInt(matcher.group(3));
                        int hour   = Integer.parseInt(matcher.group(4));
                        int minute = Integer.parseInt(matcher.group(5));
                        int second = Integer.parseInt(matcher.group(6));
                        calendar.set(year,month,day,hour,minute,second);
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            c.close();
        }
        return calendar;
    }


    @SuppressLint("DefaultLocale")
    public String sqlDate(Calendar calendar){
        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1tM:%1$tS.%1$tL", calendar);
    }

    public ArrayList<TableValues> getBackupStrings(long stateModified) {

        Calendar modifiedTime = Calendar.getInstance();
        modifiedTime.setTimeInMillis(stateModified);

        //First get all the table names
        Cursor c = db.rawQuery(""
                +"SELECT * "
                +"FROM sqlite_master "
                +"WHERE type='table' "
                , null);
        ArrayList<String> tables = new ArrayList<String>(c.getCount());
        if (c.moveToFirst()){

            do {
                String table = c.getString(c.getColumnIndex("name"));
                if (table.contains("android_")==false && table.contains("sqlite_")==false){
                    tables.add(table);
                }
            } while (c.moveToNext());
        }
        c.close();

        ArrayList<TableValues> dataToBackup = new ArrayList<TableValues>();
        for (String table : tables){

            TableValues tableValues = new TableValues();
            String query;
            if (stateModified>0){
                query = "SELECT * FROM `"+table+"` WHERE lastModificationTime > '"+sqlDate(modifiedTime)+"' ";
            } else {
                query = "SELECT * FROM `"+table+"`";
            }
            Log.i("backup",query);
            c = db.rawQuery(query , null);

            tableValues.tableName = table;
            if (c.moveToFirst()){
                for (int i = 0; i < c.getColumnCount(); i++){
                    tableValues.fieldNames.add(c.getColumnName(i));
                }
                do {
                    ArrayList<String> row = new ArrayList<String>();
                    for (int i = 0; i < c.getColumnCount(); i++){
                        row.add(c.getString(i));
                    }
                    tableValues.fieldValues.add(row);

                } while (c.moveToNext());
            }
            c.close();

            if (tableValues.fieldValues.size()>0){
                dataToBackup.add(tableValues);
            }
        }

        return dataToBackup;
    }

    // ---opens the database---
    public DataBase open() throws SQLException {synchronized (DataBase.class){
        if (db==null){
            db = SQLiteDatabase.openDatabase(path.toString(), null, SQLiteDatabase.OPEN_READWRITE);
            if (db.getVersion() < DATABASE_VERSION) {
                onUpgrade(db, db.getVersion(), DATABASE_VERSION);
            }
            if (justCreated){
                TodaysShiftCount=1;
                return this;
            }
            if (TodaysShiftCount == -1) {
                getCurShift();
                if (TodaysShiftCount==0){
                    createShiftRecordIfNonExists();
                }
            }
        }
        return this;
    }}

    public synchronized ArrayAdapter<String> getOrderNumberAdapter(final Activity context) {synchronized (DataBase.class){
        if (orderNumbers == null)
            generateOrderNumbers();
        return new ArrayAdapter<String>(context, R.layout.simple_auto_resize_list_item, orderNumbers);
    }}


    synchronized String getOrderNumberPrefix() {synchronized (DataBase.class){
        if (orderNumbers[0] == null) return new String("");
        String prefix = new String(orderNumbers[0]);
        if (prefix.length() > 2) {
            prefix = prefix.substring(0, prefix.length() / 2);
        }
        return prefix;
    }}

    // ---closes the database---
    public void close() {synchronized (DataBase.class){
        if (db != null) {
            db.close();
            db=null;
        }
    }}

    public long add(Expense expense){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues initialValues = new ContentValues();
        initialValues.put("description",  expense.description);
        initialValues.put("category",     expense.category);
        initialValues.put("amount",       expense.amount);
        initialValues.put("reimbursable", expense.reimbursable);
        initialValues.put("reimbursed",   expense.reimbursed);
        initialValues.put("shiftId",      expense.shiftId);
        initialValues.put("expenseTime",  GetDateString(expense.expenseTime));
        final long addedRow = db.insertOrThrow("expenses", null, initialValues);
        dataDidJustChange();
        return addedRow;
    }}

    public boolean update(Expense expense){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());
        final ContentValues args = new ContentValues();
        args.put("description",  expense.description);
        args.put("category",     expense.category);
        args.put("amount",       expense.amount);
        args.put("reimbursable", expense.reimbursable);
        args.put("reimbursed",   expense.reimbursed);
        args.put("shiftId",      expense.shiftId);
        args.put("expenseTime",  GetDateString(expense.expenseTime));
        final boolean retVal = db.update("expenses", args, expense.ID + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;
    }}

    public boolean delete(Expense expense){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());
        boolean retVal = db.delete(DATABASE_TABLE, "ID" + "=" + expense.ID, null) > 0;
        dataDidJustChange();
        return retVal;
    }}



    public ArrayList<Expense> getShiftExpenses(int shiftId){synchronized (DataBase.class){
        String query = "SELECT * FROM expenses WHERE shiftId = "+shiftId+" ORDER BY expenseTime";
        Log.d("DRIVER",query);
        final Cursor c = db.rawQuery(query, null);
        ArrayList<Expense> returnList = new ArrayList<Expense>();
        if (c!=null && c.moveToFirst()) {
            do {
                Expense e = new Expense();
                e.description = c.getString(c.getColumnIndex("description"));
                e.category = c.getString(c.getColumnIndex("category"));
                e.amount = c.getFloat(c.getColumnIndex("amount"));

                int r = c.getInt(c.getColumnIndex("reimbursable"));
                if (r==0){
                    e.reimbursable = false;
                } else {
                    e.reimbursable = true;
                }

                r = c.getInt(c.getColumnIndex("reimbursed"));
                if (r==0){
                    e.reimbursed = false;
                } else {
                    e.reimbursed = true;
                }
                e.shiftId = c.getInt(c.getColumnIndex("shiftId"));
                e.expenseTime.setTimeInMillis(Order.GetTimeFromString(c.getString(c.getColumnIndex("expenseTime"))));
                returnList.add(e);
            } while (c.moveToNext());
        }
        c.close();
        return returnList;
    }}

    public ArrayList<String> getExpensCategories(){synchronized (DataBase.class){
        String query = "SELECT DISTINCT category FROM expenses ORDER BY category";
        Log.d("DRIVER",query);
        final Cursor c = db.rawQuery(query, null);
        ArrayList<String> returnList = new ArrayList<String>();
        if (c.moveToFirst()) {
            do {
                returnList.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return returnList;
    }}

    public float getTotalExpensesForShift(int shiftId){synchronized (DataBase.class){
        String query = "SELECT SUM(amount) FROM expenses WHERE shiftId = "+shiftId;
        final Cursor c = db.rawQuery(query, null);
        float retVal = 0f;
        if (c.moveToFirst()){
            retVal = c.getFloat(0);
        }
        c.close();
        return retVal;
    }}

    public long addDropoff(int pickupId, String address, Calendar time){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());
        final ContentValues initialValues = new ContentValues();
        initialValues.put("pickupId", 		pickupId);
        initialValues.put("dropOffAddress", address);
        initialValues.put("dropOffTime", 	GetDateString(time));
        final long addedRow = db.insertOrThrow("dropOffs", null, initialValues);
        dataDidJustChange();
        return addedRow;
    }}

    // Modifies the fields in the order
    public boolean editDropOff(int orderId, String address) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());
        final ContentValues args = new ContentValues();
        args.put("dropOffAddress", address);
        final boolean retVal = db.update("dropOffs", args, orderId + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;
    }}


    public boolean updateDropOff(int primaryKey, int paymentType, String meter,String payment, String extra, Order order) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());
        final ContentValues args = new ContentValues();
        float paymentValue=0f;
        float meterValue=0;
        try {
            paymentValue = Float.parseFloat(payment);
        } catch(NumberFormatException e){e.printStackTrace();};
        try {
            meterValue =  Float.parseFloat(meter);
        } catch(NumberFormatException e){e.printStackTrace();};

        args.put("meterAmount",meterValue);
        args.put("payment", paymentValue);
        args.put("paymentType", paymentType);
        args.put("account", extra);

        final boolean retVal = db.update("dropOffs", args, primaryKey + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;
    }}

    public boolean updateDropOff(DropOff dropOff, Order order) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        float meterValue=0;
        try {
            args.put("payment", Float.valueOf(dropOff.payment));
        } catch(NumberFormatException e){e.printStackTrace();};
        args.put("paymentType", dropOff.paymentType);
        try {
            meterValue =  Float.valueOf(dropOff.meterAmount);
        } catch(NumberFormatException e){e.printStackTrace();};

        args.put("account", dropOff.account);
        args.put("dropOffAddress", dropOff.address);
        args.put("meterAmount",meterValue);

        final boolean retVal = db.update("dropOffs", args, dropOff.id + "= ID", null) > 0;
        dataDidJustChange();

        return retVal;
    }}

    // Add a new delivery order to the database
    // - It is necessary to determine the sort order when we add it
    public long add(final Order order) {
        return add(order,TodaysShiftCount);
    }

    @SuppressLint("DefaultLocale")
    public long add(final Order order, int shiftId) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        float orderOfNextSmallest = -1;
        long timeOfNextSmallest = Long.MAX_VALUE;

        float orderOfNextBiggest = -1;
        long timeOfNextBiggest = Long.MIN_VALUE;

        float orderOfSmallest = -1;
        long timeOfSmallest = Long.MAX_VALUE;

        float orderOfBiggest = -1;
        long timeOfBiggest = Long.MIN_VALUE;

        float myListOrder = -1;

        //Loop through all the undelivered orders
        final Cursor c = db.query(DATABASE_TABLE, new String[] { Time, DeliveryOrder }, "payed='-1'", null, null, null, Time);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    final long t = Order.GetTimeFromString(c.getString(c.getColumnIndex(DataBase.Time)));
                    final float o = c.getFloat(c.getColumnIndex(DataBase.DeliveryOrder));

                    if (order.time.getTime() > t) // if the new order time is bigger than the order we are looking at
                    {
                        if (timeOfNextBiggest < t) { // if the last biggest is
                            // smaller than the time
                            // we are looking at
                            timeOfNextBiggest = t;
                            orderOfNextBiggest = o;
                        }
                    }

                    if (order.time.getTime() < t) {
                        if (timeOfNextSmallest > t) {
                            timeOfNextSmallest = t;
                            orderOfNextSmallest = o;
                        }
                    }

                    if (t < timeOfSmallest) {
                        timeOfSmallest = t;
                        orderOfSmallest = o;
                    }

                    if (t > timeOfBiggest) {
                        timeOfBiggest = t;
                        orderOfBiggest = o;
                    }

                } while (c.moveToNext());
            }
        }
        c.close();

        String pendingDeliveriesOrder = prefs.getString("pendingDeliveriesOrder", "0");

        // Here we are determining a value for the list order we start with 1000
        // and then add 1
        // each time, we use float so we can stick numbers in the middle.
        if (timeOfNextBiggest == Long.MIN_VALUE && timeOfNextSmallest == Long.MAX_VALUE) { // This is the first
            // one in the list
            myListOrder = 1000;
        } else {
            if (timeOfNextBiggest == Long.MIN_VALUE) {// This is the last one in the list
                myListOrder = orderOfBiggest + 1;
            } else if (timeOfNextSmallest == Long.MAX_VALUE) {// This is the first one in the list
                myListOrder = orderOfSmallest - 1;
            } else { // somewhere in the middle of the list
                myListOrder = (orderOfNextBiggest + orderOfNextSmallest) / 2;
            }
        }

        final ContentValues initialValues = new ContentValues();
        initialValues.put(OrderNumber, order.number);
        initialValues.put(Address, order.address);
        initialValues.put(PhoneNumber, order.phoneNumbersOnly());
        initialValues.put(AptNumber, order.apartmentNumber.toUpperCase());
        initialValues.put(Cost, order.cost);
        initialValues.put(Time, GetDateString(order.time));
        initialValues.put(Notes, order.notes);
        initialValues.put(DeliveryOrder, myListOrder);
        initialValues.put(Payed, order.payed);
        initialValues.put(ExtraPay, order.extraPay);

        initialValues.put(PayedSplit, order.payed2);

        initialValues.put(Shift, shiftId);
        initialValues.put(PaymentType, order.paymentType);
        initialValues.put(ArivalTime, GetDateString(order.arivialTime));
        initialValues.put(PaymentTime, GetDateString(order.payedTime));
        initialValues.put(RunNumber, 0); // TODO: We don't use run numbers yet

        initialValues.put(geocodeFailed, order.geocodeFailed);
        initialValues.put(smsCoustomer, order.smsCoustomer);
        initialValues.put("validatedAddress", order.isValidated);
        if (order.geoPoint!=null){
            Log.i("geo","Writing gps coords for "+order.address);
            initialValues.put("GPSLng", (float)order.geoPoint.lng);
            initialValues.put("GPSLat", (float)order.geoPoint.lat);
        } else {
            Log.i("geo","Not saving any gps coords for "+order.address);
        }
        Log.i("geo","Writing order order.isValidated= "+order.isValidated);

        initialValues.put(StartsNewRun, order.startsNewRun);

        if (order.outOfTown1){
            initialValues.put(OutOfTown, "1");
        } else{
            initialValues.put(OutOfTown, "0");
        }

        if (order.outOfTown2){
            initialValues.put(OutOfTown2, "1");
        } else{
            initialValues.put(OutOfTown2, "0");
        }

        if (order.outOfTown3){
            initialValues.put(OutOfTown3, "1");
        } else{
            initialValues.put(OutOfTown3, "0");
        }

        if (order.outOfTown4){
            initialValues.put(OutOfTown4, "1");
        } else{
            initialValues.put(OutOfTown4, "0");
        }

        if (order.onHold){
            initialValues.put(OnHold, "1");
        } else{
            initialValues.put(OnHold, "0");
        }

        if (order.startsNewRun){
            initialValues.put(StartsNewRun, "1");
        } else{
            initialValues.put(StartsNewRun, "0");
        }

        //   db.execSQL("ALTER TABLE  orders          ADD  delivered         BOOLEAN");
        //   db.execSQL("ALTER TABLE  orders          ADD  undeliverable     BOOLEAN");

        if (order.delivered){
            initialValues.put("delivered", "1");
        } else{
            initialValues.put("delivered", "0");
        }

        if (order.undeliverable){
            initialValues.put("undeliverable", "1");
        } else{
            initialValues.put("undeliverable", "0");
        }



        //final String[] o = { new String(order.number) };
        generateOrderNumbers();
        long addedRow;

        try {
            addedRow = db.insert(DATABASE_TABLE, null, initialValues);
        } catch (SQLiteException e){
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+ExtraPay+"      FLOAT");}catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " Add "+OrderNotes+"    VARCHAR"); }catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " Add "+PickUpAddress+" VARCHAR"); 	}catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+CuponDiscount+" FLOAT");}catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+geocodeFailed+" BOOLEAN");}catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+smsCoustomer+"  BOOLEAN");}catch (SQLiteException e2){};
            try {db.execSQL("ALTER TABLE  " + DATABASE_TABLE + " ADD "+manualGPS+" 	BOOLEAN");}catch (SQLiteException e2){};

            try {db.execSQL("CREATE TABLE IF NOT EXISTS gps_notes (ID integer primary key autoincrement,"
                    + "note        		VARCHAR, "
                    + "GPSLat           FLOAT, "
                    + "GPSLng           FLOAT, "
                    + "orderId     		INT,"
                    + "shiftId          INT,"
                    + "time             TIMESTAMP);");}catch (SQLiteException e2){};
            addedRow = db.insertOrThrow(DATABASE_TABLE, null, initialValues);
        }

        dataDidJustChange();

        return addedRow;
    }}

    // ---retrieves all the titles---
    public Cursor getUndeliveredOrders() {synchronized (DataBase.class){
        return db.query(DATABASE_TABLE, // table The table name to compile the
                // query against.
                null, // fields array or null for all
                // selection A filter declaring which rows to return, formatted as an SQL WHERE clause
                //(excluding the WHERE itself). Passing null will return all rows for the given table.
                Payed + "='-1' AND " + "Shift='" + TodaysShiftCount + "'",
                null, // selectionArgs You may include ?s in selection, which
                // will be replaced by the values from selectionArgs, in
                // order that they appear in the selection. The values
                // will be bound as Strings.
                null, // groupBy A filter declaring how to group rows, formatted
                // as an SQL GROUP BY clause (excluding the GROUP BY
                // itself). Passing null will cause the rows to not be
                // grouped.
                null, // having A filter declare which row groups to include in
                // the cursor, if row grouping is being used, formatted
                // as an SQL HAVING clause (excluding the HAVING
                // itself). Passing null will cause all row groups to be
                // included, and is required when row grouping is not
                // being used.
                DeliveryOrder + "+0 DESC"); // orderBy How to order the rows,
        // formatted as an SQL ORDER BY
        // clause (excluding the ORDER BY
        // itself). Passing null will use
        // the default sort order, which may
        // be unordered.

    }}

    public ArrayList<Order> getUndeliveredOrdersArray() {synchronized (DataBase.class){
        ArrayList<Order> orders = new ArrayList<Order>();
        Cursor data = getUndeliveredOrders();
        if (data != null) {
            if (data.moveToFirst()) {
                orders.add(new Order(data));
                while (data.moveToNext()) {
                    orders.add(new Order(data));
                }
            }
            data.close();
        }
        return orders;
    }}


    public int getUndeliveredOrderCount() {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE + " WHERE " + Payed +"='-1' AND Shift='"+TodaysShiftCount+"'", null);
        int retVal=0;
        if (c.moveToFirst()) {
            retVal = c.getInt(0);
        }
        c.close();
        return retVal;
    }}

    public int getThisShidtOrderCount() {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE + " WHERE Shift='"+TodaysShiftCount+"'", null);
        int retVal=0;
        if (c.moveToFirst()) {
            retVal = c.getInt(0);
        }
        c.close();
        return retVal;
    }}

    public ArrayList<Order> searchForOrders(String searchFor, String apartmentSearchString){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        searchFor = DatabaseUtils.sqlEscapeString("%"+searchFor+"%");

        Cursor c;
        if (apartmentSearchString!=null && apartmentSearchString.length()>0){
            String aptEscaped = DatabaseUtils.sqlEscapeString("%"+apartmentSearchString+"%");
            c = db.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE (address LIKE "+searchFor+" OR Notes LIKE "+searchFor+") AND `AptNumber` LIKE "+aptEscaped+" ORDER BY Time DESC LIMIT 80", null);
        }
        else {
            c = db.rawQuery("SELECT * FROM "+DATABASE_TABLE+" WHERE address LIKE "+searchFor+" OR Notes LIKE"+searchFor+" ORDER BY Time DESC LIMIT 80", null);
        }

        ArrayList<Order> orders = new ArrayList<Order>();
        Order o = null;
        if (c != null && c.moveToFirst()) {
            do {
                o = new Order(c);
                orders.add(o);
            } while (c.moveToNext());
        }
        c.close();
        return orders;
    }}

    // ---retrieves all the titles---
    public Cursor getShiftOrders(final int shift) {synchronized (DataBase.class){
        return db.query(DATABASE_TABLE, // table The table name to compile the
                // query against.
                null, // fields array or null for all
                "Shift='" + shift + "'", // selection A filter declaring which
                // rows to return, formatted as an
                // SQL WHERE clause (excluding the
                // WHERE itself). Passing null will
                // return all rows for the given
                // table.
                null, // selectionArgs You may include ?s in selection, which
                // will be replaced by the values from selectionArgs, in
                // order that they appear in the selection. The values
                // will be bound as Strings.
                null, // groupBy A filter declaring how to group rows, formatted
                // as an SQL GROUP BY clause (excluding the GROUP BY
                // itself). Passing null will cause the rows to not be
                // grouped.
                null, // having A filter declare which row groups to include in
                // the cursor, if row grouping is being used, formatted
                // as an SQL HAVING clause (excluding the HAVING
                // itself). Passing null will cause all row groups to be
                // included, and is required when row grouping is not
                // being used.
                OrderNumber + "+0 ASC"); // orderBy How to order the rows,
        // formatted as an SQL ORDER BY clause
        // (excluding the ORDER BY itself).
        // Passing null will use the default
        // sort order, which may be unordered.
    }}

    public ArrayList<Order> getShiftOrderArray(final int shift) {synchronized (DataBase.class){
        ArrayList<Order> orders = new ArrayList<Order>();
        Cursor c =  db.query(DATABASE_TABLE, // table The table name to compile the
                // query against.
                null, // fields array or null for all
                "Shift='" + shift + "'", // selection A filter declaring which
                // rows to return, formatted as an
                // SQL WHERE clause (excluding the
                // WHERE itself). Passing null will
                // return all rows for the given
                // table.
                null, // selectionArgs You may include ?s in selection, which
                // will be replaced by the values from selectionArgs, in
                // order that they appear in the selection. The values
                // will be bound as Strings.
                null, // groupBy A filter declaring how to group rows, formatted
                // as an SQL GROUP BY clause (excluding the GROUP BY
                // itself). Passing null will cause the rows to not be
                // grouped.
                null, // having A filter declare which row groups to include in
                // the cursor, if row grouping is being used, formatted
                // as an SQL HAVING clause (excluding the HAVING
                // itself). Passing null will cause all row groups to be
                // included, and is required when row grouping is not
                // being used.
                OrderNumber + "+0 ASC"); // orderBy How to order the rows,
        // formatted as an SQL ORDER BY clause
        // (excluding the ORDER BY itself).
        // Passing null will use the default
        // sort order, which may be unordered.

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    orders.add(new Order(c));
                } while (c.moveToNext());
            } else {
                //osDate.setText("Today");
            }
            c.close();
        }else {
            //osDate.setText("Today");
        }
        return orders;
    }}

    public Order getOrder(final int key) {synchronized (DataBase.class){
        Cursor c = db.query(DATABASE_TABLE, null, /* WHERE */"ID='" + key + "'", null, null, null, null);
        Order o = null;
        if (c != null && c.moveToFirst()) {
            o = new Order(c);
        }

        return o;
    }}

    //Taxi Droid Only
    public void loadOrderDropOffs(Order order){synchronized (DataBase.class){
        Cursor c = db.rawQuery("SELECT * FROM dropOffs WHERE pickupId = "+order.primaryKey, null);
        c.close();
        if (c!=null && c.moveToFirst()){
            do {
                DropOff d = new DropOff();
                d.id = c.getInt(c.getColumnIndex("ID"));
                d.pickupId = c.getInt(c.getColumnIndex("pickupId"));
                d.time.setTime(Order.GetTimeFromString( c.getString(c.getColumnIndex("dropOffTime"))));
                d.address = c.getString(c.getColumnIndex("dropOffAddress"));
                d.payment = c.getFloat(c.getColumnIndex("payment"));
                d.meterAmount = c.getFloat(c.getColumnIndex("meterAmount"));
                d.account = c.getString(c.getColumnIndex("account"));
                d.authorization = c.getString(c.getColumnIndex("authorization"));
                d.paymentType = c.getInt(c.getColumnIndex("paymentType"));
                order.dropOffs.add(d);
            } while (c.moveToNext());
        }
        c.close();
    }}

    public Order getOrder(String where){synchronized (DataBase.class){
        final Cursor c = db.query(DATABASE_TABLE, null, where, null, null, null, null);
        Order o = null;
        if (c != null && c.moveToFirst()) {
            o = new Order(c);
        }
        c.close();
        return o;
    }}

    public Shift getShift(int shiftID){synchronized (DataBase.class){
        Cursor c = db.rawQuery("SELECT * FROM shifts WHERE ID = "+shiftID, null);
        Shift shift = new Shift();
        if (c!=null){
            if (c.moveToFirst()) {
                String t1 = c.getString(c.getColumnIndex(TIME_START));
                String t2 = c.getString(c.getColumnIndex(TIME_END));

                Log.i("CURSOR","times ="+t1+",  "+t2);
                try {
                    shift.startTime.setMillis(Order.GetTimeFromString(t1));
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                try {
                    shift.endTime.setMillis(Order.GetTimeFromString(t2));
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                shift.odometerAtShiftStart  = c.getInt(c.getColumnIndex(ODO_START));
                shift.odometerAtShiftEnd  = c.getInt(c.getColumnIndex(ODO_END));
                //shift.payRate = c.getFloat(c.getColumnIndex(PAY_RATE));
                //shift.payRateOnRun = c.getFloat(c.getColumnIndex(PAY_RATE_ON_RUN));
                shift.primaryKey = c.getInt(c.getColumnIndex("ID"));

                if (shift.endTime.getMillis() < shift.startTime.getMillis()){
                    shift.endTime=shift.startTime;
                }
            }
            c.close();

        }
        return shift;
    }}

    public void saveShift(Shift shift){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put(TIME_START, GetDateString(shift.startTime));
        args.put(TIME_END  , GetDateString(shift.endTime));
        args.put(ODO_START , shift.odometerAtShiftStart);
        args.put(ODO_END   , shift.odometerAtShiftEnd);
        args.put(ODO_START  , shift.odometerAtShiftStart);
        //	args.put(PAY_RATE, shift.payRate);
//		args.put(PAY_RATE_ON_RUN, shift.payRateOnRun);
        db.update("shifts", args, shift.primaryKey + "= ID", null);

        //When we save a shift, if there is no pay rate, create a pay rate record with the current shift and the pay rate from the settings.


        dataDidJustChange();

    }}

    public float getTotalMoneyCollectedForShift(final int shift) {synchronized (DataBase.class){
        float retVal = -1;
        final Cursor c = db.rawQuery("SELECT SUM(" + Payed + ") FROM " + DATABASE_TABLE + " WHERE " + Payed
                + "!=-1 AND Shift='" + shift + "'", null);
        if (c.moveToFirst()) {
            retVal = c.getFloat(0);
        }
        c.close();
        return retVal;
    }}

    public float getTotalCostForShift(final int shift) {synchronized (DataBase.class){
        float retVal = -1;
        final String q = new String("SELECT SUM(" + Cost + ") FROM " + DATABASE_TABLE + " WHERE " + Payed
                + "!=-1 AND Shift='" + shift + "'");
        final Cursor c = db.rawQuery(q, null);
        if (c.moveToFirst()) {
            retVal = c.getFloat(0);
        }
        c.close();
        return retVal;
    }}

    public class ShiftCounts{
        public int prev;
        public int next;
        public int cur;
    }

    public ShiftCounts getShiftCounts(int shiftId) {synchronized (DataBase.class){
        ShiftCounts counts=new ShiftCounts();
        counts.prev=0;
        counts.cur=1;
        counts.next=0;
        Cursor c = db.rawQuery("SELECT count(*) FROM shifts WHERE ID < "+shiftId, null);
        if (c.moveToFirst()) {
            counts.prev = c.getInt(0);
            counts.cur = counts.prev+1;
        }
        c = db.rawQuery("SELECT count(*) FROM shifts WHERE ID > "+shiftId, null);
        int nextCount = 0;
        if (c.moveToFirst()) {
            nextCount = c.getInt(0);
        }
        if (nextCount>0){
            counts.next = counts.cur+1;
        }
        return counts;
    }}


    public TipTotalData getTipTotal_new(final Context context, String where,String shiftWhere){synchronized (DataBase.class){
        TipTotalData ret=new TipTotalData();

        Cursor c;
        String queryString = "SELECT COUNT(*),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where;
        if (db==null || queryString==null) return ret;

        c = db.rawQuery(queryString, null);
        ret.deliveries = -1;
        if (c.moveToFirst()) {
            ret.deliveries = c.getInt(0);
        }
        c.close();

        return ret;
    }}

    public TipTotalData getTipTotal(final Context context, String where,String shiftWhere){synchronized (DataBase.class){
        TipTotalData ret=new TipTotalData();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mileagePayForUndeliverable = prefs.getBoolean("mileagePayForUndeliverable",false);

        String mileageWhere = where;

        where += " AND (undeliverable!='1' OR undeliverable IS NULL) ";
        if (mileagePayForUndeliverable==true)
        {
            mileageWhere = where;
        }

        Log.i("CURSOR","TipTotalData called where "+where);

        Cursor c;
        String queryString = "SELECT COUNT(*),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where;
        if (db==null || queryString==null) return ret;

        c = db.rawQuery(queryString, null);

        ret.deliveries = -1;
        if (c.moveToFirst()) {
            ret.deliveries = c.getInt(0);
        }
        c.close();

        StringBuilder shiftIds = new StringBuilder();
        HashMap<Long,Long> shiftIdsHash = new HashMap<Long,Long>();
        String shiftIdDeliminator = "";
        try {
            c = db.rawQuery("SELECT *,strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where+" ORDER BY "+Time+" DESC", null);
            ret.cashTips = 0;
            ret.reportableTips = 0;

            ret.bestTip = 0;
            ret.worstTip = Float.MAX_VALUE;


            ret.averagePercentageTip = 0f;
            int index=0;
            if (c.moveToFirst()) {
                do {
                    index++;

                    int paymentType1 = c.getInt(c.getColumnIndex(PaymentType));
                    int paymentType2 = c.getInt(c.getColumnIndex(PaymentType2));
                    float cost = c.getFloat(c.getColumnIndex(Cost));
                    float payed1 = c.getFloat(c.getColumnIndex(Payed));
                    float payed2 = c.getFloat(c.getColumnIndex(PayedSplit));
                    float thisTip;
                    Timestamp orderTime = new Timestamp(Order.GetTimeFromString(c.getString(c.getColumnIndex(DataBase.Time))));

                    Long shiftId = c.getLong(c.getColumnIndex("Shift"));
                    if (shiftIdsHash.containsKey(shiftId)==false){
                        shiftIdsHash.put(shiftId, shiftId);
                        shiftIds.append(shiftIdDeliminator);
                        shiftIds.append(shiftId);
                        shiftIdDeliminator = ",";
                    }

                    if (payed2 != 0){ //Split orders
                        float tip = cost;
                        if (paymentType1 != Order.CASH || paymentType1 == Order.NOT_PAID){
                            tip -= payed1;
                        }
                        if (paymentType2 != Order.CASH){
                            tip -= payed2;
                        }
                        if (tip < 0) {
                            //then we have part of the tip as non cash
                            ret.reportableTips -= tip;
                            if (paymentType1 == Order.CASH || paymentType1 == Order.NOT_PAID){
                                ret.cashTips+=payed1;
                            }
                            if (paymentType2 == Order.CASH){
                                ret.cashTips+=payed2;
                            }
                        } else {
                            //none of the tip came from non-cash payment
                            if (paymentType1 == Order.CASH || paymentType1 == Order.NOT_PAID){
                                tip -= payed1;
                            }
                            if (paymentType2 == Order.CASH){
                                tip -= payed2;
                            }
                            if (tip < 0){
                                ret.cashTips -= tip;
                            }
                        }
                        thisTip = (payed1+payed2-cost);

                    }else { //single payment orders
                        if (paymentType1 == Order.CASH || paymentType1 == Order.NOT_PAID){
                            ret.cashTips += (payed1-cost);
                        } else {
                            ret.reportableTips += (payed1-cost);
                        }
                        thisTip = (payed1-cost);
                    }

                    if (thisTip > ret.bestTip) {
                        ret.bestTip = (thisTip);
                        ret.bestTipTime = orderTime;
                    }
                    if (thisTip < ret.worstTip) {
                        ret.worstTip = (thisTip);
                        ret.worstTipTime = orderTime;
                    }

                    //Calculate % tip
                    if (cost!=0) {
                        float precentageTip = thisTip/cost;
                        ret.averagePercentageTip += precentageTip;
                    }

                    if (index==1)
                        ret.lastTip = thisTip;

                } while (c.moveToNext());
            }
            c.close();
            if (Float.isNaN(ret.worstTip) || ret.worstTip == Float.MAX_VALUE){
                ret.worstTip = 0;
            }

            if (ret.deliveries==0){
                ret.averagePercentageTip=0;
            } else {
                ret.averagePercentageTip = ret.averagePercentageTip/ret.deliveries;
                ret.averagePercentageTip *= 100;
                ret.averagePercentageTip = (int)ret.averagePercentageTip;
            }
            Log.i("MARC","SELECT Sum(" + Cost + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where);
            c = db.rawQuery("SELECT Sum(" + Cost + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where, null);
            ret.cost = -1;
            if (c.moveToFirst()) {
                ret.cost = c.getFloat(0);
            }
            Log.i("MARC","COST = "+ret.cost);
            c.close();

            c = db.rawQuery("SELECT Sum(" + Payed + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where, null);
            ret.payed = -1;
            if (c.moveToFirst()) {
                ret.payed = c.getFloat(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + PayedSplit + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where, null);
            if (c.moveToFirst()) {
                ret.payed += c.getFloat(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + Payed + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " +PaymentType+" = "+Order.CASH+" AND "+where, null);
            ret.payedCash = 0;
            if (c.moveToFirst()) {
                ret.payedCash = c.getFloat(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + PayedSplit + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " +PaymentType2+" = "+Order.CASH+" AND "+where, null);
            if (c.moveToFirst()) {
                ret.payedCash += c.getFloat(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + OutOfTown + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+where, null);
            if (c.moveToFirst()) {
                ret.outOfTownOrders += c.getInt(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + OutOfTown2 + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+where, null);
            if (c.moveToFirst()) {
                ret.outOfTownOrders2 += c.getInt(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + OutOfTown3 + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+where, null);
            if (c.moveToFirst()) {
                ret.outOfTownOrders3 += c.getInt(0);
            }
            c.close();

            c = db.rawQuery("SELECT Sum(" + OutOfTown4 + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+where, null);
            if (c.moveToFirst()) {
                ret.outOfTownOrders4 += c.getInt(0);
            }
            c.close();



            c = db.rawQuery("SELECT Sum(" + StartsNewRun + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+where, null);
            if (c.moveToFirst()) {
                ret.runs += c.getInt(0);
            }
            c.close();




            ret.total = 0;

            final String MilagePayPerTrip = prefs.getString("per_delivery_pay", "0");
            final String MilagePayPercent = prefs.getString("percent_order_price", "0");
            final String MilagePayPerMile = prefs.getString("odometer_per_mile", "0");
            final String MilagePayPerOutOfTownDelivery = prefs.getString("per_out_of_town_delivery", "0");
            final String MilagePayPerOutOfTownDelivery2 = prefs.getString("per_out_of_town_delivery2", "0");
            final String MilagePayPerOutOfTownDelivery3 = prefs.getString("per_out_of_town_delivery3", "0");
            final String MilagePayPerOutOfTownDelivery4 = prefs.getString("per_out_of_town_delivery4", "0");
            final String MilagePayPerRun = prefs.getString("per_run_pay", "0");

            ret.mileageEarned=0;

            c = db.rawQuery("SELECT Sum(" + ExtraPay + "),strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE "+mileageWhere, null);
            if (c.moveToFirst()) {
                ret.extraPay += c.getInt(0);
                ret.mileageEarned += ret.extraPay;
            }
            c.close();

            //Calculate fixed mileage pay per trip
            try {
                float f = Float.parseFloat(MilagePayPerTrip);
                //if (f>0){
                f = f * (float) ret.deliveries;
                ret.mileageEarned += f;
                //}
            } catch (final NumberFormatException e) {}

            //Calculate mileage pay as % of order total
            try {
                float f = Float.parseFloat(MilagePayPercent);
                if (f>0){
                    f = (f/100)* (ret.cost);
                    ret.mileageEarned += f;
                }
            } catch (final NumberFormatException e) {}


            try {
                float outOfTowns = Float.parseFloat(MilagePayPerOutOfTownDelivery);
                float outOfTownMileage = ret.outOfTownOrders * outOfTowns;
                //if (outOfTownMileage>0){
                ret.mileageEarned += outOfTownMileage;
                //}
            } catch (final NumberFormatException e) {}

            try {
                float outOfTowns = Float.parseFloat(MilagePayPerOutOfTownDelivery2);
                float outOfTownMileage = ret.outOfTownOrders2 * outOfTowns;
                //if (outOfTownMileage>0){
                ret.mileageEarned += outOfTownMileage;
                //}
            } catch (final NumberFormatException e) {}

            try {
                float outOfTowns = Float.parseFloat(MilagePayPerOutOfTownDelivery3);
                float outOfTownMileage = ret.outOfTownOrders3 * outOfTowns;
                //if (outOfTownMileage>0){
                ret.mileageEarned += outOfTownMileage;
                //}
            } catch (final NumberFormatException e) {}

            try {
                float outOfTowns = Float.parseFloat(MilagePayPerOutOfTownDelivery4);
                float outOfTownMileage = ret.outOfTownOrders4 * outOfTowns;
                //if (outOfTownMileage>0){
                ret.mileageEarned += outOfTownMileage;
                //}
            } catch (final NumberFormatException e) {}

            try {
                float milagePayPerRun = Float.parseFloat(MilagePayPerRun);
                float runMileagePay = ret.runs * milagePayPerRun;
                //if (runMileagePay>0){
                ret.mileageEarned += runMileagePay;
                //}
            } catch (final NumberFormatException e) {}

            c = db.rawQuery("SELECT shift,strftime('%w',`"+ DataBase.Time + "`) AS `weekday` FROM " + DATABASE_TABLE + " WHERE " + where, null);
            ArrayList<Integer> list = new ArrayList<Integer>(100);
            if (c!=null) {
                if (c.moveToFirst()){
                    do {
                        Integer s = c.getInt(0);
                        if (!list.contains(s)){
                            list.add(s);
                        }
                    } while (c.moveToNext());
                }
            }
            c.close();

            long hoursInMills = 0;
            for (int i = 0; i < list.size(); i++){
                c = db.rawQuery("SELECT "+TIME_START+","+TIME_END+" FROM shifts WHERE id = "+list.get(i), null);
                if (c.moveToFirst()) {
                    do {

                        long timeStart = c.getLong(c.getColumnIndex(TIME_START));
                        long timeEnd;
                        try {
                            timeEnd = c.getLong(c.getColumnIndex(TIME_END));
                        } catch (IllegalStateException e){
                            timeEnd = timeStart;
                        }
                        if (timeEnd > timeStart) {
                            hoursInMills += timeEnd-timeStart;
                        }
                    }while (c.moveToNext());
                }
                c.close();
            }
            ret.hours = (float)hoursInMills/3600000.0f;
            float f;
            try {
                f = Float.parseFloat(MilagePayPerMile);
            } catch (final NumberFormatException e) {
                f = 0;
            }

            for (int i = 0; i < list.size(); i++){
                c = db.rawQuery("SELECT "+ODO_START+","+ODO_END+" FROM shifts WHERE id = "+list.get(i), null);
                long odoStart=0;
                long odoEnd=0;
                if (c.moveToFirst()) {
                    do {
                        odoStart = c.getLong(c.getColumnIndex(ODO_START));
                        odoEnd = c.getLong(c.getColumnIndex(ODO_END));
                        if (odoEnd-odoStart>0){
                            ret.mileageEarned +=  (float)(odoEnd-odoStart)*f;
                            ret.odometerTotal += (odoEnd-odoStart);
                        }
                    }while (c.moveToNext());
                }
                c.close();

                f = (f/100)* (ret.payed - ret.cost);
                ret.mileageEarned += f;
            }


            ret.total = ret.mileageEarned;
            ret.total += ret.payed - ret.cost;

            ret.averageTip = ((ret.payed - ret.cost)/ret.deliveries);
        } catch (NullPointerException e){
            e.printStackTrace(); //It would be better to return bogus info than crash. I was able to repro by clicking text message.
        }

        Log.i("CURSOR","QUERYING HOURS CALCULATION");
        if (shiftWhere==null){
            c = db.rawQuery(""
                    +"SELECT *,strftime('%w',`shifts`.`TimeStart`) AS `weekday` "
                    +"FROM hours_worked  "
                    +"INNER JOIN shifts ON hours_worked.shiftId=shifts.ID "
                    +"WHERE shifts.ID IN ("+shiftIds+") "
                    +"ORDER BY hours_worked.start DESC "
                    , null);
        } else {
            c = db.rawQuery(""
                    +"SELECT *,strftime('%w',`shifts`.`TimeStart`) AS `weekday` "
                    +"FROM hours_worked "
                    +"INNER JOIN shifts ON hours_worked.shiftId=shifts.ID "
                    +shiftWhere+" "
                    +"ORDER BY hours_worked.start DESC "
                    , null);
        }
        //DateTime start = null;
        //float rate=0;
        ret.hourlyPay = 0;
        ret.hours = 0;
        DateTime wageTransitionStart;
        DateTime shiftStart;
        DateTime shiftEnd;

        ArrayList<WageZone> wageZones = new ArrayList<WageZone>();

        if (c.moveToFirst()) {
            do {
                Log.i("CURSOR","ROW "+c.getPosition());
                //	for (int i = 0; i < c.getColumnCount(); i++){
                //		Log.i("CURSOR","  "+c.getColumnName(i)+" = "+c.getString(i));
                //	}

                wageTransitionStart = new DateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("start"))));
                shiftStart = new DateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("TimeStart"))));

                String timeEnd = c.getString(c.getColumnIndex("TimeEnd"));
                try {
                    shiftEnd   = new DateTime(Order.GetTimeFromString(timeEnd));
                } catch (IllegalStateException e){
                    shiftEnd = shiftStart;
                }

                WageZone wz = new WageZone();
                wz.rate = c.getFloat(c.getColumnIndex("rate"))/60;
                wz.shiftEnd = shiftEnd;
                wz.shiftStart = shiftStart;
                wz.wageTransitionStart = wageTransitionStart;
                wz.shiftID = c.getInt(c.getColumnIndex("shiftId"));
                wageZones.add(wz);

            } while (c.moveToNext());
            c.close();

            for (int i = 1; i < wageZones.size();i++){
                WageZone wzPrev = wageZones.get(i-1);
                WageZone wzCur = wageZones.get(i);
                if (wzPrev.shiftID==wzCur.shiftID){
                    wzPrev.wageTransitionEnd = wzCur.wageTransitionStart;
                }
                else {
                    wzPrev.wageTransitionEnd = wzPrev.shiftEnd;
                    Log.i("REST","logged hours"+(Minutes.minutesBetween(wzPrev.wageTransitionEnd, wzPrev.shiftEnd).getMinutes()/60));
                }
            }
            wageZones.get(wageZones.size()-1).wageTransitionEnd = wageZones.get(wageZones.size()-1).shiftEnd;

            ArrayList<WageZone> filteredZones = new ArrayList<WageZone>();
            for (WageZone wz : wageZones){
                if (wz.wageTransitionStart.isAfter(wz.shiftEnd))          continue;
                if (wz.wageTransitionEnd.  isBefore(wz.shiftStart))       continue;
                if (wz.wageTransitionStart.isAfter(wz.wageTransitionEnd)) continue;
                filteredZones.add(wz);
            }

            ret.hourlyPay =0;
            ret.hours=0;
            for (WageZone wz : filteredZones){
                if (wz.wageTransitionStart.isBefore(shiftStart)){
                    wz.wageTransitionStart = wz.shiftStart;
                }
                if (wz.wageTransitionEnd.isAfter(shiftEnd)){
                    wz.wageTransitionEnd = wz.shiftEnd;
                }
                Minutes m = Minutes.minutesBetween(wz.wageTransitionStart,wz.wageTransitionEnd);

                float min = ((float)m.getMinutes());

                float earnings = (float)wz.rate*min;
                ret.hourlyPay += Math.abs(earnings);
                float hours = Math.abs(min/60.0f);
                ret.hours += hours;

                String payRateStringKey = ""+(int)(wz.rate*10000);
                PayRatePieriod payRatePeriod = ret.payRatePieriods.get(payRateStringKey);
                if (payRatePeriod==null){
                    payRatePeriod = ret.new PayRatePieriod();
                    payRatePeriod.hourlyPay = wz.rate;
                    payRatePeriod.hours = 0;
                    ret.payRatePieriods.put(payRateStringKey, payRatePeriod);
                }
                payRatePeriod.hours += hours;
            }
        }
        else {//if we did not load any payment rows then this is an old shift from before we did this
            Log.i("CURSOR","No hours worked records for shift - Recreating for "+shiftIdsHash.keySet());
            Wage wage = new Wage();
            for (Long shiftId : shiftIdsHash.keySet()){
                MutableDateTime firstOrder = firstOrderTimeForShift(shiftId);
                MutableDateTime lastOrder = lastOrderTimeForShift(shiftId);
                Shift theShift = getShift(shiftId.intValue());
                Minutes shiftMinutesLong = Minutes.minutesBetween(theShift.startTime, theShift.endTime);
                if (shiftMinutesLong.getMinutes()==0){
                    theShift.startTime = firstOrder;
                    theShift.endTime = lastOrder;
                    Log.i("CURSOR","Updating shift times "+firstOrder);
                    saveShift(theShift);
                }
                String query = "SELECT COUNT(*) FROM `hours_worked` WHERE `shiftID`="+theShift.primaryKey;
                c = db.rawQuery(query, null);
                int count=-1;
                if (c != null){
                    if (c.moveToFirst()){
                        count = c.getInt(0);
                    }
                    c.close();
                }
                if (count==0){
                    Log.i("CURSOR","Creating summy wage");
                    wage.startTime = theShift.startTime;
                    try {
                        wage.wage=Float.parseFloat(prefs.getString("hourly_rate", "0"));
                    } catch (NumberFormatException e){
                        wage.wage=0;
                    }
                    saveWage(wage, theShift);
                }
                Minutes m = Minutes.minutesBetween(theShift.startTime, theShift.endTime);
                float earnings = wage.wage*((float)m.getMinutes());
                ret.hourlyPay += Math.abs(earnings);
                ret.hours += Math.abs(((float)m.getMinutes())/60.0f);
                Log.i("CURSOR"," earnings ="+earnings+"    minutes="+m.getMinutes()+"    start="+theShift.startTime+"    end="+theShift.endTime);

            }
        }
        Log.i("CURSOR","TOTAL Hourly Pay = "+ret.hourlyPay);
        c.close();
        return ret;
    }}


    public class WageZone {
        public int shiftID;
        public DateTime shiftStart;
        public DateTime shiftEnd;
        public DateTime wageTransitionStart;
        public DateTime wageTransitionEnd;
        public float rate;
    }
	
	/*public String getTodaysTipTotalString(final Context context, float currentTip) {
		final int shiftNumber = TodaysShiftCount;
		final DecimalFormat currency = new DecimalFormat("$#0.00");
		
		TipTotalData x  = getTipTotal(context,Shift + " = "+shiftNumber+" AND "+Payed+">0");
		
		String mps="";
		if (x.mileageEarned>0)
			mps = new String("\nMileage Earned:" + currency.format(x.mileageEarned));
		return new String("\nTips Made:" + currency.format((x.payed - x.cost) + currentTip) + mps + "\nDriver Earnings:"
				+ currency.format(x.total));		
	}*/

    static String GetDateString(Timestamp time){synchronized (DataBase.class){
        Calendar t = CalendarFromTimestamp(time);
        return GetDateString(t);
    }}

    static String GetDateString(MutableDateTime time){synchronized (DataBase.class){
        Calendar t = time.toGregorianCalendar();
        return GetDateString(t);
    }}

    static String GetDateString(DateTime time){synchronized (DataBase.class){
        Calendar t = time.toGregorianCalendar();
        return GetDateString(t);
    }}

    @SuppressLint("DefaultLocale")
    static String GetDateString(Calendar t){synchronized (DataBase.class){
        String dateString = String.format("%1$tY-%1$tm-%1$td %1$tH:%1tM:%1$tS.%1$tL", t);
        return dateString;
    }}


    static Calendar CalendarFromTimestamp(Timestamp time){synchronized (DataBase.class){
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        int year =  c.get(Calendar.YEAR);
        if (year > 2050){
            year -= 1900;
        }
        c.set(Calendar.YEAR,year);
        return c;
    }}

    @SuppressLint("DefaultLocale")
    public static String GetHumanReadableDateString(Timestamp time){  synchronized (DataBase.class){
        Calendar c = CalendarFromTimestamp(time);
        return String.format("%3$tm/%3$td/%3$tY", c,c,c);
    }}



    public int getShiftUndiliveredOrderCount(final int shift) {synchronized (DataBase.class){
        int retVal = -1;
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE + " WHERE " + Payed + "!=-1 AND Shift='"
                + shift + "'", null);
        if (c.moveToFirst()) {
            retVal = c.getInt(0);
        }
        c.close();
        return retVal;
    }}

    public int getThisShiftTotalOrderCount() {synchronized (DataBase.class){
        int retVal = -1;
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE + " WHERE Shift='" + TodaysShiftCount
                + "'", null);
        if (c.moveToFirst()) {
            retVal = c.getInt(0);
        }
        c.close();
        return retVal;
    }}

    public int getHoursSinceLastOrder() {synchronized (DataBase.class){
        long retVal = -1;
        String timeString = new String();
        final Cursor c = db.rawQuery("SELECT MAX(" + Time + ") FROM " + DATABASE_TABLE, null);
        if (c.moveToFirst()) {
            timeString = c.getString(0);
            try {
                retVal = Order.GetTimeFromString(timeString);
            } catch (final RuntimeException e) {
                retVal = System.currentTimeMillis();
            }
        } else {
            retVal = 0;
        }
        c.close();

        return (int) ((System.currentTimeMillis() - retVal) / 3600000);
    }}

    public String[] getLast10OrderNumbers() {synchronized (DataBase.class){
        final String retVal[] = new String[10];// rowid (select max(rowid) from
        // employee).
        int id = -1;
        Cursor c = db.rawQuery("SELECT MAX(ID) FROM " + DATABASE_TABLE, null);
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        c = db.rawQuery("SELECT " + OrderNumber + " FROM " + DATABASE_TABLE + " WHERE ID > " + (id - 10) + " AND "
                + OrderNumber + "!=''", null);
        if (c.moveToFirst()) {
            int i = 0;
            do {
                retVal[i++] = c.getString(c.getColumnIndex(OrderNumber));
            } while (c.moveToNext());
        }
        c.close();
        return retVal;
    }}

    // select distinct first name from employee;
    // select count(*) from (select distinct date send from mailings)
    synchronized public ArrayAdapter<String> getCostAdapter(final Context that) {synchronized (DataBase.class){
        String query = "SELECT "+Cost+" FROM " + DATABASE_TABLE + " GROUP BY "+Cost+" ORDER BY count(*) DESC LIMIT 0,100";
        ArrayList<String> arrayList = new ArrayList<String>();
        final Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                String address = c.getString(0);
                arrayList.add(address);
            } while (c.moveToNext());
        }
        c.close();

        return new ArrayAdapter<String>(that, R.layout.simple_auto_resize_list_item, arrayList);
    }}

    synchronized public int getCostScroll() {synchronized (DataBase.class){
        if (orderCosts == null) return 0;
        return orderCosts.length / 2;
    }}

    // Modifies an orders delivery order value
    public boolean changeOrder(final int key, final float order) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put(DeliveryOrder, order);
        boolean retVal = db.update(DATABASE_TABLE, args, key + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;
    }}

    // Modifies an orders delivery payment value
    public boolean setOrderPayment(final int key, final float payment, final int paymentType, float payment2, int paymentType2, boolean startNewRun, String notes, boolean undeliverable) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put(Payed, payment);
        args.put(PayedSplit, payment2);
        args.put(PaymentType, paymentType);
        args.put(PaymentType2, paymentType2);
        args.put(Notes, notes);
        args.put(PaymentTime, GetDateString(new Timestamp(System.currentTimeMillis())));
        args.put(ArivalTime, GetDateString(new Timestamp(System.currentTimeMillis())));// TODO:remove and set with GPS
        if (startNewRun){
            args.put(StartsNewRun, "1");
        } else {
            args.put(StartsNewRun, "0");
        }
        args.put("undeliverable",undeliverable);

        boolean retVal = db.update(DATABASE_TABLE, args, key + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;
    }}

    public boolean saveGeolocation(Order order) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();

        args.put(geocodeFailed, order.geocodeFailed);

        if (order.geocodeFailed){
            args.put("validatedAddress", false);
        } else {
            args.put("GPSLng", (float)order.geoPoint.lng);
            args.put("GPSLat", (float)order.geoPoint.lat);
            args.put("validatedAddress", order.isValidated);
        }

        final boolean retVal = db.update(DATABASE_TABLE, args, order.primaryKey + "= ID", null) > 0;
        dataDidJustChange();

        return retVal;
    }}

    // Modifies the fields in the order
    @SuppressLint("DefaultLocale")
    public boolean edit(final Order order) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put(OrderNumber, order.number);
        args.put(Address, order.address);
        args.put(PhoneNumber, order.phoneNumbersOnly());
        args.put(AptNumber, order.apartmentNumber.toUpperCase());
        args.put(Cost, order.cost);
        args.put(Time, GetDateString(order.time));
        args.put(Notes, order.notes);
        args.put(Payed, order.payed);
        args.put(PaymentType, order.paymentType);
        args.put(ExtraPay, order.extraPay);

        if (order.arivialTime != null) {
            args.put(ArivalTime, GetDateString(order.arivialTime));
        }
        if (order.payedTime != null) {
            args.put(PaymentTime, GetDateString(order.payedTime));
        }
        if (order.outOfTown1){
            args.put(OutOfTown, "1");
        } else{
            args.put(OutOfTown, "0");
        }
        if (order.outOfTown2){
            args.put(OutOfTown2, "1");
        } else{
            args.put(OutOfTown2, "0");
        }
        if (order.outOfTown3){
            args.put(OutOfTown3, "1");
        } else{
            args.put(OutOfTown3, "0");
        }
        if (order.outOfTown4){
            args.put(OutOfTown4, "1");
        } else{
            args.put(OutOfTown4, "0");
        }
        if (order.delivered){
            args.put("delivered", "1");
        } else{
            args.put("delivered", "0");
        }
        if (order.undeliverable){
            args.put("undeliverable", "1");
        } else{
            args.put("undeliverable", "0");
        }

        args.put(StartsNewRun, order.startsNewRun);

        args.put(PaymentType2, order.paymentType2);
        args.put(PayedSplit, order.payed2);

        args.put("GPSLng", (float)order.geoPoint.lng);
        args.put("GPSLat", (float)order.geoPoint.lat);


        args.put("validatedAddress", order.isValidated);

        args.put(geocodeFailed, order.geocodeFailed);
        args.put(smsCoustomer, order.smsCoustomer);


        //For taxi droid
        if (order.streetHail){
            args.put("StreetHail", "1");
        } else{
            args.put("StreetHail", "0");
        }
        boolean retVal;
        retVal = db.update(DATABASE_TABLE, args, order.primaryKey + "= ID", null) > 0;

        dataDidJustChange();

        for (int i=0; i < order.dropOffs.size();i++){
            updateDropOff(order.dropOffs.get(i),order);
        }
        return retVal;
    }}

    public boolean delete(final long rowId) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        boolean retVal = db.delete(DATABASE_TABLE, "ID" + "=" + rowId, null) > 0;
        dataDidJustChange();
        return retVal;

    }}


    public int getCurShift() {synchronized (DataBase.class){
        try {
            final Cursor c = db.rawQuery("SELECT MAX(ID) FROM shifts", null);

            if (c != null && c.moveToFirst()) {
                TodaysShiftCount = (int) c.getLong(0);
            }
            c.close();
        } catch (NullPointerException e){
            e.printStackTrace();
            return 1;
        }
        return TodaysShiftCount;
    }}

    public boolean updateAddress(final int updateAddressKey, final String newOrderAddress) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put(Address, newOrderAddress);
        final boolean retVal = db.update(DATABASE_TABLE, args, updateAddressKey + "= ID", null) > 0;
        dataDidJustChange();

        return retVal;
    }}

    public long getThisShiftOdomenterStart() {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT "+ODO_START+" FROM shifts WHERE ID ="+TodaysShiftCount, null);
        long odoVal=0;
        if (c != null && c.moveToFirst()) {
            odoVal =  c.getLong(0);
        }
        c.close();
        return odoVal;
    }}

    public long getThisShiftOdomenterEnd() {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT "+ODO_END+" FROM shifts WHERE ID ="+TodaysShiftCount, null);
        long odoVal=0;
        if (c != null && c.moveToFirst()) {
            odoVal =  c.getLong(0);
        }
        c.close();
        return odoVal;
    }}

    public int getMostRecientOdomenterValue() {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT "+ODO_START+","+ODO_END+" FROM shifts ORDER BY lastModificationTime LIMIT 1 ", null);
        int odoVal=0;
        if (c != null && c.moveToFirst()) {
            odoVal =  c.getInt(0);
            int v = c.getInt(1);
            if (v>odoVal) odoVal = v;
        }
        c.close();
        return odoVal;
    }}

    public int getNumberOfOrdersInShift(int shift) {synchronized (DataBase.class){
        final Cursor c = db.rawQuery("SELECT count(*) FROM "+DATABASE_TABLE+" WHERE Shift ="+shift, null);
        int totalOrdersThisShift=0;
        if (c != null && c.moveToFirst()) {
            totalOrdersThisShift =  c.getInt(0);
        }
        c.close();
        return totalOrdersThisShift;
    }}

    public int getNumberOfOrdersThisShift() {synchronized (DataBase.class){
        return getNumberOfOrdersInShift(TodaysShiftCount);
    }}


    public void setThisShiftOdometerStart(String string) {synchronized (DataBase.class){
        final ContentValues args = new ContentValues();
        long val = 0;
        try {
            val = Long.parseLong(string);
        } catch(Exception e){};
        args.put(ODO_START, val );
        //final boolean retVal = db.update("shifts", args, TodaysShiftCount + "= ID", null) > 0;
    }}

    public boolean setThisShiftOdometerEnd(String string) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        long val = 0;
        try {
            val = Long.parseLong(string);
        } catch(Exception e){};
        args.put(ODO_END, val);
        boolean retVal = db.update("shifts", args, TodaysShiftCount + "= ID", null) > 0;
        dataDidJustChange();
        return retVal;

    }}

    public void createShiftRecordIfNonExists() {synchronized (DataBase.class){
        int maxShiftFromOrders=0;
        int maxShiftFromShifts=-1;

        //For upgrade from before we had a shift table we need to check and see if there is
        //any existing shift records in the order table and create dummy shifts to match
        Cursor c = db.rawQuery("SELECT MAX(" + Shift + ") FROM " + DATABASE_TABLE, null);
        if (c != null && c.moveToFirst()) {
            maxShiftFromOrders = (int) c.getLong(0);
        }
        c.close();

        c = db.rawQuery("SELECT MAX(ID) FROM shifts", null);
        if (c != null && c.moveToFirst()) {
            maxShiftFromShifts = (int) c.getLong(0);
        }
        c.close();

        //This is because the 1st shift needs to be one instead of 0
        //and is really a startup task
        if (maxShiftFromShifts == 0 && maxShiftFromOrders==0){
            maxShiftFromOrders=1;
        }

        while (maxShiftFromShifts<maxShiftFromOrders){
            final ContentValues init = new ContentValues();
            init.put(TIME_START, 0);
            init.put(TIME_END,0);
            TodaysShiftCount = (int) db.insertOrThrow("shifts", null, init);
            dataDidJustChange();
            maxShiftFromShifts=TodaysShiftCount;
        };
    }}

    //This one is called when a new shift is either auto-created or because the user clicked the new shift icon
    public void setNextShift() {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());


        //Query all the records that are not payed and are in todays shift
        final Cursor c = db.rawQuery("SELECT ID FROM " + DATABASE_TABLE + " WHERE " + Payed + "=-1 AND Shift='"+ TodaysShiftCount + "'", null);

        // Set 0 time stamps for a new new shift record and create it to update our shift count
        ContentValues init = new ContentValues();
        init.put(TIME_START, GetDateString(DateTime.now()));
        init.put(TIME_END,GetDateString(DateTime.now()));
        TodaysShiftCount = (int) db.insertOrThrow("shifts", null, init);

        //Move all the open orders to the new shift
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    final long key = c.getLong(0);
                    final ContentValues args = new ContentValues();
                    args.put(Shift, TodaysShiftCount);
                    db.update(DATABASE_TABLE, args, key + "= ID", null);
                } while (c.moveToNext());
            }
        }
        c.close();

        //Set a pay rate record for this shift with the starting rate from the settings.
        init = new ContentValues();
        init.put("shiftID", TodaysShiftCount);

        String dateString = GetDateString(DateTime.now());
        Log.i("SHIFT","dateString ="+dateString);
        init.put("start", dateString);
        float rate = 0;
        try {
            rate = Float.parseFloat(prefs.getString("hourly_rate", "0"));
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
        init.put("rate", rate);

        dataDidJustChange();
    }}

    public ArrayAdapter<String> getOdometerPredtion() {synchronized (DataBase.class){
        Cursor c;
        long lastOdometer=0;

        c = db.rawQuery("SELECT "+ODO_END+" FROM shifts ORDER BY lastModificationTime DESC LIMIT 1", null);
        if (c != null && c.moveToFirst()) {
            lastOdometer = c.getLong(0);
        }
        c.close();

        c = db.rawQuery("SELECT "+ODO_START+" FROM shifts ORDER BY lastModificationTime DESC LIMIT 1", null);
        if (c != null && c.moveToFirst()) {
            long l = c.getLong(0);
            if (l > lastOdometer){
                lastOdometer = l;
            }
        }
        c.close();

        if (lastOdometer==0){
            c = db.rawQuery("SELECT MAX("+ODO_END+") FROM shifts", null);
            if (c != null && c.moveToFirst()) {
                lastOdometer = c.getLong(0);
            }
            c.close();
        }

        String[] odometerPrediction = new String[200];
        String s = new String(""+(lastOdometer+1));
        try {
            s = s.substring(0, s.length()-1);
            long l = Long.parseLong(s);

            for (int i= 0; i < 200; i++){
                odometerPrediction[i]=new String(""+(l+i));
            }
        } catch(Exception e){
            return null;
        }
        return new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, odometerPrediction);

    }}

    public void deleteShift(int viewingShift) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        int previousShift = getPrevoiusShiftNumber(viewingShift);
        if (previousShift < 1) {
            final Cursor c = db.rawQuery("SELECT ID FROM " + DATABASE_TABLE + " WHERE Shift='"+ viewingShift + "'", null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        final long key = c.getLong(0);
                        delete(key);
                    } while (c.moveToNext());
                }
            }
            c.close();
        } else {
            final ContentValues args = new ContentValues();
            args.put("Shift", previousShift);
            db.update(DATABASE_TABLE, args, "Shift = '"+viewingShift+"'", null);
        }
        db.delete("shifts", "ID" + "=" + viewingShift, null);
        dataDidJustChange();
    }}

    public int getPrevoiusShiftNumber(int viewingShift) {synchronized (DataBase.class){
        while (viewingShift>0){
            viewingShift--;
            final Cursor c = db.rawQuery("SELECT ID FROM shifts WHERE ID='"+ viewingShift + "'", null);
            if (c != null && c.moveToFirst()) {
                c.close();
                return viewingShift;
            }
        }
        return 0;
    }}

    String paymentTypeString(int paymentType){
        switch (paymentType){
            case 0: return context.getResources().getString(R.string.Cash);
            case 1: return context.getResources().getString(R.string.Check);
            case 2: return context.getResources().getString(R.string.Credit);
            case 3: return context.getResources().getString(R.string.ebt);
            case 4: return context.getResources().getString(R.string.debit);
            default: return  "Unpaid";
        }
    }

    public int getNextShiftNumber(int viewingShift) {synchronized (DataBase.class){
        long max=0;
        final Cursor cm = db.rawQuery("SELECT MAX(ID) FROM shifts", null);
        if (cm != null && cm.moveToFirst()) {
            max = cm.getLong(0);
            cm.close();
        }
        while (viewingShift<=max){
            viewingShift++;
            final Cursor c = db.rawQuery("SELECT ID FROM shifts WHERE ID='"+ viewingShift + "'", null);
            if (c != null && c.moveToFirst()) {
                c.close();
                return viewingShift;
            }
        }
        return getPrevoiusShiftNumber(viewingShift);
    }}

    @SuppressLint("DefaultLocale")
    public int findShiftForTime(Calendar calendar) {synchronized (DataBase.class){
        String dateString = String.format("%3$tY-%3$tm-%3$td", calendar, calendar, calendar);//""+time2.getYear()+"-0"+time2.getMonth()+"-"+time2.getDate();
        String sql = "SELECT "+Shift+" FROM " + DATABASE_TABLE + "  WHERE "+Time+" >= '"+dateString+"'";
        final Cursor c = db.rawQuery(sql, null);
        if (c != null && c.moveToFirst()) {
            int newShift = c.getInt(0);
            c.close();
            return newShift;
        }
        c.close();
        return -1;//error
    }}

    public String getCSVData(Calendar startDate, Calendar endDate, final ProgressDialog progressDialog, Activity activity) {synchronized (DataBase.class){
        String query = ""
                +"SELECT `Time`"        //0
                +	   ",`OrderNumber`" //1
                +	   ",`Shift`"       //2
                +	   ",`Address`"     //3
                +	   ",`AptNumber`"   //4
                +	   ",`Cost`"        //5
                +	   ",`PaymentType`" //6
                +	   ",`PaymentType2`"//7  __merge__
                +	   ",`PayedSplit`"  //8  __merge__
                +	   ",`Payed`"       //9
                +	   ",`Notes`"       //10 __merge notes with all the flags and extra pay
                +	   ",`OutOfTown`"   //11
                +	   ",`OutOfTown2`"  //12
                +	   ",`OutOfTown3`"  //13
                +	   ",`OutOfTown4`"  //14
                +	   ",`ExtraPay` "   //15
                +"FROM " + DATABASE_TABLE
                + " WHERE `" + Time + "` >= '"+ String.format("%3$tY-%3$tm-%3$td", startDate, startDate, startDate)+"'"
                + " AND `"   + Time + "` <= '"+ String.format("%3$tY-%3$tm-%3$td", endDate  , endDate  , endDate)+"'";

        String csvData = new String();
        final Cursor c = db.rawQuery(query, null);

        //Output table columns
        csvData +=      "Date";
        csvData += ","+ "Order Number";
        csvData += ","+ "Time";
        csvData += ","+ "Shift";
        csvData += ","+ "Address"; //merge in apt number
        csvData += ","+ "Cost";
        csvData += ","+ "Payment Type"; //merge in payment type 2
        csvData += ","+ "Payment Amount";   //Merge in payed split
        csvData += ","+ "Tip";    //Calculated
        csvData += ","+ "Other";  //Combination of notes, out of town flags, and extra pay
        csvData += "\n";




        progressDialog.setMax(c.getCount());



        int index = 0;
        final int count = c.getCount();
        if (c != null && c.moveToFirst()) {

            int timeCol = c.getColumnIndex("Time");
            int orderNumCol = c.getColumnIndex("OrderNumber");
            int shiftCol = c.getColumnIndex("Shift");
            int aptCol = c.getColumnIndex("AptNumber");
            int costCol = c.getColumnIndex("Cost");
            int paymentTypeCol = c.getColumnIndex("PaymentType");
            int paymentType2Col = c.getColumnIndex("PaymentType2");
            int paymentCol = c.getColumnIndex("Payed");
            int payment2Col = c.getColumnIndex("PayedSplit");
            int undeliverableCol = c.getColumnIndex("undeliverable");


            DateFormat dateFormater = DateFormat.getDateInstance();
            DateFormat timeFormatter = DateFormat.getTimeInstance();

            //String[] altPayAmount = new String[4];
            String[] altPayLabel  = new String[4];


            altPayLabel[0]  = prefs.getString("per_out_of_town_delivery_label1","");
            altPayLabel[1]  = prefs.getString("per_out_of_town_delivery_label2","");
            altPayLabel[2]  = prefs.getString("per_out_of_town_delivery_label3","");
            altPayLabel[3]  = prefs.getString("per_out_of_town_delivery_label4","");


            do {

                final float t = index;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float c = count;
                        float r = t/c;
                        int percentage = (int)(r*100);
                        progressDialog.setMessage("Processing "+percentage+"% done");
                    }
                });


                progressDialog.setProgress(++index);
                //progressDialog.setMessage("Exported "+index+" orders");

                Date orderTime = new Date(Order.GetTimeFromString(c.getString(timeCol)));

                boolean isUndeliverable=false;
                if (undeliverableCol>0) {
                    Log.e("MARC","**************** MARC ********************");
                    isUndeliverable = c.getInt(undeliverableCol) == 1;
                }
                String dateString = dateFormater.format(orderTime);
                String orderNumberString = c.getString(orderNumCol);
                String timeString = timeFormatter.format(orderTime);
                String shiftString = c.getString(shiftCol);
                String apt = c.getString(aptCol);
                String addressString;
                if (apt != null && apt.length() > 0){
                    addressString = c.getString(3)+" Apt:"+apt;
                }else {
                    addressString = c.getString(3);
                }

                float  cost = c.getFloat(costCol);
                String costString = Utils.getFormattedCurrency(cost);

                float paymentSplit = c.getFloat(payment2Col);
                float payed = c.getFloat(paymentCol);
                float extraPay = c.getFloat(15);

                int paymentType = c.getInt(paymentTypeCol);
                int paymentTypeSplit = c.getInt(paymentType2Col);

                String paymentAmountString;
                String paymentTypeString;
                String tipString;
                if (paymentSplit>0){
                    paymentAmountString =
                            Utils.getFormattedCurrency(payed)
                                    +" + "+ Utils.getFormattedCurrency(paymentSplit)
                                    +" = "+ Utils.getFormattedCurrency(paymentSplit + payed);

                    Log.i("MARC","payment type = "+paymentType);
                    paymentTypeString = paymentTypeString(paymentType)
                            +" & "+   paymentTypeString(paymentTypeSplit);

                    tipString = Utils.getFormattedCurrency(payed + paymentSplit - cost);

                } else {
                    if (isUndeliverable){
                        paymentAmountString = "";
                        tipString = "";
                        paymentTypeString = "Undeliverable";
                    }
                    else if (payed < 0) {
                        paymentAmountString = "0";
                        tipString = "";
                        paymentTypeString   = context.getString(R.string.undelivered);
                    } else {
                        tipString = Utils.getFormattedCurrency(payed - cost);
                        paymentAmountString =  Utils.getFormattedCurrency(payed);
                        if (paymentType>=0) {
                            paymentTypeString = paymentTypeString(paymentType);
                        } else {
                            paymentTypeString = "";
                        }

                    }
                }

                String notes = c.getString(10);

                StringBuilder sb = new StringBuilder();
                if (notes != null && notes.length()>0){
                    sb.append(notes);
                    sb.append("  ");
                }
                if (extraPay>0){
                    sb.append(context.getResources().getString(R.string.Extra_Pay));
                    sb.append(":"+ Utils.getFormattedCurrency(extraPay));
                    sb.append("  ");
                }

                for (int i = 0; i < 4; i++){
                    if (c.getInt(11+i) >0 && altPayLabel[i]!=null && altPayLabel[i].length()>0){
                        sb.append(altPayLabel[i]+"  ");
                    }
                }

                if (extraPay>0){
                    sb.append("Extra Pay:"+ Utils.getFormattedCurrency(extraPay));
                }

                String otherString = sb.toString();

                csvData += " \""+ cvsEscapeString(dateString)          +"\"";
                csvData += ",\""+ cvsEscapeString(orderNumberString)   +"\"";
                csvData += ",\""+ cvsEscapeString(timeString)          +"\"";
                csvData += ",\""+ cvsEscapeString(shiftString)         +"\"";
                csvData += ",\""+ cvsEscapeString(addressString)       +"\"";
                csvData += ",\""+ cvsEscapeString(costString)          +"\"";
                csvData += ",\""+ cvsEscapeString(paymentTypeString)   +"\"";
                csvData += ",\""+ cvsEscapeString(paymentAmountString) +"\"";
                csvData += ",\""+ cvsEscapeString(tipString)           +"\"";
                csvData += ",\""+ cvsEscapeString(otherString)         +"\"";

                csvData += "\n";

            } while (c.moveToNext());
        }
        c.close();
        return csvData;
    }}

    String cvsEscapeString(String input){
        try {
            String output = input.replaceAll("\"", "\"\"");
            return output;
        } catch (NullPointerException e){
            return "null";
        }
    }


    static String getFormattedCurrency(Float f){synchronized (DataBase.class){
        String currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        DecimalFormat currency = new DecimalFormat("#0.00");
        currency.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
        currency.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
        return currencySymbol+currency.format(f);
    }}


    public void searchAddressSuggestionsFor(String addressOrNotes, ArrayList<AddressInfo> resultsFromDB) {synchronized (DataBase.class){
        String addressSoFarPlusSpace = DatabaseUtils.sqlEscapeString(addressOrNotes+" %");
        String notesSoFar = DatabaseUtils.sqlEscapeString("%"+addressOrNotes+"%");
        addressOrNotes = DatabaseUtils.sqlEscapeString(addressOrNotes+"%");

        try {
            String query = "SELECT * FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+addressOrNotes+" OR Notes LIKE "+notesSoFar+" GROUP BY "+Address+" ORDER BY "+Address+" LIKE "+addressSoFarPlusSpace+" DESC,count(*) DESC LIMIT 0,100";
            final Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {

                do {
                    Order o = new Order(c);
                    if (o.isValidated && o.geoPoint != null){
                        resultsFromDB.add(new AddressInfo(o.address,o.geoPoint));
                    } else {
                        resultsFromDB.add(new AddressInfo(o.address,null));
                    }
                } while (c.moveToNext());
            }
            c.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }}

    public void getAddressSuggestionsFor(String addressSoFar, ArrayList<String> resultsFromDB) {synchronized (DataBase.class){
        String addressSoFarPlusSpace = DatabaseUtils.sqlEscapeString(addressSoFar+" %");
        addressSoFar = DatabaseUtils.sqlEscapeString(addressSoFar+"%");
        try {
            String query = "SELECT "+Address+" FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+addressSoFar+" GROUP BY "+Address+" ORDER BY "+Address+" LIKE "+addressSoFarPlusSpace+" DESC,count(*) DESC LIMIT 0,100";
            final Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String address = c.getString(0);
                    resultsFromDB.add(address);
                } while (c.moveToNext());
            }
            c.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }}

    public static final int NO_PAYMENT = 0;        //HArdcoded elsewhere these values can not change
    public static final int CASH_PAYMENT = 1;
    public static final int CREDIT_PAYMENT = 2;
    public static final int ACCOUNT_PAYMENT = 3;



    public void estimateShiftTimes(Shift shift) {synchronized (DataBase.class){
        long firstTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();
        int successes=0;
        Cursor c;
        try {
            c = db.rawQuery("SELECT MAX(Time) FROM "+DATABASE_TABLE +" WHERE Shift="+shift.primaryKey, null);
            if (c != null && c.moveToFirst()) {
                lastTime = Order.GetTimeFromString(c.getString(0));
                successes++;
            }
            c.close();
            c = db.rawQuery("SELECT MIN(Time) FROM "+DATABASE_TABLE +" WHERE Shift="+shift.primaryKey, null);
            if (c != null && c.moveToFirst()) {
                firstTime = Order.GetTimeFromString(c.getString(0));
                successes++;
            }
            c.close();
            if (successes<2) { //Then its a new empty shift set the start time to now
                shift.startTime.setMillis(System.currentTimeMillis());
                shift.endTime.setMillis(System.currentTimeMillis());
                shift.noEndTime=true;
            } else {
                if (shift.startTime.getMillis() > firstTime || shift.startTime.getMillis()==0) {
                    shift.startTime.setMillis(firstTime);
                }
                if (shift.endTime.getMillis() < lastTime){
                    shift.endTime.setMillis(lastTime);
                }
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            shift.endTime.setMillis(lastTime);
            shift.startTime.setMillis(firstTime);
        }
    }}


    public MutableDateTime firstOrderTimeForShift(long shiftId) {synchronized (DataBase.class){
        MutableDateTime firstTime = new MutableDateTime();

        Cursor c;
        c = db.rawQuery("SELECT MIN(Time) FROM "+DATABASE_TABLE +" WHERE Shift="+shiftId, null);
        if (c != null && c.moveToFirst()) {
            String rawTime = c.getString(0);
            if (rawTime==null){
                firstTime=null;
            } else {
                firstTime.setMillis(Order.GetTimeFromString(rawTime));
            }
        }
        c.close();

        return firstTime;
    }}




    public MutableDateTime lastOrderTimeForShift(long shiftID) {synchronized (DataBase.class){
        long lastTime = 0;

        Cursor c;
        c = db.rawQuery("SELECT MAX(Time) FROM "+DATABASE_TABLE +" WHERE Shift="+shiftID, null);
        if (c != null && c.moveToFirst()) {
            String s = c.getString(0);
            if (s!=null) lastTime = Order.GetTimeFromString(s);
        }
        c.close();

        c = db.rawQuery("SELECT MAX(ArivialTime) FROM "+DATABASE_TABLE +" WHERE Shift="+shiftID, null);
        if (c != null && c.moveToFirst()) {
            String s = c.getString(0);
            if (s!=null) {
                long t = Order.GetTimeFromString(s);
                if (t>lastTime)lastTime=t;
            }
        }
        c.close();

        c = db.rawQuery("SELECT MAX(PaymentTime) FROM "+DATABASE_TABLE +" WHERE Shift="+shiftID, null);
        if (c != null && c.moveToFirst()) {
            String s = c.getString(0);
            if (s!=null) {
                long t = Order.GetTimeFromString(s);
                if (t>lastTime)lastTime=t;
            }
        }
        c.close();

        if (lastTime==0) return null;
        return new MutableDateTime(lastTime);
    }}


    //The apartment number field from the order is used as the order type field in taxi droid
    public ArrayAdapter<String> getOrderTypeAdapter() {synchronized (DataBase.class){
        ArrayList<String> tripTypeList = new ArrayList<String>();
        Cursor c = db.rawQuery("SELECT DISTINCT AptNumber FROM "+DATABASE_TABLE, null);
        if (c.moveToFirst()){
            do {
                String s = c.getString(0);
                if (s!=null && s.length()>1){
                    tripTypeList.add(s);
                }
            } while (c.moveToNext());
        }
        c.close();
        return new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,tripTypeList);
    }}

    public class NoteEntry {
        public String note;
        public String date;
    }

    //This is a terrible hack, getPastNotesForAddressAndAptNo gets called first and then
    //sets a global variable.
    //TODO: I need to merge this and have a single function that returns an array list or something
    String currentAptNo="";
    public ArrayList<NoteEntry> getPastNotesForAddress(String address) {synchronized (DataBase.class){
        ArrayList<NoteEntry> retArray = new ArrayList<NoteEntry>();
        try {
            address = DatabaseUtils.sqlEscapeString(address);
            String query;
            if (currentAptNo.length() > 1){
                query = "SELECT "+Notes+",Time FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+address+" AND "+AptNumber+" NOT LIKE '"+currentAptNo+"' ORDER BY Time DESC LIMIT 0,25";
            } else {
                //TODO: Fix/Text seems wrong
                query = "SELECT "+Notes+",Time FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+address+" ORDER BY Time DESC LIMIT 0,25";
            }
            final Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String note = c.getString(0);
                    Timestamp time = new Timestamp(Order.GetTimeFromString(c.getString(1)));
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(time.getTime());
                    if (note.length()>0){
                        NoteEntry ne = new NoteEntry();
                        ne.date = String.format("%tb %te", cal,cal) ;
                        ne.note = note;
                        retArray.add(ne);
                    }
                } while (c.moveToNext());
            }
            c.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return retArray;
    }}

    public ArrayList<NoteEntry> getPastNotesForAddressAndAptNo(String address,String aptNo) {synchronized (DataBase.class){
        ArrayList<NoteEntry> retArray = new ArrayList<NoteEntry>();
        currentAptNo=aptNo;
        //if (aptNo.length() < 1) return retVal;
        try {
            address = DatabaseUtils.sqlEscapeString(address);
            String query = "SELECT "+Notes+",Time FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+address+" AND "+AptNumber+" LIKE '"+aptNo+"' ORDER BY Time DESC LIMIT 0,25";
            Log.i("CUSROR",query);
            final Cursor c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String note = c.getString(0);
                    Timestamp time = new Timestamp(Order.GetTimeFromString(c.getString(1)));
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(time.getTime());
                    if (note.length()>0){
                        NoteEntry ne = new NoteEntry();
                        ne.date = String.format("%tb %te", cal,cal) ;
                        ne.note = note;
                        retArray.add(ne);
                    }
                } while (c.moveToNext());
            }
            c.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return retArray;
    }}

/*	public ArrayList<String> getPhoneNumbersFromNotes(String address,String aptNo) {synchronized (DataBase.class){
		currentAptNo=aptNo;
		ArrayList<String> retArray = new ArrayList<String>();
		String retVal = "";
		Pattern phoneNumber = Pattern.compile("([0-9]{0,4})\\){0,1}\\s{0,1}\\-{0,1}([0-9]{3,4})\\s{0,1}\\-\\s{0,1}([0-9]{4})");
		//if (aptNo.length() < 1) return retVal;
		try {
			address = DatabaseUtils.sqlEscapeString(address);
			String query = "SELECT "+Notes+",Time FROM " + DATABASE_TABLE + " WHERE "+Address+" LIKE "+address+" AND "+AptNumber+" LIKE '"+aptNo+"' ORDER BY Time DESC LIMIT 0,25";
			final Cursor c = db.rawQuery(query, null);
			if (c != null && c.moveToFirst()) {
				do {
					String note = c.getString(0);
					if (note.length()>0){
						//retVal += note +"\n";
						
					}
				} while (c.moveToNext());
			}
			c.close();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
		return retArray;
	}}*/

    public void saveGpsNote(GpsNote note){synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        final ContentValues args = new ContentValues();
        args.put("note"  , note.notes);
        args.put("GPSLat", note.lat);
        args.put("GPSLng", note.lng);
        if (note.notification) args.put("notification", 1);  else  args.put("notification", 0);
        if (note.alarm ) args.put("alarm", 1); else args.put("alarm", 0);
        args.put("time"  , GetDateString(note.time));
        if (note.ID != 0){
            args.put("ID", note.ID);
        }
        try {
            db.execSQL("ALTER TABLE  gps_notes          ADD  notification     BOOLEAN");
            db.execSQL("ALTER TABLE  gps_notes          ADD  alarm     BOOLEAN");
        } catch (SQLiteException e){
            e.printStackTrace();
            //TODO: remove this all together some time soon
        }


        note.ID = db.insertWithOnConflict("gps_notes", null, args, SQLiteDatabase.CONFLICT_REPLACE);
    }}


    public ArrayList<NotedObject> getNotes(final double latitude,final double longitude, boolean loadOrders) {synchronized (DataBase.class){


        ArrayList<NotedObject> notes = new ArrayList<NotedObject>();

        String query = "SELECT *, (((`GPSLat`-'"+latitude+"')*(`GPSLat`-'"+latitude+"')) + ((`GPSLng`-'"+longitude+"')*(`GPSLng`-'"+longitude+"'))) AS dist FROM gps_notes WHERE length(note)>0 ORDER BY dist ASC LIMIT 10";

        Log.i("CURSOR","get gpos notes"+ query);

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                GpsNote note = new GpsNote(c);
                notes.add(note);

            } while (c.moveToNext());
        }
        c.close();

        if (loadOrders) {
            query = "SELECT *, (((`GPSLat`-'"+latitude+"')*(`GPSLat`-'"+latitude+"')) + ((`GPSLng`-'"+longitude+"')*(`GPSLng`-'"+longitude+"'))) AS dist FROM orders WHERE length(Notes)>0 ORDER BY dist ASC LIMIT 10";
            c = db.rawQuery(query, null);
            if (c != null && c.moveToFirst()) {
                do {
                    Order order = new Order(c);
                    notes.add(order);
                } while (c.moveToNext());
            }
            c.close();
        }

        Collections.sort(notes,  new Comparator<NotedObject>() {public int compare(NotedObject o1, NotedObject o2) {
            double x1 = o1.getLat();
            double x2 = latitude;
            double y1 = o1.getLng();
            double y2 = longitude;
            double dist1 = ((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2));
            o1.distanceInLatLng = dist1;

            x1 = o2.getLat();
            y1 = o2.getLng();

            double dist2 = ((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2));
            o2.distanceInLatLng = dist2;

            return (int)(dist1-dist2);
        }});

        return notes;
    }}


    public boolean delete(GpsNote gpsNote) {synchronized (DataBase.class){
        BackupManager.dataChanged(context.getPackageName());

        boolean retVal = db.delete("gps_notes", "ID" + "=" + gpsNote.ID, null) > 0;
        return retVal;


    }}


    public ArrayList<String> wageHistory() {synchronized (DataBase.class){
        ArrayList<String> wageHistory = new ArrayList<String>();
        Cursor c = db.rawQuery(""
                +"SELECT DISTINCT(rate) FROM `hours_worked` ORDER BY `lastModificationTime` DESC LIMIT 20 "
                , null);
        if (c != null){
            if (c.moveToFirst()){
                do {
                    String wage = c.getString(0);
                    try {
                        wage = Utils.getFormattedCurrency(Utils.parseCurrency(wage));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    wageHistory.add(wage);
                } while (c.moveToNext());
            }
            c.close();
        }

        return wageHistory;
    }}

    public long setWage(float wage, Shift shift, DateTime when){synchronized (DataBase.class){
        ContentValues c = new ContentValues();
        DecimalFormat df = new DecimalFormat("#.##");
        c.put("rate", df.format(wage));
        c.put("start", GetDateString(when));
        c.put("shiftID",shift.primaryKey);

        return db.insert("hours_worked", null, c);
    }}


    public void saveWage(Wage wage,Shift shift){synchronized (DataBase.class){
        ContentValues c = new ContentValues();
        DecimalFormat df = new DecimalFormat("#.##");
        c.put("rate", df.format(wage.wage));
        c.put("start", GetDateString(wage.startTime));
        c.put("shiftID",shift.primaryKey);
        c.put("ID",wage.id);

        db.insertWithOnConflict("hours_worked", null, c, SQLiteDatabase.CONFLICT_REPLACE);
    }}


    public Wage currentWage() {synchronized (DataBase.class){
        Wage wage = new Wage();
        Cursor c = db.rawQuery(""
                +"SELECT * FROM `hours_worked` ORDER BY `start` DESC LIMIT 1 "
                , null);
        if (c != null){
            if (c.moveToFirst()){
                try {
                    wage.wage = Float.parseFloat(c.getString(c.getColumnIndex("rate")));
                } catch (NumberFormatException e){ wage.wage = 0; };
                wage.id = c.getLong(c.getColumnIndex("ID"));
                wage.startTime = new MutableDateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("start"))));
            }
            c.close();
        }
        return wage;
    }}


    public Wage getWage(long wageID) {synchronized (DataBase.class){
        Wage wage = new Wage();
        Cursor c = db.rawQuery(""
                +"SELECT * FROM `hours_worked` WHERE ID="+wageID
                , null);
        if (c != null){
            if (c.moveToFirst()){
                try {
                    wage.wage = Float.parseFloat(c.getString(c.getColumnIndex("rate")));
                }catch (NumberFormatException e){ wage.wage = 0; };
                wage.id = c.getLong(c.getColumnIndex("ID"));
                wage.startTime = new MutableDateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("start"))));
            }
            c.close();
        }
        return wage;
    }}

    public ArrayList<Wage> wageTransitionsForShift(Shift shift) {synchronized (DataBase.class){
        ArrayList<Wage> wages = new ArrayList<Wage>();
        Cursor c;

        String query = "SELECT * FROM `hours_worked` WHERE `shiftID`="+shift.primaryKey+" ORDER BY `start` DESC ";

        c = db.rawQuery(query, null);
        if (c != null){
            if (c.moveToFirst()){
                do {
                    Wage wage = new Wage();
                    wage.wage = Float.parseFloat(c.getString(c.getColumnIndex("rate")));
                    wage.id = c.getLong(c.getColumnIndex("ID"));
                    wage.startTime = new MutableDateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("start"))));

                    wages.add(wage);
                } while (c.moveToNext());
            }
            c.close();
        }

        return wages;
    }}


    public boolean isTodaysShift(Shift shift) {synchronized (DataBase.class){
        if (shift.primaryKey==TodaysShiftCount) return true;
        return false;
    }}


    public boolean isWageFirstInShift(Wage wage, Shift shift) {synchronized (DataBase.class){
        boolean retVal = false;
        String query = "SELECT * FROM `hours_worked` WHERE `shiftID`="+shift.primaryKey+" ORDER BY `start` ASC LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if (c != null){
            if (c.moveToFirst()){
                if (  wage.wage == Float.parseFloat(c.getString(c.getColumnIndex("rate")))
                        && wage.startTime.isEqual(new DateTime(Order.GetTimeFromString(c.getString(c.getColumnIndex("start"))))) )
                {
                    retVal = true;
                }
            }
            c.close();
        }
        return retVal;
    }}


    public ArrayList<Order> findOrdersForPhoneNumber(String string) {
        // TODO: Search notes and phone number field for orders and return AddressInfo objects

        String searchFor = DatabaseUtils.sqlEscapeString("%"+string+"%");
        String query = "SELECT * FROM `orders` WHERE `PhoneNumber` LIKE "+searchFor+" OR `notes` LIKE "+searchFor+" limit 20";
        ArrayList<Order> result = new ArrayList<Order>();


        Cursor c = db.rawQuery(query, null);
        if (c != null){
            if (c.moveToFirst()){
                do {
                    Order order = new Order(c);
                    result.add(order);

                } while (c.moveToNext());
            }
            c.close();
        }
        return result;
    }



    public ArrayList<String> getRecentPhoneNumbers(String string) {

        String searchFor = DatabaseUtils.sqlEscapeString("%"+string);
        String query = "SELECT  `PhoneNumber` FROM `orders` ORDER BY `Time` limit 100";
        ArrayList<String> result = new ArrayList<String>();

        Cursor c = db.rawQuery(query, null);
        if (c != null){
            if (c.moveToFirst()){
                int phoneNumberIndex = c.getColumnIndex("PhoneNumber");
                do {
                    String phoneNumberString = c.getString(phoneNumberIndex);
                    result.add(phoneNumberString);

                } while (c.moveToNext());
            }
            c.close();
        }
        return result;
    }


}