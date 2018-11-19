package catglo.com.deliverydroid.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.widget.Toast
import catglo.com.deliveryDatabase.DataBase
import catglo.com.deliveryDatabase.StreetList
import catglo.com.deliverydroid.AboutActivity
import catglo.com.deliverydroid.BuildConfig
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.backup.GoogleDriveBackupRestoreActivity
import catglo.com.deliverydroid.widgets.TwoLinesListPreference
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity


import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class SettingsActivity : PreferenceActivity(), OnSharedPreferenceChangeListener {
    internal var databaseFileCopy: Preference? = null
    internal var databaseOnSdcard: CheckBoxPreference? = null

    internal var prefs: SharedPreferences? = null
    internal var pattern: Pattern? = null
    private var streetList: StreetList? = null
    internal var dataBase: DataBase? = null

    private var pedit: Editor? = null
    private var databaseFileMerge: Preference? = null
    private var databaseFileUse: Preference? = null
    private var navigationListPref: TwoLinesListPreference? = null
    private var aboutPref: Preference? = null
    private var backupToGoogleDrive: Preference? = null

    override fun onDestroy() {
        dataBase!!.close()
        super.onDestroy()
    }

    public override fun onStart() {
        super.onStart()

        // EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    public override fun onStop() {
        super.onStop()

        // EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.settings)


        if (dataBase == null) {
            dataBase = DataBase(applicationContext)
            dataBase!!.open()
        }


        navigationListPref = preferenceScreen.findPreference("navigationIntent") as TwoLinesListPreference
        navigationListPref!!.entriesSubtitles = resources.getTextArray(R.array.navigation_options_summary)


        //		InstructionsPreference instructionsAltPay = (InstructionsPreference) getPreferenceScreen().findPreference("instructionsAltPay");
        //		instructionsAltPay.

        streetList = StreetList.LoadState(applicationContext)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pedit = prefs?.edit()
        pedit!!.putString("centrPoint_lat_s", prefs?.getFloat("centrPoint_lat", 0f).toString() + "")
        pedit!!.putString("centrPoint_lng_s", prefs?.getFloat("centrPoint_lng", 0f).toString() + "")
        pedit!!.apply()

        run {
            databaseFileCopy = preferenceScreen.findPreference("DatabaseFileCopy") as Preference
            databaseOnSdcard = preferenceScreen.findPreference("DatabaseOnSdcard") as CheckBoxPreference
            //databaseUpload   = (CheckBoxPreference) getPreferenceScreen().findPreference("DatabaseUpload");
            databaseFileMerge = preferenceScreen.findPreference("DatabaseFileMerge") as Preference
            databaseFileUse = preferenceScreen.findPreference("DatabaseFileUse") as Preference
            aboutPref = preferenceScreen.findPreference("aboutPref") as Preference
            backupToGoogleDrive = preferenceScreen.findPreference("backupToGoogleDrive") as Preference

            aboutPref!!.onPreferenceClickListener = OnPreferenceClickListener {
                val i = Intent(applicationContext, AboutActivity::class.java)
                startActivity(i)
                false
            }

            backupToGoogleDrive!!.onPreferenceClickListener = OnPreferenceClickListener {
                val i = Intent(applicationContext, GoogleDriveBackupRestoreActivity::class.java)
                startActivity(i)
                false
            }


            databaseFileMerge!!.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->

                MaterialFilePicker()
                    .withActivity(this@SettingsActivity)
                    .withRequestCode(2)
                    .withFilter(Pattern.compile(".*\\.SQLite$")) // Filtering files and directories by file name using regexp
                    .withFilterDirectories(true) // Set directories filterable (false by default)
                    .withHiddenFiles(false) // Show hidden files and folders
                    .start()

                false
            }

            databaseFileUse!!.onPreferenceClickListener = OnPreferenceClickListener {
                MaterialFilePicker()
                    .withActivity(this@SettingsActivity)
                    .withRequestCode(1)
                    .withFilter(Pattern.compile(".*\\.SQLite$")) // Filtering files and directories by file name using regexp
                    .withFilterDirectories(true) // Set directories filterable (false by default)
                    .withHiddenFiles(false) // Show hidden files and folders
                    .start()

                false
            }


            databaseOnSdcard?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                try {
                    if (newValue as Boolean == true) {
                        //copy to sdcard
                        copyDatabse(
                            filesDir.toString() + "/" + BuildConfig.DATABASE_NAME,
                            Environment.getExternalStorageDirectory().toString() + "/" + BuildConfig.DATABASE_NAME
                        )

                    } else {
                        //copy from sdcard
                        copyDatabse(
                            Environment.getExternalStorageDirectory().toString() + "/" + BuildConfig.DATABASE_NAME,
                            filesDir.toString() + BuildConfig.DATABASE_NAME
                        )

                    }
                    val toast = Toast.makeText(
                        this@SettingsActivity.applicationContext,
                        "Database file moved! You must restart this app.", Toast.LENGTH_LONG
                    )
                    toast.show()
                    setResult(400)
                    finish()

                } catch (e: FileNotFoundException) {
                    val toast = Toast.makeText(
                        this@SettingsActivity.applicationContext,
                        "DID NOT MOVE DATABASE FILE! (file not found error)" + filesDir + "/" + BuildConfig.DATABASE_NAME + "   " + Environment.getExternalStorageDirectory() + "/" + BuildConfig.DATABASE_NAME,
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return@OnPreferenceChangeListener false
                } catch (e: IOException) {
                    val toast = Toast.makeText(
                        this@SettingsActivity.applicationContext,
                        "DID NOT MOVE DATABASE FILE! You may have insufficent space on your sdcard.",
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return@OnPreferenceChangeListener false
                }

                true
            }


            databaseFileCopy?.onPreferenceClickListener = OnPreferenceClickListener {

                if (prefs?.getBoolean("DatabaseOnSdcard", false) == true) {
                    var filePath = Environment.getExternalStorageDirectory().toString() + "/" + BuildConfig.DATABASE_NAME

                    AlertDialog.Builder(this@SettingsActivity)
                        .setTitle("Database is on your external storage already")
                        .setMessage("You can copy it off your phone by plugging it in to your computer or by sharing the file")
                        .setPositiveButton("Share file"
                        ) { _, _ ->
                            val path = Uri.parse("file://" + filePath)
                            val emailIntent = Intent(Intent.ACTION_SEND)
                            emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            emailIntent.putExtra(Intent.EXTRA_STREAM, path)
                            emailIntent.type = "delivery/mysql"
                            startActivity(Intent.createChooser(emailIntent, "Send"))
                        }
                        .setNegativeButton("Copy manually",null)
                        .show()


                    return@OnPreferenceClickListener false
                } else {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val fileName = "DeliveryDroid" + timeStamp + ".SQLite";

                    try {
                        val filePath = Environment.getExternalStorageDirectory().toString() + "/" + fileName
                        copyDatabse(
                            filesDir.toString() + "/" + BuildConfig.DATABASE_NAME,
                            filePath
                        )

                        AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("Database file copied to\n"+fileName)
                            .setMessage("You can copy it off your phone by plugging it in to your computer or by sharing the file")
                            .setPositiveButton("Share file"
                            ) { _, _ ->
                                val path = Uri.parse("file://" + filePath)
                                val emailIntent = Intent(Intent.ACTION_SEND)
                                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                emailIntent.putExtra(Intent.EXTRA_STREAM, path)
                                emailIntent.type = "delivery/mysql"
                                startActivity(Intent.createChooser(emailIntent, "Send"))
                            }
                            .setNegativeButton("Copy manually",null)
                            .show()


                    } catch (e: FileNotFoundException) {
                        val toast = Toast.makeText(
                            this@SettingsActivity.applicationContext,
                            "DID NOT BACKUP DATABASE FILE! You may have specified an invalid file name",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        return@OnPreferenceClickListener false
                    } catch (e: IOException) {
                        val toast = Toast.makeText(
                            this@SettingsActivity.applicationContext,
                            "DID NOT BACKUP DATABASE FILE! You may have insufficent space on your sdcard.",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        return@OnPreferenceClickListener false
                    }

                }
                true
            }
        }


    }


    @Throws(FileNotFoundException::class, IOException::class)
    internal fun copyDatabse(to: String, from: String) {
        val input = FileInputStream(to)
        val myOutput = FileOutputStream(from)

        // transfer bytes from the inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        length = input.read(buffer)
        while (length > 0) {
            myOutput.write(buffer, 0, length)
            length = input.read(buffer)
        }

        // Close the streams
        myOutput.flush()
        myOutput.close()
        input.close()
    }

    override fun onResume() {
        super.onResume()

        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode)
        {
            1 -> {
                //databaseFileUse file selected
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                    val builder = AlertDialog.Builder(this@SettingsActivity )
                    builder.setMessage(R.string.databaseUseWarning)
                        .setCancelable(false)
                        .setPositiveButton(R.string.Yes, object:DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                var path : File? = null
                                if (prefs?.getBoolean("DatabaseOnSdcard", false) == true) {
                                    path = Environment.getExternalStorageDirectory()
                                } else {
                                    path = getApplicationContext().getFilesDir()
                                }

                                try {
                                    copyDatabse(
                                        filePath,//Environment.getExternalStorageDirectory() + "/" + newValue.toString(),
                                        path.toString() + "/" + BuildConfig.DATABASE_NAME
                                    )
                                } catch (e :FileNotFoundException) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (e : IOException) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Toast.makeText(
                                    this@SettingsActivity,
                                    "Database file moved! You must restart this app.", Toast.LENGTH_LONG
                                ).show()
                                setResult(400)
                                finish()
                            }
                        }).show()
                }
            }

            2 -> {
                //databaseFileMerge
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    AlertDialog.Builder(this@SettingsActivity)
                        .setCancelable(false)
                        .setPositiveButton(R.string.Yes, object:DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                val fileToImport = filePath
                                val otherDB = DataBase(applicationContext, fileToImport)
                                otherDB.open()
                                dataBase!!.MergeDatabase(otherDB)
                                otherDB.close()
                            }

                        })

                }
            }
        }

    }
}