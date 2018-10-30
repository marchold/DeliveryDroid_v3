package catglo.com.deliveryDatabase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import catglo.com.api.GoogleAddressSuggester;


public class AddressHistorySuggester extends GoogleAddressSuggester
{
	float range;
	private ArrayList<AddressInfo> recientStreetNames;
	public ArrayList<AddressInfo> addressList;

	private String prefKey;
	private AddressSuggestionCommitor commitor;

	public interface AddressResultListener {
		public void onResultAddress(ArrayList<AddressInfo> results);
	}
	private AddressResultListener resultListener=null;
	
	final String fileNamePrefix="history____";
		
	public AddressHistorySuggester(Context context, String prefKey, final AddressResultListener commit){
		super(context,null);
	
		FileInputStream fis;
		try {
			fis = context.openFileInput(fileNamePrefix+prefKey);
			ObjectInputStream is = new ObjectInputStream(fis);
			recientStreetNames = (ArrayList<AddressInfo>) is.readObject();
			is.close();
		} catch (Exception e) {
			recientStreetNames = new ArrayList<AddressInfo>();
		}

		this.prefKey = prefKey;
        this.resultListener = commit;
	}

	public interface AddressSuggestionCommitor {
		void commit(ArrayList<AddressInfo> addressList, String searchString);
	}

	Pattern initialNumebrs = Pattern.compile("^([0-9]+)");

	void init(){
		commitor = new AddressSuggestionCommitor(){public void commit(ArrayList<AddressInfo> addresses,  String originalSearchString) {
			
			ArrayList<AddressInfo> list = new ArrayList<AddressInfo>();
			
			Pattern streetNameAfterNumber = Pattern.compile("^([0-9]+\\s{0,2})(\\w+)");
			
			Matcher m = streetNameAfterNumber.matcher(originalSearchString);
			
			if (m.find()){//If the address starts with a number and has street letters
				for (AddressInfo address : addresses){
					list.add(address);
				}	
			}
			else { 
				try {
					for (AddressInfo address : recientStreetNames){
						if (address.address.startsWith(originalSearchString)) {
							list.add(address);
						}
					}
				} catch (NullPointerException e){
					e.printStackTrace();
				}
			}
		
			AddressHistorySuggester.this.addressList = list;
			if (resultListener != null) {
				resultListener.onResultAddress(list);
			}
		}};
	}

	@Override
	public void lookup(final String addressSoFar) {
		if (commitor == null){
			init();
		}
		
		super.lookup(addressSoFar);
	}

	public void saveResult(AddressInfo value) {
		boolean exists = false;
		for (AddressInfo s : recientStreetNames){
			try {
				if (s.address.equalsIgnoreCase(value.address)){
					exists = true;
					break;
				} 
			}catch (NullPointerException e){
				e.printStackTrace();
			}
		}
		if (!exists){
			recientStreetNames.add(value);
		}
		
		Collections.sort(recientStreetNames, new Comparator<AddressInfo>(){public int compare(AddressInfo lhs, AddressInfo rhs) {
			try {
				return lhs.address.compareTo(rhs.address);
			} catch (NullPointerException e){
				e.printStackTrace();
				return -1;
			}
		}});
		try {
			FileOutputStream fos = getContext().openFileOutput(fileNamePrefix+prefKey, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(recientStreetNames);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
