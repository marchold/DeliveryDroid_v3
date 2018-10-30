package catglo.com.deliverydroid.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import catglo.com.deliveryDatabase.DataBase;
import catglo.com.deliveryDatabase.StreetList;
import catglo.com.deliveryDatabase.ZipCode;
import catglo.com.deliverydroid.AboutActivity;
import catglo.com.deliverydroid.BuildConfig;
import catglo.com.deliverydroid.R;
import catglo.com.deliverydroid.backup.GoogleDriveBackupRestoreActivity;
import catglo.com.widgets.TwoLinesListPreference;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.regex.Pattern;



public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	EditTextPreference			databaseFileCopy;
	CheckBoxPreference			databaseOnSdcard;

	SharedPreferences			prefs;
	private PreferenceScreen	zipCodePrefs;
	private EditTextPreference	newZipCode;
	Pattern						pattern;
	private StreetList streetList;
	DataBase dataBase;

	private Editor pedit;
	private EditTextPreference databaseFileMerge;
	private EditTextPreference databaseFileUse;
	private Preference rebuildStreetList;
	private EditTextPreference centrPoint_lat_s;
	private EditTextPreference centrPoint_lng_s;
	private Preference rebuildRegionsList;
	private TwoLinesListPreference navigationListPref;
    private Preference aboutPref;
    private Preference backupToGoogleDrive;

    @Override
	protected void onDestroy() {
		dataBase.close();
		super.onDestroy();
	}
	
	 @Override
	  public void onStart() {
	    super.onStart();

	   // EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    
	   // EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }
	
	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
	

	    if (dataBase == null) {
        	dataBase = new DataBase(getApplicationContext());
        	dataBase.open();
        }
	    
	    
	    navigationListPref = (TwoLinesListPreference)getPreferenceScreen().findPreference("navigationIntent");
	    navigationListPref.setEntriesSubtitles(getResources().getTextArray(R.array.navigation_options_summary));
	    
	    
//		InstructionsPreference instructionsAltPay = (InstructionsPreference) getPreferenceScreen().findPreference("instructionsAltPay");
//		instructionsAltPay.
		
		streetList = StreetList.LoadState(getApplicationContext());
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pedit = prefs.edit();
		pedit.putString("centrPoint_lat_s", prefs.getFloat("centrPoint_lat", 0)+"");
		pedit.putString("centrPoint_lng_s", prefs.getFloat("centrPoint_lng", 0)+"");
		pedit.commit();
		
		centrPoint_lat_s = (EditTextPreference) getPreferenceScreen().findPreference("centrPoint_lat_s");
		centrPoint_lng_s = (EditTextPreference) getPreferenceScreen().findPreference("centrPoint_lng_s");
		centrPoint_lat_s.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			public boolean onPreferenceChange(Preference preference,Object newValue) {
				pedit = prefs.edit();
				float value;
				try {
					value = Float.parseFloat(newValue.toString());
				} catch (NumberFormatException e){
					value = 100;
					Toast.makeText(getApplicationContext(), R.string.error_decoding_number, Toast.LENGTH_LONG).show();
				}
				pedit.putFloat("centrPoint_lat", value);
				pedit.putString("centrPoint_lat_s", newValue.toString());
				pedit.commit();
				return false;
			}});
		
		centrPoint_lng_s.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				pedit = prefs.edit();
				float value;
				try {
					value = Float.parseFloat(newValue.toString());
				} catch (NumberFormatException e){
					value = 100;
					Toast.makeText(getApplicationContext(), R.string.error_decoding_number, Toast.LENGTH_LONG).show();
				}
			
				pedit.putFloat("centrPoint_lng", value );
				pedit.putString("centrPoint_lng_s", newValue.toString());
				pedit.commit();
				return false;
			}});
		//SettingsStoreAddress storeAddress = (SettingsStoreAddress) getPreferenceScreen().findPreference("storeAddress");
		
		
	    {
			databaseFileCopy = (EditTextPreference) getPreferenceScreen().findPreference("DatabaseFileCopy");
			databaseOnSdcard = (CheckBoxPreference) getPreferenceScreen().findPreference("DatabaseOnSdcard");
			//databaseUpload   = (CheckBoxPreference) getPreferenceScreen().findPreference("DatabaseUpload");
			databaseFileMerge = (EditTextPreference) getPreferenceScreen().findPreference("DatabaseFileMerge");
			databaseFileUse = (EditTextPreference) getPreferenceScreen().findPreference("DatabaseFileUse");
			rebuildStreetList = (Preference) getPreferenceScreen().findPreference("rebuildStreetList");
			rebuildRegionsList = (Preference)getPreferenceScreen().findPreference("rebuildRegionsList");
            aboutPref = (Preference)getPreferenceScreen().findPreference("aboutPref");
            backupToGoogleDrive = (Preference)getPreferenceScreen().findPreference("backupToGoogleDrive");

/*
			rebuildStreetList.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				public boolean onPreferenceClick(Preference preference) {
					final Iterator<ZipCode> keys = StreetList.zipCodes.iterator();
					while (keys.hasNext()) {
						final ZipCode z = keys.next();
						z.state = ZipCode.STATE_NEEDS_LOOKUP;
					}
					final ProgressDialog dialog = ProgressDialog.show(SettingsActivity.this, "", 
	                        "Downloading. Please wait...", false);
					dialog.show();
					Thread t = new Thread(new Runnable(){public void run() {
						streetList.run();
						runOnUiThread(new Runnable(){public void run() {
							dialog.dismiss();
						}});
					}});
					t.start();
					
					return false;
				}

			});
			
			rebuildRegionsList.setOnPreferenceClickListener(new OnPreferenceClickListener(){public boolean onPreferenceClick(Preference arg0) {
				final ProgressDialog dialog = ProgressDialog.show(SettingsActivity.this, "", 
                        "Waiting for GPS lock. Please wait...", false);
				dialog.show();
				
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_LOW);
				locationManager.requestSingleUpdate(criteria, new LocationListener(){
					public void onLocationChanged(Location location) {
						
						final Editor prefEditor = prefs.edit();
						prefEditor.putFloat("centrPoint_lat", (float) location.getLatitude());
						prefEditor.putFloat("centrPoint_lng", (float) location.getLongitude());
						prefEditor.putString("centrPoint_lat_s", location.getLatitude()+"");
						prefEditor.putString("centrPoint_lng_s", location.getLongitude()+"");	
									
						dialog.setTitle("Downloading, Please Wait...");
						
						new WebServiceUpdateLocalePreferances(prefs,(float) location.getLatitude(),(float) location.getLongitude(),new WebServiceUpdateLocalePreferances.LocalityListener(){public void result(LocalityInfo info) {
							prefEditor.putString("addressFilterComponents", info.filter);
							prefEditor.commit();
							runOnUiThread(new Runnable(){public void run() {
								dialog.dismiss();
							}});
						}}).lookup();
						
					}
					public void onProviderDisabled(String arg0) {
						Toast.makeText(getApplicationContext(), "Error No GPS", Toast.LENGTH_LONG).show();						
					}
					public void onProviderEnabled(String arg0) {}
					public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
				}, null);
				
				return false;
			}});
*/
            aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getApplicationContext(), AboutActivity.class);
                    startActivity(i);
                    return false;
                }
            });

            backupToGoogleDrive.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getApplicationContext(), GoogleDriveBackupRestoreActivity.class);
                    startActivity(i);
                    return false;
                }
            });

			databaseFileCopy.setPositiveButtonText("Copy it!");
			databaseFileCopy.setNegativeButtonText("Don't Copy");
			

			databaseFileMerge.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				public boolean onPreferenceChange(final Preference preference, final Object newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
					builder.setMessage(R.string.databaseMergeWarning)
					       .setCancelable(false)
					       .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	    String fileToImport = newValue.toString();
									DataBase otherDB = new DataBase(getApplicationContext(),fileToImport);
									otherDB.open();
									dataBase.MergeDatabase(otherDB);
									otherDB.close(); 
					           }
					       })
					       .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
	
					return true;
				}
			});
			
			databaseFileUse.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(final Preference preference, final Object newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
					builder.setMessage(R.string.databaseUseWarning)
					       .setCancelable(false)
					       .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
									File path = null;
									if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
										path = Environment.getExternalStorageDirectory();
									} else {
										path = getApplicationContext().getFilesDir();
									}
									
									try {
										copyDatabse(Environment.getExternalStorageDirectory() + "/" + newValue.toString(),
												path.toString() + "/" + BuildConfig.DATABASE_NAME);
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
											"Database file moved! You must restart this app.", Toast.LENGTH_LONG);
									toast.show();
									setResult(400);
									finish();
					           }
					       })
					       .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});
			
			databaseOnSdcard.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(final Preference preference, final Object newValue) {
					try {
						if ((Boolean) newValue == true) {
							//copy to sdcard
							copyDatabse(getFilesDir() + "/" + BuildConfig.DATABASE_NAME,
									Environment.getExternalStorageDirectory() + "/" + BuildConfig.DATABASE_NAME);

						} else {
							//copy from sdcard
							copyDatabse(Environment.getExternalStorageDirectory() + "/" + BuildConfig.DATABASE_NAME,
									getFilesDir() + BuildConfig.DATABASE_NAME);

						}
						final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
								"Database file moved! You must restart this app.", Toast.LENGTH_LONG);
						toast.show();
						setResult(400);
						finish();

					} catch (final FileNotFoundException e) {
						final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
								"DID NOT MOVE DATABASE FILE! (file not found error)"+getFilesDir() + "/" + BuildConfig.DATABASE_NAME+"   "+Environment.getExternalStorageDirectory() + "/" + BuildConfig.DATABASE_NAME,
								Toast.LENGTH_LONG);
						toast.show();
						return false;
					} catch (final IOException e) {
						final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
								"DID NOT MOVE DATABASE FILE! You may have insufficent space on your sdcard.",
								Toast.LENGTH_LONG);
						toast.show();
						return false;
					}
					return true;
				}

			});
			


			databaseFileCopy.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(final Preference preference, final Object newValue) {
					if (prefs.getBoolean("DatabaseOnSdcard", false) == true) {
						final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
								"Can not copy databse already on sdcard", Toast.LENGTH_LONG);
						toast.show();
						return false;
					} else {
						final String val = newValue.toString();
						try {
							copyDatabse(getFilesDir() + "/" + BuildConfig.DATABASE_NAME,
									Environment.getExternalStorageDirectory() + "/" + val);
							final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
									"successfully copied database to "+Environment.getExternalStorageDirectory() +"/"+ val, Toast.LENGTH_LONG);
							toast.show();
						} catch (final FileNotFoundException e) {
							final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
									"DID NOT BACKUP DATABASE FILE! You may have specified an invalid file name",
									Toast.LENGTH_LONG);
							toast.show();
							return false;
						} catch (final IOException e) {
							final Toast toast = Toast.makeText(SettingsActivity.this.getApplicationContext(),
									"DID NOT BACKUP DATABASE FILE! You may have insufficent space on your sdcard.",
									Toast.LENGTH_LONG);
							toast.show();
							return false;
						}
					}
					return true;
				}
			});
		}

		newZipCode = (EditTextPreference) getPreferenceScreen().findPreference("NewZipCode");
		newZipCode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference, final Object newValue) {
				/*String zipCode = "";
				//try {
					zipCode = newValue.toString();
					StreetList.zipCodes.insert(new ZipCode(zipCode, ZipCode.STATE_NEEDS_LOOKUP,
							streetList.client));
					addZipPref(new String("" + zipCode));
					streetList.saveURLState();
					return true;
				//} catch (final Exception e) {
				//
				//}
				*/
				return false;
			}
		});
		newZipCode.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference) {
				/*Iterator<ZipCode> e = StreetList.zipCodes.iterator();
				float minimumDistance = Float.MAX_VALUE;
				ZipCode closestUnusedZipcode=null;
				while (e.hasNext()){
					ZipCode z = StreetList.zipCodes.get(""+e.next().zipCode);
					if (z != null && z.state==ZipCode.STATE_NOT_IN_DELIVERY_AREA && z.distance < minimumDistance){
						minimumDistance=z.distance;
						closestUnusedZipcode=z;
					}
				}
				final EditTextPreference ps = (EditTextPreference) getPreferenceScreen().findPreference("NewZipCode");
				if (closestUnusedZipcode!=null){
					ps.getEditText().setText(""+closestUnusedZipcode.zipCode);
					ps.setText(""+closestUnusedZipcode.zipCode);
					return true;
				} else {
					//TODO: we should search the net for more close by zip codes.
					ps.getEditText().setText("");
					ps.setText("");
				}*/
				return false;
			}	
		});

		zipCodePrefs = (PreferenceScreen) getPreferenceScreen().findPreference("SetUpZip");
		
		final Iterator<ZipCode> keys = StreetList.zipCodes.iterator();
		while (keys.hasNext()) {
			final ZipCode z = keys.next();
			if (z.state == ZipCode.STATE_LOOKUP_SUCCESS || z.state == ZipCode.STATE_NEEDS_LOOKUP) {
				final String zipCode = ""+z.zipCode;
				addZipPref(zipCode);
			}
		}
		
	}

	private void addZipPref(final String zipCode) {
		// Edit text preference
		final EditTextPreference editTextPref = new EditTextPreference(this);
		editTextPref.setDialogTitle("Edit Zip Code");
		editTextPref.setKey(zipCode);
		editTextPref.setTitle(zipCode);
		editTextPref.setSummary("One of your dilevery area zip codes");
		editTextPref.setText(zipCode);
		final Editor pedit = prefs.edit();
		pedit.putString(zipCode, zipCode);
		pedit.commit();

		zipCodePrefs.addPreference(editTextPref);
		editTextPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final EditTextPreference e = (EditTextPreference) preference;
				e.setText(e.getKey());
				return false;
			}
		});



		editTextPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference, final Object newValue) {

			/*	final String oldZip = preference.getKey();
				final String newZip = newValue.toString();
				StreetList.zipCodes.remove(oldZip);
				if (newZip.matches("^[0-9]{5}$")) {
					StreetList.zipCodes.insert(new ZipCode(newZip,ZipCode.STATE_NEEDS_LOOKUP, streetList.client));
					final EditTextPreference ps = (EditTextPreference) getPreferenceScreen().findPreference(oldZip);
					ps.setKey(newZip);
					ps.setTitle(newZip);
					ps.setText(newZip);
					streetList.saveURLState();
				} else {
					finish();
					final Toast toast = Toast.makeText(getApplicationContext(), "Deleted Zip Code " + oldZip,
							Toast.LENGTH_LONG);
					toast.show();
				}*/
				return true;
			}

		});
	}
	
	
	
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

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (key.startsWith("Zip")) {
			streetList.exit = true;

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			streetList = StreetList.LoadState(this);
			streetList.addZipCode(prefs.getString(key, ""));
			streetList.start();
		}

	}

}