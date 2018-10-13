package catglo.com.deliveryDatabase;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

public class TipTotalData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float cost;
	public float payed;
	public int deliveries;
	public int runs;
	public float mileageEarned;
	public float total;         //Total Tips Earned + mileage earned 
	public float payedCash;
	public int outOfTownOrders;
	public float cashTips;
	public float reportableTips;
	public int	outOfTownOrders2;
	public int	outOfTownOrders3;
	public int	outOfTownOrders4;
	public int odometerTotal;
	public float hours;
	public float hourlyPay;
	
	
	public float bestTip;
	public Timestamp bestTipTime;
	public float worstTip;
	public Timestamp worstTipTime;
	public float averageTip;
	public int extraPay;
	public float averagePercentageTip;
	public float lastTip;
	
	public class PayRatePieriod implements Serializable {
		public float hours;
		public float hourlyPay;	
	}
	public HashMap<String,PayRatePieriod> payRatePieriods = new HashMap<String,PayRatePieriod>();
};