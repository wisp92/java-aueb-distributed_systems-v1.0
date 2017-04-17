package direction_api.mapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import direction_api.common.ServerThread;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class MapperServerThread extends ServerThread {
	
	private static final String api_key = "AIzaSyCQiXuE8rL19_uTfG7b_K9c4aSHWV3be7I";
	
	private final MapperDatabase mapper_database;	
	
	public MapperServerThread(java.net.Socket socket, MapperDatabase mapper_database)
			throws IOException{
		super(socket);
		
		this.mapper_database = mapper_database;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		MapperClientReducerThread thread;
		boolean completed = true;
		
		try {
			
			int id = this.in.readInt();
			Query query = (Query)(this.in.readObject()); // TODO: Check class type first.
			SocketInformation reducer_socket = (SocketInformation)(this.in.readObject()); // TODO: Check class type first.
			
			RoutesList routes;
			
			synchronized (this.mapper_database) {
				routes = this.mapper_database.searchRoute(query);
			}
			
			if (routes.isEmpty()) {
				
				String route = getRouteFromAPI(query);
				
				synchronized (this.mapper_database) {
					
					this.mapper_database.insertRoute(query, route);
					routes = this.mapper_database.searchRoute(query);
					
				}
				
			}
			
			thread = new MapperClientReducerThread(reducer_socket, id, routes);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(1000);
			}
			
		} catch (ClassNotFoundException | InterruptedException | IOException ex) {
			
			completed = false;
			
			ex.printStackTrace();
			
		} finally {
			
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}
	
	protected static String getRouteFromAPI(Query query) {
		
		Coordinates source      = query.getSource();
		Coordinates destination = query.getDestination();
		String route = null;
		
		try {
			
			URL api_url = new URL(
				"https://maps.googleapis.com/maps/api/directions/json?" +
				"origin=" + source.toString() + "&" +
				"destination=" + destination.toString() + "&" +
				"key=" + api_key
			);
			URLConnection api_connection = api_url.openConnection();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(api_connection.getInputStream(), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			route = builder.toString();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return route;
		
	}

}
