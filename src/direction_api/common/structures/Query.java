package direction_api.common.structures;
import java.io.Serializable;

import direction_api.common.Hash;

/**
 * 
 * @author p3100161, [Jenny's ID]
 *
 */

public class Query implements Serializable {

	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = -3481780946666495460L;
	
	private final Coordinates source;
	private final Coordinates destination;
	
	/**
	 * A Query object can be initialized by providing the requested
	 * source and destination coordinates.
	 * @param source
	 * @param destination
	 */
	public Query(Coordinates source, Coordinates destination) {
		
		this.source      = source;
		this.destination = destination;
		
	}
	
	public Coordinates getSource() {
		return this.source;
	}
	
	public Coordinates getDestination() {
		return this.destination;
	}
	
	@Override
	public boolean equals(Object object) {
		
		boolean is_equal = false;
		
		if (this == object) {
			is_equal = true;
		}
		else if (object instanceof Query) {
			
			Query query = (Query)object;
			is_equal = ((query.getSource().equals(this.getSource())) &&
					(query.getDestination().equals(this.getDestination())));
					
		}
		
		return is_equal;
		
	}
	
	@Override
	public int hashCode() {
		return this.getSHA1().hashCode();
	}
	
	public String getSHA1() {
		return Hash.getHash(this.getSource().toString() +
				this.getDestination().toString(), "SHA-1");
	}
	
}
