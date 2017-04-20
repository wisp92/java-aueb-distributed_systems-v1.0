package direction_api.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.UnexpectedException;

import direction_api.common.Server;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

/**
 * 
 * @author p3100161, p3130029
 *
 */

/*
 * Creates a MapperServer object responsible to do the mapping
 * between the query and the matching routes.
 */
public class MapperServer extends Server {
	
	private final MapperDatabase database;	
	private final String google_api_key;
	
	public MapperServer(
			Socket socket,
			MapperDatabase database,
			String google_api_key
			) throws IOException{
		super(socket);
		
		this.database = database;
		this.google_api_key  = google_api_key;
	}
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Server#task()
	 */
	@Override
	protected void task() throws IOException {
		
		MapperClientReducerThread thread;
		boolean completed = true;
		
		try {
			
			
			/*
			 * First we retrieve from the master the query, its unique ID and the
			 * socket information of the reducer that we are supposed to sent the
			 * results.
			 */
			int id = this.in.readInt();
			Query query = this.readObject(this.in.readObject(), Query.class);
			SocketInformation reducer_socket = this.readObject(
					this.in.readObject(), SocketInformation.class);
			
			
			/*
			 * We then search the database based on the received query an if
			 * a match is found we sent the results to the reducer.
			 */
			RoutesList routes;
			synchronized (this.database) {
				routes = this.database.searchRoute(query);
			}
			
			if (routes.isEmpty()) {
				
				/*
				 * If no results were found in the database then we should try to
				 * retrieve an available route directly from the Google'w API.
				 */
				String route = getRouteFromAPI(query);
				
				synchronized (this.database) {
					
					/*
					 * While we wait for the results of the Google API the database might
					 * already been informed from another thread. So we should perform another
					 * search after we add our own result to the database.
					 */
					this.database.insertRoute(query, route);
					routes = this.database.searchRoute(query);
					
				}
				
			}
			
			/*
			 * At this point we should have retrieved at least one (in fact exactly one).
			 * So we instantiate a client thread to pass our results to the reducer and
			 * we wait for its response.
			 */
			thread = new MapperClientReducerThread(reducer_socket, id, routes);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(1000);
			}
			
		} catch (ClassNotFoundException | InterruptedException | IOException ex) {
			
			/*
			 * By setting completed to false we inform the master that
			 * something went wrong.
			 */
			completed = false;
			
			ex.printStackTrace(); // TODO: Should check this later on.
			
		} finally {
			
			/*
			 * Finally we should inform the master if it is possible to retrieve
			 * its results from the reducer.
			 */
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}
	
	protected String getRouteFromAPI(Query query) {
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
			ex.printStackTrace(); // TODO: Should check this later on.
		}
		
		return route;
		
	}

	/**
	 * Implements a safe cast for the remote objects and throw an
	 * exception if the cast is not possible.
	 * @param object
	 * @param object_class
	 * @return
	 * @throws UnexpectedException
	 */
	private <E> E readObject(Object object, Class<E> object_class) throws UnexpectedException {
		if (object_class.isInstance(object)) {
			return object_class.cast(object);
		}
		else {
			throw new UnexpectedException("Unexpected type of object.");
		}
	}
}
