package catglo.com.deliverydroid.settings;

import android.app.Activity;

import catglo.com.deliverydroid.widgets.ActivityHostFragment;

public class Settings_ListOptions_Wrapper extends ActivityHostFragment {

    public Settings_ListOptions_Wrapper() {
        super();
    }


    @Override
    protected Class<? extends Activity> getActivityClass() {
        return Settings_ListOptions_Activity.class;
    }

}
