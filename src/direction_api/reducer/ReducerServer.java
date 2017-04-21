package direction_api.reducer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import direction_api.common.Constants.MsgType;
import direction_api.common.Constants;
import direction_api.common.Server;
import direction_api.common.structures.Query;
import direction_api.common.structures.QueryResults;
import direction_api.common.structures.Route;

/**
 * @author p3100161, p3130029
 *
 * Creates a Server object responsible for storing and retrieving
 * the results of the queries.
 * It is also responsible for reducing the list of the queries to
 * one final result.
 */

public class ReducerServer extends Server {
	
	/*
	 * The results of the queries are temporarily stores in a hash map
	 * where they can be retrieved using the identification number of
	 * the master's initial connection.
	 */
	private final HashMap<Integer, QueryResults> stored_routes;
	private int id;
	
	public ReducerServer(Socket socket, HashMap<Integer, QueryResults> stored_routes)
			throws IOException {
		super(socket);
		
		this.stored_routes = stored_routes;
		this.id            = -1;
	}
	
	@Override
	protected void task() throws IOException {
		
		try {
			
			/*
			 * First we make sure to identify the type of
			 * the connection in order to know If we must
			 * store to or retrieve results from the hashmap.
			 */
			MsgType type_of_connection = this.readObject(this.in.readObject(), MsgType.class);
			this.id = this.in.readInt();
			
			if (Constants.debugging) {
				System.out.println("Reducer(S)> type() = " + type_of_connection.name());
				System.out.println("Reducer(S)> connection_id: " + this.id);
			}
			
			QueryResults results = null;
			
			switch(type_of_connection) {
				case MSG_PUT_ROUTE:
					
					/*
					 * In case we have a communication with the mapper we
					 * should retrieve the query and the results to store them in the hashmap.
					 */
					results = this.readObject(this.in.readObject(), QueryResults.class);
					
					if (Constants.debugging) {
						System.out.println("Reducer(S):" + this.id + "> original_query: " +
								results.getQuery().toString());
					}
					
					synchronized (this.stored_routes) {
						stored_routes.put(this.id, results);
					}
					
					/*
					 * We then notify the mapper tha is now safe to tell to the master
					 * to seek its result.
					 */
					
					if (Constants.debugging) {
						System.out.println("Reducer(S):" + this.id + "> return()");
					}
					
					this.out.writeBoolean(true);
					this.out.flush();
					
					break;
					
				case MSG_GET_ROUTE:
					
					synchronized (this.stored_routes) {
						
						/*
						 * Under normal execution this condition should always be true.
						 */
						if (stored_routes.containsKey(this.id)) {
							results = stored_routes.remove(this.id);
						}
						
					}
					
					/*
					 * Null should only be send if the specified id does not exist in
					 * the hashmap.
					 */
					Route route = null;
					
					if (results instanceof QueryResults) {
						
						ArrayList<Route> routes = results.getResults();
						Query original_query    = results.getQuery();
						
						/*
						 * The reducer should return only one route to the master.
						 * So we use the distance between the original query the
						 * the queries of the results to find the best route.
						 */
						if (!routes.isEmpty()) {
							route = routes.stream().reduce(routes.get(0),
									(x0, x) -> (
											original_query.getDistanceFrom(x.getQuery()) < 
											original_query.getDistanceFrom(x0.getQuery())) ?
													x : x0);
						}
						else {
							/*
							 * If no results where found the route is going to contain
							 * a null string.
							 */
							route = new Route(original_query, null);
						}
					}
					
					if (Constants.debugging) {
						System.out.println("Reducer(S):" + this.id + "> route: " + route.toString());
						System.out.println("Reducer(S):" + this.id + "> return(route)");
					}
					
					out.writeObject(route);
					out.flush();
					
					break;
					
			}
			
			
		} catch (ClassNotFoundException ex) {
			
			ex.printStackTrace();
			/*
			 * If an object can't be recognized we stop the execution.
			 */
			throw new IOException();
			
		}
			
	}

}

