package catglo.com.deliverydroid.neworder;

import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import catglo.com.deliveryDatabase.Order;
import catglo.com.deliverydroid.R;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by goblets on 2/21/14.
 */
public class PhoneNumberEntryFragment extends ButtonPadFragment {
    private ArrayList<Order> currentOrderList;
    private ArrayList<String> currentPrefixStrings;

    @Override
    public ListAdapter getListAdapter() {
        return null;
    }

    private boolean foundAddressMatch = false;

    @Override
    public void onResume(){
        super.onResume();

        NewOrderActivity activity = (NewOrderActivity)getActivity();
        if (activity.order.phoneNumber==null || activity.order.phoneNumber.length()==0){

            ArrayList<String> phoneNumberStrings = activity.dataBase.getRecentPhoneNumbers("");

            // The idea here is to find the most likely prefixes.
            final HashMap<String,Integer> prefixes  = new HashMap<String, Integer>(4+8+16+32+64);

            for (String phoneNumberString : phoneNumberStrings) {
                if (phoneNumberString != null) {
                    switch (phoneNumberString.length()) {
                        default:
                        case 6:
                            addIncrement(prefixes, phoneNumberString.substring(0, 6));
                        case 5:
                            addIncrement(prefixes, phoneNumberString.substring(0, 5));
                        case 4:
                            addIncrement(prefixes, phoneNumberString.substring(0, 4));
                        case 3:
                            addIncrement(prefixes, phoneNumberString.substring(0, 3));
                        case 2:
                            addIncrement(prefixes, phoneNumberString.substring(0, 2));
                        case 1:
                        case 0:
                    }
                }
            }

            //Purge and keys that are prefixes for other keys with equal probability
            ArrayList<String> purgeKeys = new ArrayList<String>();
            for (String key : prefixes.keySet()){
                Integer value = prefixes.get(key);
                int len = key.length();
                while (len > 1) {
                    len--;
                    String substr = key.substring(0,len);
                    if (prefixes.get(substr)!=null && prefixes.get(substr).equals(value)) {
                        purgeKeys.add(substr);
                    }
                }
            }
            for (String purgeKey : purgeKeys){
                prefixes.remove(purgeKey);
            }


            currentPrefixStrings = new ArrayList<String>(prefixes.size());
            for (String key : prefixes.keySet()){
                currentPrefixStrings.add(key);
            }

            Collections.sort(currentPrefixStrings, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    Integer lhv = prefixes.get(lhs);
                    Integer rhv = prefixes.get(rhs);
                    return rhv - lhv;
                }
            });

            PhonePrefixListAdapter adapter = new PhonePrefixListAdapter(getActivity(),R.layout.simple_auto_resize_list_item, currentPrefixStrings);
            list.setAdapter(adapter);

        } else {
            edit.setText(activity.order.phoneNumber);
        }




    }


    @Override
    protected void onDataChangedHandler() {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        edit.setText(activity.order.phoneNumber);
    }

    @Override
    public void onTextChanged(String newText) {
        NewOrderActivity activity = (NewOrderActivity)getActivity();

        //Get the phone number from the activity
        activity.order.phoneNumber = edit.getText().toString();

        //Let the last screen fragment know about the update
        DataAwareFragment fragment = activity.getFragment(NewOrderActivity.Pages.order);
        if (fragment!=null) fragment.onDataChanged();

        //Look up the phone number in the database to populate list
        currentOrderList = activity.dataBase.findOrdersForPhoneNumber(Order.phoneNumbersOnly(newText));
        PhoneOrderListAdapter adapter = new PhoneOrderListAdapter(getActivity(), R.layout.simple_auto_resize_list_item, currentOrderList);
        list.setAdapter(adapter);

    }

    private void addIncrement(HashMap<String, Integer> five, String subString){
        Integer i = five.get(subString);
        if (i==null){
            i = 0;
        }
        i++;
        five.put(subString,i);
    }

    static private class PhoneOrderListAdapter extends ArrayAdapter<Order> {
        public PhoneOrderListAdapter(Context context, int resource, List<Order> objects) {
            super(context, resource, R.layout.new_order_phone_list_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View view = View.inflate(getContext(),R.layout.new_order_phone_list_item,null);
            TextView addressText = (TextView)view.findViewById(R.id.orderAddress);
            TextView phoneText = (TextView)view.findViewById(R.id.phoneNumber);
            Order order = getItem(position);
            addressText.setText(order.address);
            if (order.phoneNumber != null) {
                String formattedNumber = PhoneNumberUtils.formatNumber(order.phoneNumber);
                phoneText.setText(order.phoneNumber);
            }
            view.setTag(order);
            return view;
        }


    }

    static private class PhonePrefixListAdapter extends ArrayAdapter<String> {
        public PhonePrefixListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, android.R.id.text1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView view;
            if (convertView!=null){
                view = (TextView)convertView;
            } else {
                view = (TextView)View.inflate(getContext(),R.layout.simple_auto_resize_list_item,null);
            }
            String prefix = getItem(position);
            view.setText(PhoneNumberUtils.formatNumber(prefix));
            view.setTag(prefix);
            return view;
        }


    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_PHONE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewOrderActivity activity = (NewOrderActivity)getActivity();

                Object object = view.getTag();
                if (object instanceof  Order){

                    Order order = (Order)object;

                    activity.order.address = order.address;
                    activity.order.apartmentNumber = order.apartmentNumber;
                    activity.order.outOfTown1 = order.outOfTown1;
                    activity.order.outOfTown2 = order.outOfTown2;
                    activity.order.outOfTown3 = order.outOfTown3;
                    activity.order.outOfTown4 = order.outOfTown4;
                    activity.order.geoPoint   = order.geoPoint; //TODO: Something smarter with geocode failed
                    activity.order.geocodeFailed = order.geocodeFailed;

                    edit.setText(order.phoneNumber);

                    foundAddressMatch=true;
                } else {
                    String prefixString = (String)object;
                    edit.setText(prefixString);
                    try {
                        edit.setSelection(prefixString.length() + 1);
                    } catch (IndexOutOfBoundsException e){
                       try {
                           edit.setSelection(prefixString.length());
                       }catch (IndexOutOfBoundsException e2){
                           edit.setSelection(prefixString.length()-1);
                       }
                    }
                }



                DataAwareFragment fragment = activity.getFragment(NewOrderActivity.Pages.address);
                if (fragment!=null) fragment.onDataChanged();

            }
        });

        edit.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        return view;
    }
}
