import java.io.Serializable;

public class Query implements Serializable {

	private static final long serialVersionUID = -3481780946666495460L;
	
	private int source_postal_code;
	private int destination_postal_code;
	
	public Query(int source_postal_code, int destination_postal_code) {
		
		this.source_postal_code      = source_postal_code;
		this.destination_postal_code = destination_postal_code;
		
	}
	
	public int getSourcePostalCode() {
		return this.source_postal_code;
	}
	
	public int getDestinationPostalCode() {
		return this.destination_postal_code;
	}
	
}
