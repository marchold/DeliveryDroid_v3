package catglo.com.deliverydroid.backup

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView


import catglo.com.deliveryDatabase.DataBase
import catglo.com.deliverydroid.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField


import java.io.*
import java.util.ArrayList
import java.util.Date
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.CreateFileActivityOptions
import com.google.android.gms.tasks.*
import kotlinx.android.synthetic.main.backup_to_google_drive_activity.*

class GoogleDriveBackupRestoreActivity : AppCompatActivity() {
    private var mDriveClient: DriveClient? = null
    private var mDriveResourceClient: DriveResourceClient? = null
    private var mOpenItemTaskSource: TaskCompletionSource<DriveId>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.backup_to_google_drive_activity)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        signIn()
        backup?.setOnClickListener { saveFileToDrive() }
        restore?.setOnClickListener { restoreBackup() }
    }

    private fun signIn() {
        Log.i("DD", "Start sign in")
        val googleSignInClient = buildGoogleSignInClient()
        startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE)
            .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }


    @Throws(FileNotFoundException::class, IOException::class)
    internal fun copyDatabse(to: String, from: String) {
        val input = FileInputStream(to)
        val myOutput = FileOutputStream(from)

        // transfer bytes from the inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        while ((length = input.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length)
        }

        // Close the streams
        myOutput.flush()
        myOutput.close()
        input.close()
    }


    private fun saveFileToDrive() {

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var path: File
        if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
            path = Environment.getExternalStorageDirectory()
        } else {
            path = applicationContext.filesDir
        }
        path = File(path, DataBase.DATABASE_NAME)


        mDriveResourceClient!!
            .createContents()
            .continueWithTask { task -> createFileIntentSender(task.result!!, path) }
            .addOnFailureListener { e -> Log.w("DD", "Failed to create new contents.", e) }
    }

    /**
     * Creates an [IntentSender] to start a dialog activity with configured [ ] for user to create a new photo in Drive.
     */
    private fun createFileIntentSender(driveContents: DriveContents, dbFileToBackup: File): Task<Void>? {
        try {
            Log.i("DD", "New contents created.")

            val outputStream = driveContents.outputStream
            val input = FileInputStream(dbFileToBackup)


            val buffer = ByteArray(1024)
            var length: Int
            while ((length = input.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length)
            }

            input.close()


            // Create the initial metadata - MIME type and title.
            // Note that the user will be able to change the title later.
            val metadataChangeSet = MetadataChangeSet.Builder()
                .setMimeType("delivery/mysql")
                .setTitle("Delivery Droid Database Backup")
                .build()
            // Set up options to configure and display the create file activity.
            val createFileActivityOptions = CreateFileActivityOptions.Builder()
                .setInitialMetadata(metadataChangeSet)
                .setInitialDriveContents(driveContents)
                .build()

            return mDriveClient!!
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith { task ->
                    startIntentSenderForResult(task.result, REQUEST_CODE_CREATOR, null, 0, 0, 0)
                    null
                }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                Log.i("DD", "Sign in request code")
                // Called after user is signed in.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DD", "Signed in successfully.")
                    // Use the last signed in account here since it already have a Drive scope.
                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                }
            }
        }
    }

    private fun pickItem(openOptions: OpenFileActivityOptions): Task<DriveId> {
        mOpenItemTaskSource = TaskCompletionSource()
        mDriveClient!!
            .newOpenFileActivityIntentSender(openOptions)
            .continueWith({ task ->
                startIntentSenderForResult(
                    task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0
                )
                null
            } as Continuation<IntentSender, Void>)
        return mOpenItemTaskSource!!.task
    }

    protected fun restoreBackup() {
        val openOptions = OpenFileActivityOptions.Builder()
            .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "delivery/mysql"))
            .setActivityTitle(getString(R.string.BackupRestoreData))
            .build()
        pickItem(openOptions).addOnSuccessListener { getDatabaseFile() }.addOnFailureListener { }
    }

    private fun getDatabaseFile() {
        // [START drive_android_open_file]
        val openFileTask = mDriveResourceClient!!.openFile(file, DriveFile.MODE_READ_ONLY)
        // [END drive_android_open_file]
        // [START drive_android_read_contents]
        openFileTask
            .continueWithTask { task ->
                val contents = task.result
                // Process contents...
                // [START_EXCLUDE]
                // [START drive_android_read_as_string]
                BufferedReader(InputStreamReader(contents!!.inputStream)).use { reader ->
                    val builder = StringBuilder()
                    var line: String
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n")
                    }
                    //       showMessage(getString(R.string.content_loaded));
                    //       mFileContents.setText(builder.toString());
                }
                // [END drive_android_read_as_string]
                // [END_EXCLUDE]
                // [START drive_android_discard_contents]
                val discardTask = mDriveResourceClient!!.discardContents(contents)
                // [END drive_android_discard_contents]
                discardTask
            }
            .addOnFailureListener { e ->
                // Handle failure
                // [START_EXCLUDE]
                //  Log.e(TAG, "Unable to read contents", e);
                //  showMessage(getString(R.string.read_failed));
                //  finish();
                // [END_EXCLUDE]
            }
        // [END drive_android_read_contents]
    }

    companion object {
        private val REQUEST_CODE_CREATOR = 1
        private val REQUEST_CODE_SIGN_IN = 2
        private val REQUEST_CODE_OPEN_ITEM = 3
    }

}
