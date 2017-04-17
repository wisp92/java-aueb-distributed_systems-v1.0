package direction_api.reducer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;

import direction_api.common.Configuration;
import direction_api.common.structures.RoutesList;

public class Reducer {

	public static final int default_port = 4322;
	public static final int default_no_connections = 0;
	public static final String default_conf_file = "reducer.properties";
	
	private Configuration configuration;
	private ServerSocket server_socket;
	
	public Reducer() {
		this(Reducer.class.getResource(default_conf_file));
	}
	
	public Reducer(URL resource) {
			
		this.configuration = new Configuration(resource);
		
		this.start(
				this.configuration.getInt("port", default_port),
				this.configuration.getInt("number_of_allowed_connections", default_no_connections));
		
	}
	
	private void start(int port, int no_connections) {
		
		ArrayDeque<ReducerServerThread> threads = new ArrayDeque<ReducerServerThread>();
		HashMap<Integer, RoutesList> stored_routes = new HashMap<Integer, RoutesList>();
		
		try {
			
			this.server_socket = new ServerSocket(port, no_connections);
			
			while (true) {
				
				ReducerServerThread thread = new ReducerServerThread(
						this.server_socket.accept(), stored_routes);
				thread.start();
				threads.addLast(thread);
				
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			
			try {
				
				if (!this.server_socket.isClosed()) {
					this.server_socket.close();
				}
				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
	}

	public static void main(String args[]) {
		new Reducer();
	}
	
}
