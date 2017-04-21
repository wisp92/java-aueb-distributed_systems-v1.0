package direction_api.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import direction_api.common.Constants;
import direction_api.common.Server;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.Route;
import direction_api.common.structures.QueryResults;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 * 
 * Creates a MapperServer object responsible to do the mapping
 * between the query and the matching routes.
 */

/*
 * Creates a MapperServer object responsible to do the mapping
 * between the query and the matching routes.
 */
public class MapperServer extends Server {
	
	private final MapperDatabase database;	
	private final String google_api_key;
	private int id;
	
	public MapperServer(
			Socket socket,
			MapperDatabase database,
			String google_api_key
			) throws IOException{
		super(socket);
		
		this.database        = database;
		this.google_api_key  = google_api_key;
		this.id              = -1;
	}
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Server#task()
	 */
	@Override
	protected void task() throws IOException, InterruptedException {
		
		/*
		 * By setting completed to false we inform the master that
		 * something went wrong.
		 */
		boolean completed = false;
		
		try {
			
			/*
			 * First we retrieve from the master the query, its unique ID and the
			 * socket information of the reducer that we are supposed to sent the
			 * results.
			 */
			this.id = this.in.readInt();
			Query query = this.readObject(this.in.readObject(), Query.class);
			SocketInformation reducer_socket = this.readObject(
					this.in.readObject(), SocketInformation.class);
			
			if (Constants.debugging) {
				System.out.println("Mapper(S)> connection_id: " + this.id);
				System.out.println("Mapper(S):" + this.id + "> query: " + query.toString());
				System.out.println("Mapper(S):" + this.id + "> after() = " + reducer_socket.toString());
			}
			
			/*
			 * We then search the database based on the received query an if
			 * a match is found we sent the results to the reducer.
			 */
			QueryResults results;
			synchronized (this.database) {
				results = this.database.searchRoute(query);
			}
			
			if (Constants.debugging) {
				System.out.println("Mapper(S):" + this.id + "> database_results: " +
						(!results.getResults().isEmpty()));
			}
			
			if (results.getResults().isEmpty()) {
				
				if (Constants.debugging) {
					System.out.println("Mapper(S):" + this.id + "> get(google_api)");
				}
				
				/*
				 * If no results were found in the database then we should try to
				 * retrieve an available route directly from the Google'w API.
				 */
				Route route = getRouteFromAPI(query);
				
				if (Constants.debugging) {
					System.out.println("Mapper(S):" + this.id + "> route: " + route.toString());
					System.out.println("Mapper(S):" + this.id + "> update_database()");
				}
				
				synchronized (this.database) {
					
					/*
					 * While we wait for the results of the Google API the database might
					 * already been informed from another thread. So we should perform another
					 * search after we add our own result to the database.
					 */
					this.database.insertRoute(route);
					results = this.database.searchRoute(query);
					
				}
				
			}
			
			/*
			 * At this point we should have retrieved at least one (in fact exactly one).
			 * So we instantiate a client thread to pass our results to the reducer and
			 * we wait for its response.
			 */
			
			if (Constants.debugging) {
				System.out.println("Mapper(S):" + this.id + "> connect(after())");
			}
			
			try (MapperToReducerClient thread = new MapperToReducerClient(
					reducer_socket, this.id, results)) {
				
				thread.start();
				
				while (!thread.isCompleted()) {
					Thread.sleep(Constants.max_sleep_time);
				}
				
			}
			
			completed = true;
			
		} catch (ClassNotFoundException ex) {
			
			ex.printStackTrace();
			/*
			 * If an object can't be recognized we stop the execution.
			 */
			throw new IOException();
			
		} finally {
			
			/*
			 * Finally we should inform the master if it is possible to retrieve
			 * its results from the reducer.
			 */
			
			if (Constants.debugging) {
				System.out.println("Mapper(S):" + this.id + "> return(" + completed + ")");
			}
			
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}
	
	protected Route getRouteFromAPI(Query query) {
		// TODO: Create a serialized JSON object to store in the database.
		
		Coordinates source      = query.getSource();
		Coordinates destination = query.getDestination();
		String route = null;
		
		try {
			
			/*
			 * We first construct the query according to the format of the Google directions API
			 * and we open a connection we the server.
			 */
			URL api_url = new URL(
				"https://maps.googleapis.com/maps/api/directions/json?" +
				"origin=" + source.toString() + "&" +
				"destination=" + destination.toString() + "&" +
				"key=" + this.google_api_key
			);
			URLConnection api_connection = api_url.openConnection();
			
			/* 
			 * We retrieve the results in JSOn format.
			 */
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(api_connection.getInputStream(), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			route = builder.toString();
			
		} catch (IOException ex) {
			
			/*
			 * Exceptions at this point should not stop the execution in case
			 * we want to retry some times.
			 */
			ex.printStackTrace();
			
		}
		
		return new Route(query, route);
		
	}

}
