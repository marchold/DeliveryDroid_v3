package catglo.com.deliverydroid.backup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliverydroid.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;


public class GoogleDriveBackupRestoreActivity
        extends Activity
      //  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DriveFolder.OnChildrenRetrievedCallback, DriveFile.OnContentsOpenedCallback
{

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private GoogleApiClient mGoogleApiClient;
    private View backup;
    private View restore;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_to_google_drive_activity);

        backup = findViewById(R.id.backup);
        restore = findViewById(R.id.restore);
        listView = (ListView)findViewById(R.id.listView1);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(GoogleDriveBackupRestoreActivity.this)
                .addOnConnectionFailedListener(GoogleDriveBackupRestoreActivity.this)
                .build();

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFileToDrive();
            }
        });

        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query.Builder query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.MIME_TYPE, "delivery/mysql"));

//                Drive.DriveApi
             //   Drive.DriveApi.query(mGoogleApiClient, query.build()).addResultCallback(GoogleDriveBackupRestoreActivity.this);

                
            }
        });

        //       Drive.DriveApi.fetchDriveId(mGoogleApiClient, EXISTING_FILE_ID)
         //               .setResultCallback(idCallback);
          //  }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appopriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("MARC", "HAHA connected");
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i("MARC", "Creating new contents.");
   /*     final Bitmap image = null;
        Drive.DriveApi.newContents(mGoogleApiClient).addResultCallback(new DriveApi.OnNewContentsCallback() {
            @Override
            public void onNewContents(DriveApi.ContentsResult result) {
                // If the operation was not successful, we cannot do anything
                // and must
                // fail.
                if (!result.getStatus().isSuccess()) {
                    Log.i("MARC", "Failed to create new contents.");
                    return;
                }
                // Otherwise, we can write our data to the new contents.
                Log.i("MARC", "New contents created.");
                // Get an output stream for the contents.
                OutputStream outputStream = result.getContents().getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                File path = null;
                if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
                    path = Environment.getExternalStorageDirectory();
                } else {
                    path = getApplicationContext().getFilesDir();
                }

                try {

                    final FileInputStream input = new FileInputStream(path.toString() + "/" + DataBase.DATABASE_NAME);

                    final byte[] buffer = new byte[1024];
                    int length;
                    while ((length = input.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    input.close();

                } catch (IOException e) {
                    Log.i("MARC", "Unable to write file contents.");
                }


                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("delivery/mysql").setTitle("Delivery Droid Database Backup").build();



                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialContents(result.getContents())
                        .build(mGoogleApiClient);

                try {

                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);

                } catch (IntentSender.SendIntentException e) {

                    Log.i("MARC", "Failed to launch file chooser.");
                }
            }
        });
*/

    }
    /*

    @Override
    public void onOpen(final DriveApi.ContentsResult contentsResult) {

        DialogFragment dialogFragment = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle bundle){
                return new AlertDialog.Builder(GoogleDriveBackupRestoreActivity.this)
                    .setMessage(R.string.databaseUseWarning)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {try {
                            InputStream is = contentsResult.getContents().getInputStream();

                            FileOutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(),"dd_restore.SQLite"));
                            byte[] buffer = new byte[2048];
                            int avail;
                            while ((avail = is.available())>0){
                                if (avail>2048)avail=2048;
                                is.read(buffer,0,avail);
                                fileOutputStream.write(buffer,0,avail);
                            }
                            is.close();
                            fileOutputStream.close();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            File path = null;
                            if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
                                path = Environment.getExternalStorageDirectory();
                            } else {
                                path = getApplicationContext().getFilesDir();
                            }

                            copyDatabse(Environment.getExternalStorageDirectory() + "/dd_restore.SQLite",
                                    path.toString() + "/" + DataBase.DATABASE_NAME);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }}
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).create();
            }
        };
        dialogFragment.show(getFragmentManager(),"Confirm Restore From Google Drive");
    }
*/

    void copyDatabse(final String to, final String from) throws FileNotFoundException, IOException {
        final FileInputStream input = new FileInputStream(to);
        final OutputStream myOutput = new FileOutputStream(from);

        // transfer bytes from the inputfile to the outputfile
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        input.close();
    }


    static class GoogleDriveDeliveryDroidFile {
        public String title;
        public DriveId id;
        public Date date;

        public String toString(){
            return title;
        }
    }
    ArrayList<GoogleDriveDeliveryDroidFile> fileList;

    @Override
    public void onChildrenRetrieved(DriveApi.MetadataBufferResult result) {
        if (!result.getStatus().isSuccess()) {
            //showMessage("Problem while retrieving files");
            return;
        }
        //mResultsAdapter.clear();
        MetadataBuffer metadataBuffer = result.getMetadataBuffer();
        //mResultsAdapter.append();
        //showMessage("Successfully listed files.");
        fileList = new ArrayList<GoogleDriveDeliveryDroidFile>();
        for (Metadata metadata : metadataBuffer){
            GoogleDriveDeliveryDroidFile file = new GoogleDriveDeliveryDroidFile();
            file.title = metadata.getTitle();
            file.id = metadata.getDriveId();
            file.date = metadata.getCreatedDate();
            fileList.add(file);
        }
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new ArrayAdapter<GoogleDriveDeliveryDroidFile>(this,android.R.layout.simple_list_item_1,fileList));

     /*   listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, fileList.get(position).id);
                driveFile.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).addResultCallback(GoogleDriveBackupRestoreActivity.this);
            }
        });
        */
    }
}
