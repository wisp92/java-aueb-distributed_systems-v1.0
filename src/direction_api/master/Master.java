package direction_api.master;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;

import direction_api.common.Configuration;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class Master {

	public static final String default_conf_file = "master.properties";
	public static final int default_port = 4323;
	public static final int default_no_connections = 0;
	public static final int default_no_mappers = 0;
	public static final String default_reducer_host = "localhost";
	public static final int default_reducer_port = 4322;
	
	private Configuration configuration;
	private ServerSocket server_socket;
	private SocketInformation[] mapper_sockets;
	private SocketInformation reducer_socket;
	private IDRange client_ids;
	
	public Master() {
		this(Master.class.getResource(default_conf_file));
	}
	
	public Master(URL resource) {
			
		this.configuration = new Configuration(resource);
		int no_mappers     = this.configuration.getInt("number_of_mappers", default_no_mappers);
		int no_connections = this.configuration.getInt(
				"number_of_allowed_connections", default_no_connections);
		
		this.client_ids     = new IDRange(no_connections);
		this.reducer_socket = new SocketInformation(
				this.configuration.getString("reducer_host", default_reducer_host),
				this.configuration.getInt("reducer_port", default_reducer_port));
		
		if (no_mappers > 0) {
			
			this.mapper_sockets = new SocketInformation[no_mappers];
			
			for (int i = 0; i < no_mappers; i ++) {
				
				String mapper_host_key = "mapper_host." + (i + 1);
				String mapper_port_key = "mapper_port." + (i + 1);
				
				if (
					   !this.configuration.containsKey(mapper_host_key)
					|| !this.configuration.containsKey(mapper_port_key)
				) {
					no_mappers = 0;
					break;
				}
				else {
					this.mapper_sockets[i] = new SocketInformation(
						this.configuration.getString(mapper_host_key),
						this.configuration.getInt(mapper_port_key)
					); 
				}
			}
		}
		
		this.start(
			this.configuration.getInt("port", default_port),
			this.configuration.getInt("number_of_allowed_connections", default_no_connections)
		);
		
	}
	
	private void start(int port, int no_connections) {
		
		ArrayDeque<MasterServerThread> threads = new ArrayDeque<MasterServerThread>();
		HashMap<Query, RoutesList> cached_routes = new HashMap<Query, RoutesList>();
		
		try {
			
			this.server_socket = new ServerSocket(port, no_connections);
			
			while (true) {
				
				MasterServerThread thread = new MasterServerThread(
					this.server_socket.accept(),
					this.mapper_sockets,
					this.reducer_socket,
					this.client_ids,
					cached_routes
				);
				thread.start();
				threads.add(thread);
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
		new Master();
	}
	
}
