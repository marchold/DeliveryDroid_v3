package catglo.com.deliveryDatabase;

import java.util.Calendar;

public class Expense {
	public long ID;
	public String description;
	public String category;
	public float amount;
	public boolean reimbursable;
	public boolean reimbursed;
	public Calendar expenseTime = Calendar.getInstance();
	public int shiftId;
}
