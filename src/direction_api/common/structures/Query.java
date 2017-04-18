package direction_api.common.structures;

import java.io.Serializable;

import direction_api.common.Hash;

/**
 * 
 * @author p3100161, p3130029
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		
		boolean is_equal = false;
		
		if (this == object) {
			is_equal = true;
		}
		else if (object instanceof Query) {
			
			Query query = (Query)object;
			/*
			 * Equality between  queries is determined by the relation
			 * of each ones source and destination.
			 */
			is_equal = ((query.getSource().equals(this.getSource())) &&
					(query.getDestination().equals(this.getDestination())));
					
		}
		
		return is_equal;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/*
		 * Since the getSHA1() is based on the source
		 * and destination values the hashCode() complies
		 * with the corresponding equals() method.
		 */
		return this.getSHA1().hashCode();
	}
	
	/*
	 * Computes the SHA-1 hash digest of the object's
	 * coordinates.
	 */
	public String getSHA1() {
		return Hash.getHash(this.getSource().toString() +
				this.getDestination().toString(), "SHA-1");
	}
	
}
