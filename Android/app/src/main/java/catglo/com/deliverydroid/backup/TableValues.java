package catglo.com.deliverydroid.backup;

import java.io.Serializable;
import java.util.ArrayList;



/**
 * Created by goblets on 9/7/13.
 */
public class TableValues implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String tableName;
    public ArrayList<String> fieldNames  = new ArrayList<String>();
    public ArrayList<ArrayList<String>> fieldValues = new ArrayList<ArrayList<String>>();
    public String modificationTime;
    
    /*public static TableValues constructFromJson(String json)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, TableValues.class);
    }
     
    public String toJson() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }*/
}