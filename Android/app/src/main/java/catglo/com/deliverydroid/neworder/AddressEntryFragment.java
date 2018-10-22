package catglo.com.deliverydroid.neworder;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import catglo.com.api.GoogleAddressSuggester;
import catglo.com.deliveryDatabase.AddressInfo;
import catglo.com.deliveryDatabase.AddressSuggester;
import catglo.com.deliverydroid.R;
import catglo.com.widgets.ButtonPadFragment;
import catglo.com.widgets.DataAwareFragment;


import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by goblets on 2/16/14.
 */
public class AddressEntryFragment extends ButtonPadFragment {
    AddressSuggester addressSuggestior;
    Pattern pattern;
    private int		inputStage;
    ArrayList<AddressInfo> addressList;
    public AddressInfo selectedPoint=null;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Super is going to set the text view but we need to also pull the GPS location
        super.onItemClick(parent,view,position,id);
        AddressInfo address = addressList.get(position);

        NewOrderActivity activity = (NewOrderActivity)getActivity();
        activity.order.geoPoint = address.location;
        activity.order.isValidated = true;

    }


    @Override
    public ListAdapter getListAdapter() {
        return null;
    }

    @Override
    protected void onDataChangedHandler() {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        edit.setText(activity.order.address);
    }

    @Override
    public void onResume(){
        super.onResume();

        NewOrderActivity activity = (NewOrderActivity)getActivity();

        addressSuggestior = new AddressSuggester(getContext(), activity.dataBase, new GoogleAddressSuggester.AddressListListener() {
            @Override
            public void commit(ArrayList<AddressInfo> addressList, String searchString) {
                if (   addressList  ==null
                        || getActivity()==null
                        || getActivity().isFinishing()==true)
                {
                    return;
                }
                ArrayAdapter<AddressInfo> streets = new ArrayAdapter<AddressInfo>(getActivity(), R.layout.out_the_door_address_list_item, addressList);
                if (streets.isEmpty()==false){
                    list.setAdapter(streets);
                    list.setVisibility(View.VISIBLE);
                    tooltipLayout.setVisibility(View.GONE);
                    AddressEntryFragment.this.addressList = addressList;
                }
            }
        });

        edit.setText(activity.order.address);


        ArrayList<String> addressStrings = new ArrayList<String>();
        activity.dataBase.getAddressSuggestionsFor("",addressStrings);
        addressList = new ArrayList<AddressInfo>();
        for (String address:addressStrings){
            AddressInfo i = new AddressInfo();
            i.address = address;
            addressList.add(i);
        }
        ArrayAdapter<AddressInfo> streets = new ArrayAdapter<AddressInfo>(activity, R.layout.out_the_door_address_list_item, addressList);
        if (streets.isEmpty()==false){
            list.setAdapter(streets);
            list.setVisibility(View.VISIBLE);
            tooltipLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);


        pattern = Pattern.compile("([0-9\\-\\#\\@\\*_]*\\s)(.*)");


       /* setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE){
                        onSpace();
                    }
                }
                return false;
            }}
        );*/

        abc.setVisibility(View.VISIBLE);
        space.setVisibility(View.VISIBLE);


      //  edit.clearFocus();
      //  one.requestFocus();
      //  list.requestFocus();



        return view;
    }




    @Override
    protected void onSpace(){
        switch (inputStage) {
            case 0:
                list.setVisibility(View.VISIBLE);
                //text.setText("Address - Street Name");

                break;
            case 1:
                list.setVisibility(View.VISIBLE);
                //text.setText("Address - Suffix");
                final String[] sufixList =
                        {
                                "Apt. ", "Suite.", "Ave", "St", "Pl", "Dr",
                                "N", "S","E", "W", "NW", "NE", "SW", "SE"
                        };
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        R.layout.out_the_door_address_list_item, sufixList);
                setAdapter(adapter);

                inputStage = 2;
                break;
        }
    }


    @Override
    public void onTextChanged(String newText) {
        NewOrderActivity activity = (NewOrderActivity)getActivity();
        activity.order.address = edit.getText().toString();
        DataAwareFragment fragment = activity.getFragment(NewOrderActivity.Pages.order);
        if (fragment!=null) fragment.onDataChanged();

        if (edit.isFocused()) {
            selectedPoint = null;
        }
        if (addressSuggestior!=null) addressSuggestior.lookup(newText);

    }
}
