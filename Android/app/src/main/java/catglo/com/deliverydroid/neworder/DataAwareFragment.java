package catglo.com.deliverydroid.neworder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;


/**
 * Created by goblets on 2/22/14.
 */
abstract public class DataAwareFragment extends Fragment {
    private BroadcastReceiver receiver;

    @Override
    public void onResume(){
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATED_DELIVERY_DROID_DATABASE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onDataChanged();
            }
        };
        getActivity().registerReceiver(receiver, filter);

    }

    @Override public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    protected boolean settingInitialValues = false;
    public final void onDataChanged(){
        settingInitialValues = true;
        onDataChangedHandler();
        settingInitialValues =  false;
    }
    protected abstract void onDataChangedHandler();

}
