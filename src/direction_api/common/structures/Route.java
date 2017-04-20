package direction_api.common.structures;

import java.io.Serializable;

/**
 * @author p3100161, p3130029
 *
 * Creates Route object that is a serializable representation
 * of the actual result of Google API.
 */

public class Route implements Serializable {

	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = 1350219233099093423L;
	
	/*
	 * A route is represented by s String object in JSON format as
	 * provided by the Google's direction API.
	 */
	private final String raw_route;
	private final Query  query;
	
	public Route(Query query, String raw_route) {
		
		this.query     = query;
		this.raw_route = raw_route;
		
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.raw_route;
	}
	
}
