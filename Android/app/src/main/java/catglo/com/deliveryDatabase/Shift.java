package catglo.com.deliveryDatabase;


import org.joda.time.MutableDateTime;

public class Shift {
	public MutableDateTime startTime;
	public MutableDateTime endTime;
	public int odometerAtShiftStart;
	public int odometerAtShiftEnd;
	public int primaryKey;
	public boolean noEndTime=false;
	public Shift(){
		startTime=MutableDateTime.now();
		endTime=MutableDateTime.now();
	}
}
