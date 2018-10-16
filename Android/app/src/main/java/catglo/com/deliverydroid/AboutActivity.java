package catglo.com.deliverydroid;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class AboutActivity extends AppCompatActivity {

      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            //int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            ((TextView)findViewById(R.id.version)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

       // String legalString = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
       // ((TextView)findViewById(R.id.legal)).setText(legalString);



    }


}
