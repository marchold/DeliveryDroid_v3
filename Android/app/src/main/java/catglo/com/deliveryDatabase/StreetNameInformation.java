package catglo.com.deliveryDatabase;

public class StreetNameInformation {
	public String					suffixUrl;
	int						state;
	public String					name;
	boolean					dirty;
	public static final int	STATE_NEEDS_LOOKUP		= 1;
	public static final int	STATE_LOOKUP_SUCCESS	= 2;
	public static final int	STATE_LOOKUP_FAILED		= 3;

	public StreetNameInformation(final String url, final String name) {
		super();
		this.name = name;
		this.suffixUrl = url;
		state = STATE_NEEDS_LOOKUP;
		dirty = true;
	}
}