import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class MapperServerThread extends ServerThread {
	
	private RoutesDB routes_db;	
	
	public MapperServerThread(Socket request_socket, RoutesDB routes_db) throws IOException{
		super(request_socket);
		
		this.routes_db = routes_db;
	}
	
	protected static String getRouteFromAPI(Query route_query) {
		
		String source_latlon      = route_query.getSource().toString();
		String destination_latlon = route_query.getDestination().toString();
		String route = "";
		
		try {
			
			URL api_url = new URL(
				"https://maps.googleapis.com/maps/api/directions/json?" +
				"origin=" + source_latlon + "&" +
				"destination=" + destination_latlon + "&" +
				"key=AIzaSyCQiXuE8rL19_uTfG7b_K9c4aSHWV3be7I"
			);
			URLConnection api_connection = api_url.openConnection();
			
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(api_connection.getInputStream(), "UTF-8")
			);
			StringBuilder builder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			route =  builder.toString();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return route;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		boolean completed = true;
		
		try {
			
			// Get information from master.
			int id                         = this.in.readInt();
			Query route_query              = (Query)(this.in.readObject());
			SocketStructure reducer_socket = (SocketStructure)(this.in.readObject());
			
			Routes routes;
			
			synchronized (this.routes_db) {
				routes = this.routes_db.searchRoute(route_query);
			}
			
			if (routes.isEmpty()) {
				
				String route = getRouteFromAPI(route_query);
				
				synchronized (this.routes_db) {
					
					this.routes_db.insertRoute(route_query, route);
					routes = this.routes_db.searchRoute(route_query);
					
				}
				
			}
			
			MapperClientReducerThread t = new MapperClientReducerThread(reducer_socket, id, routes);
			t.start();
			
			while (!t.isCompleted()) {
				sleep(1000);
			}
			
		} catch (ClassNotFoundException | InterruptedException ex) {
			
			completed = false;
			
			ex.printStackTrace();
			
		} finally {
			
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}

}
