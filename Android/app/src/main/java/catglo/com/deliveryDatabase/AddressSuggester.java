package catglo.com.deliveryDatabase;

import android.content.Context;

import catglo.com.GoogleAddressSuggester;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddressSuggester extends GoogleAddressSuggester {

    //Listener for results sent back to caller
    private GoogleAddressSuggester.AddressResultListener commitLookup;

	private DataBase dataBase;
	float range;
	private StreetList streetList;
	public ArrayList<AddressInfo> addressList;

    public static final Pattern initialNumbers = Pattern.compile("^([0-9]+)");

    // public GoogleAddressSuggester(Tooled tooled, AddressResultListener addressListListener) {
    public AddressSuggester(final Context context, final DataBase dataBase, final AddressResultListener addressListListener){
		super(context,new AddressResultListener(){
            @Override
            public void commit(ArrayList<AddressInfo> addresses, String originalSearchString) {

                Pattern streetNameAfterNumber = Pattern.compile("^([0-9]+\\s{0,2})(\\w+)");
                Matcher m = streetNameAfterNumber.matcher(originalSearchString);
                ArrayList<AddressInfo> list = null;
                if (m.find()){//If the address starts with a number and has street letters
                    String numberPart = m.group(1);
                    String streetPart = m.group(2);

                    //Filter out addresses that do not have the same numerical prefix
                    list = new ArrayList<AddressInfo>();
                    m = initialNumbers.matcher(originalSearchString);
                    if (m.find()) {
                        String numberPartOfSearchString = m.group(1);
                        for (AddressInfo addressInfo : addresses){
                            String address = addressInfo.getAddress();
                            if (address.startsWith(numberPartOfSearchString)){
                                list.add(addressInfo);
                            }
                        }
                    }

                    //Local streeet list is broken
                    /*
                    //If the google found at least 1 match with the same prefix, that is probably it, just use google
                    //The google result did not find a good match, filter the local street list
                    if (list.size()==0){
                        int size = StreetList.parentList.size();
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                //In this case the originalSearchString has a space so we need to parse out the street name
                                //and filter by the letters
                                String s = new String(StreetList.parentList.get(i).name).toLowerCase();

                                if (s.startsWith(streetPart.toLowerCase())){
                                    s = s.substring(0, 1).toUpperCase() + s.substring(1);
                                    String addressString = numberPart + s.replace('+', ' ')
                                    AddressInfo addressInfo = new AddressInfo(addressString,);
                                    list.add(addressInfo);
                                }
                            }
                        }
                    }*/
                }
                else { //We never get here if its just a number so this is for text first search the notes and append to the google list
                    list=new ArrayList<AddressInfo>();
                    for (AddressInfo address : addresses){
                        list.add(address);
                    }
                    ArrayList<AddressInfo> resultsFromDB = new ArrayList<AddressInfo>();
                    dataBase.searchAddressSuggestionsFor(originalSearchString,resultsFromDB);
                    for (AddressInfo ai : resultsFromDB){
                        list.add(ai);
                    }
                }

                if (addressListListener != null) {
                    addressListListener.commit(list,originalSearchString);
                }
            }
        });
		this.dataBase = dataBase;
        streetList = StreetList.LoadState(context);
        
       // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.commitLookup = addressListListener;

	}
	
	@Override
	public void lookup(final String addressSoFar) {
		if (useAlternateLocalLookup(addressSoFar)){
            alternateLocalLookup(addressSoFar);
        } else {
            super.lookup(addressSoFar);
        }
	}
	

	protected boolean useAlternateLocalLookup(String addressSoFar) {
		Pattern streetNumberOnly = Pattern.compile("^[0-9]+\\s{0,2}$");
		if (streetNumberOnly.matcher(addressSoFar).find()){
			return true;
		} else {
			return false;
		}
	}
	protected void alternateLocalLookup(String addressSoFar)    {
		ArrayList<AddressInfo> resultsFromDB = new ArrayList<AddressInfo>();
		addressList = new ArrayList<AddressInfo>();
		Pattern streetNumberOnly = Pattern.compile("^[0-9]+$");
		if (streetNumberOnly.matcher(addressSoFar).find()){
			dataBase.searchAddressSuggestionsFor(addressSoFar,resultsFromDB);
			
			for (AddressInfo ai : resultsFromDB){
				addressList.add(ai);
			}
			if (commitLookup != null) {
				commitLookup.commit(addressList,addressSoFar);
			}
		}
		/*
		//Local street list is broken
		else
		{
			int size = StreetList.parentList.size();
			if (size > 0) {
				for (int i = 0; i < size; i++) {

					String s = new String(StreetList.parentList.get(i).name).toLowerCase();
					s = s.substring(0, 1).toUpperCase() + s.substring(1);
					String addressString = addressSoFar + s.replace('+', ' ');
                    AddressInfo ai = new AddressInfo(addressString,);
					resultsFromDB.add(ai);
				}				
			}
			for (AddressInfo ai : resultsFromDB){
				addressList.add(ai);
			}
			if (commitLookup != null) {
                commitLookup.commit(addressList, addressSoFar);
			}
		}*/
	}
}
