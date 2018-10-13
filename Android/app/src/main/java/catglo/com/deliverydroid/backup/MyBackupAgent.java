package catglo.com.deliverydroid.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import catglo.com.deliveryDatabase.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by goblets on 9/6/13.
 */
public class MyBackupAgent extends BackupAgentHelper {

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String DATABASE_BACKUP_KEY = "database";


    class DatabaseBackupHelper implements BackupHelper {
        @Override
        public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
            Log.i("backup","performing backup of database");

            //Get the timestamp of the last backup
            FileInputStream fileInputStream = new FileInputStream(oldState.getFileDescriptor());
            DataInputStream in = new DataInputStream(fileInputStream);
            long stateModified = 0;
            try {
                stateModified = in.readLong();
            } catch (IOException e) {
            	Log.w("backup","failed to read backup state version");
            }

            //Get the data that has changed since the timestamp

            DataBase dataBase = new DataBase(getApplicationContext());
            dataBase.open();
            long backupTimestamp = dataBase.lastModified().getTimeInMillis();
            ArrayList<TableValues> tableFields = dataBase.getBackupStrings(stateModified);

            Log.i("backup","Database has "+tableFields.size()+" tables to back up");


            ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
            ObjectOutputStream outWriter = null;
            try {
                //Write Data
                outWriter = new ObjectOutputStream(bufStream);
                outWriter.writeObject(tableFields);
                byte[] buffer = bufStream.toByteArray();
                int len = buffer.length;
                data.writeEntityHeader(DATABASE_BACKUP_KEY+":"+DATABASE_BACKUP_KEY, len);
                data.writeEntityData(buffer, len);

                //Write Data State Descriptors
                FileOutputStream fileOutputStream = new FileOutputStream(newState.getFileDescriptor());
                DataOutputStream out = new DataOutputStream(fileOutputStream);
                out.writeLong(backupTimestamp);

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i("backup","Completed database backup with timestamp of "+backupTimestamp);

            
            dataBase.close();
        }

        @Override
        public void restoreEntity(BackupDataInputStream data) {
            Log.i("backup","Restoring Data Backup on "+data.getKey()+" of "+(data.size()));
           
            DataBase dataBase = new DataBase(getApplicationContext());
            dataBase.open();

            try {
                int dataSize = data.size();
                byte[] dataBuf = new byte[dataSize];
                data.read(dataBuf, 0, dataSize);
                ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
                ObjectInputStream objStream = new ObjectInputStream(baStream);

                @SuppressWarnings("unchecked")
				ArrayList<TableValues> tableValues = (ArrayList<TableValues>)objStream.readObject();

                Log.i("backup","Restoring "+tableValues.size()+" tables ");
               
                int rows = 0;
                for (TableValues row : tableValues) {
                    dataBase.write(row);
                    rows+=row.fieldValues.size();
                }

                Log.i("backup","Completed restore of "+rows+" rows of data");

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("backup",e.getLocalizedMessage());
                throw new IllegalStateException(e.getLocalizedMessage());
            } finally {
            	dataBase.close();
            }
        }

        @Override
        public void writeNewStateDescription(ParcelFileDescriptor newState) {
            Log.i("backup","Writing state descriptor");
            DataBase dataBase = new DataBase(getApplicationContext());
            dataBase.open();
            try {
                long backupTimestamp = dataBase.lastModified().getTimeInMillis();
                FileOutputStream fileOutputStream = new FileOutputStream(newState.getFileDescriptor());
                DataOutputStream out = new DataOutputStream(fileOutputStream);
                out.writeLong(backupTimestamp);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            	dataBase.close();
            }
        }
    }

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {

        String prefFile = getApplicationContext().getPackageName()+"_preferences";
        SharedPreferencesBackupHelper preferencesBackupHelper = new SharedPreferencesBackupHelper(this, prefFile);
        addHelper(PREFS_BACKUP_KEY, preferencesBackupHelper);

        DatabaseBackupHelper databaseBackupHelper = new DatabaseBackupHelper();
        addHelper(DATABASE_BACKUP_KEY, databaseBackupHelper);

    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized (DataBase.class) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized (DataBase.class) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

}