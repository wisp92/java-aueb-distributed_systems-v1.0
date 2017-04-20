package direction_api.common.structures;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author p3100161, p3130029
 * 
 * Creates QueryResults object that wraps together the original query
 * and the results associated with it.
 */

public class QueryResults implements Serializable {
	
	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = 1850488110532000204L;
	
	private final Query query;
	private final ArrayList<Route>  results;
	
	public QueryResults(Query query, ArrayList<Route> results) {
		
		this.query   = query;
		this.results = results;
		
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	public ArrayList<Route> getResults() {
		return this.results;
	}
	
}