package catglo.com.deliverydroid.neworder;

import android.annotation.TargetApi;
import android.app.*;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;


import android.view.View;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.DeliveryDroidBaseActionBarActivity;
import catglo.com.deliverydroid.R;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

public class NewOrderActivity extends DeliveryDroidBaseActionBarActivity
                                 implements androidx.appcompat.app.ActionBar.TabListener,
                                            ButtonPadFragment.ButtonPadNextListener {



    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    public Order order = new Order();
    private View tabletPane;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onNextButtonPressed() {
        int i = getActionBar().getSelectedNavigationIndex();
        try {
            getActionBar().selectTab(getActionBar().getTabAt(i+1));
        } catch (IndexOutOfBoundsException e){
            //TODO: maybe focus whatever seems like the next logical thing in the last screen frag on tablet
        }
    }



    public enum Pages {
        number,price,phone,address,time,order;
    };
    ArrayList<Pages> viewPagerPages = new ArrayList<Pages>(6);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_new_order_acticity);



        //If one of these exists its a tablet
        tabletPane = findViewById(R.id.orderDetailsContainer);

        boolean useOrderNumberScreen = sharedPreferences.getBoolean("useOrderNumberScreen", true);
        boolean useCostScreen        = sharedPreferences.getBoolean("usePriceScreen", true);
        boolean usePhoneScreen       = sharedPreferences.getBoolean("usePhoneScreen", true);
        boolean useAddressScreen     = sharedPreferences.getBoolean("useAddressScreen", true);
        boolean useTimeScreen        = sharedPreferences.getBoolean("useTimeScreen", false);

        if (useOrderNumberScreen) viewPagerPages.add(Pages.number);
        if (useCostScreen)        viewPagerPages.add(Pages.price);
        if (usePhoneScreen)       viewPagerPages.add(Pages.phone);
        if (useAddressScreen)     viewPagerPages.add(Pages.address);
        if (useTimeScreen)        viewPagerPages.add(Pages.time);
        if (tabletPane==null)     viewPagerPages.add(Pages.order);
        else {
            NewOrderLastScreenFragment lastScreenFragment = new NewOrderLastScreenFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.orderDetailsContainer,lastScreenFragment,"order_details_fragment")
                    .commit();
        }


        // Set up the action bar.
        final androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(androidx.appcompat.app.ActionBar.NAVIGATION_MODE_TABS);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        actionBar.setBackgroundDrawable(getDrawable(R.color.chea_action_bar_bkgd));
        actionBar.setStackedBackgroundDrawable(getDrawable(R.color.chea_action_bar_bkgd));

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),viewPagerPages);


        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabSelected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {
        int position = tab.getPosition();
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(androidx.appcompat.app.ActionBar.Tab tab, androidx.fragment.app.FragmentTransaction ft) {

    }

    public int getFragmentIndex(Pages fragmentPage){
        int index;
        for (index = 0; index < viewPagerPages.size(); index++){
            if (viewPagerPages.get(index) == fragmentPage){
                return index;
            }
        }
        return -1;
    }

    public DataAwareFragment getFragment(Pages fragmentPage){
        if (   fragmentPage==Pages.order
            && tabletPane!=null){
            return (DataAwareFragment)getSupportFragmentManager().findFragmentByTag("order_details_fragment");
        }
        int index = getFragmentIndex(fragmentPage);
        if (index < 0){
            return null;
        }
        return (DataAwareFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + index);
    }


    static public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Pages> viewPagerPages;
        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm, ArrayList<Pages> viewPagerPages) {
            super(fm);
            this.viewPagerPages = viewPagerPages;

        }

        @Override
        public Fragment getItem(int position) {
            Pages page =  viewPagerPages.get(position);
            switch (page) {
                case number:
                    return new OrderNumberButtonPadFragment();
                case price:
                    return new OrderPriceButtonPadFragment();
                case phone:
                    return new PhoneNumberEntryFragment();
                case address:
                    return new AddressEntryFragment();
                case time:
                    return new TimeFragment();
                case order:
                    return new NewOrderLastScreenFragment();

            }
            throw new IllegalStateException("No fragment for page");
        }

        @Override
        public int getCount() {
           return  viewPagerPages.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            Pages page =  viewPagerPages.get(position);
            switch (page) {
                case number:
                    return  "number".toUpperCase(l);
                case price:
                    return  "price".toUpperCase(l);
                case phone:
                    return  "phone".toUpperCase(l);
                case address:
                    return  "address".toUpperCase(l);
                case time:
                    return  "time".toUpperCase(l);
                case order:
                    return  "order".toUpperCase(l);
            }
            return null;
        }
    }

    void askGoBack(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NewOrderActivity.this);
        builder.setMessage(R.string.ba_to_home_from_new_order)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).show();
    }

    @Override
    public void onBackPressed(){
        askGoBack();
    }


}
