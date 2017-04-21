package direction_api.master;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.net.Socket;

import direction_api.common.Constants;
import direction_api.common.IDPool;
import direction_api.common.LRUCache;
import direction_api.common.structures.Query;
import direction_api.common.structures.Route;
import direction_api.common.structures.SocketInformation;
import direction_api.common.ServerManager;
import direction_api.common.Server;

/**
 * @author p3100161, p3130029
 *
 * Creates a Master object as direct implementation of ServerManager object.
 * A Master object is responsible to wait for user requests and the create
 * threads to reply to them.
 * It keeps all the information about the location of the other servers and
 * is responsible for identifying the communication between them.
 * Finally it keeps a cache of recent requests in order to reply faster to the more
 * popular requests.
 */

public class Master extends ServerManager {

	public static final String default_conf_file    = "master.properties";
	public static final int default_no_mappers      = 0;
	public static final String default_reducer_host = "localhost";
	public static final int default_reducer_port    = 0;
	public static final int default_no_connections  = 1;
	
	/*
	 * Because the master is respsonsible of knowing the addresses of each of the socket
	 * information of both the mappers and the reducer should be provided at run time.
	 */
	protected ArrayList<SocketInformation> mapper_sockets;
	protected SocketInformation reducer_socket;
	/*
	 * In order for the master to be able to receive the results of the query from the
	 * reducer we must be able to identify each connection with a different id.
	 * Because many users can request the same query a more safe way to do it is
	 * by specifying a unique id for the duration of the connection.
	 * The only drawback is that we have to define the maximum number of allowed
	 * connections beforehand.
	 */
	protected IDPool connections_id_pool;
	
	/*
	 * The most recent results are kept in cache in case they are requested
	 * again by a user.
	 * The cache's policy is LRU.
	 */
	protected LRUCache<Query, Route> cached_results;
	
	public Master() {
		super(Master.class.getResource(default_conf_file));
		
		if (this.no_connections < default_no_connections) {
			this.setNumberOfAllowedConnections(default_no_connections);
		}
		
		if (this.mapper_sockets == null) {
			this.mapper_sockets = new ArrayList<SocketInformation>();
		}
		
	}
	
	/*
	 * We implement some basic getters and setters that might be
	 * used by an initializer.
	 */
	public int getNumberOfMappers() {
		return this.mapper_sockets.size();
	}
	
	public SocketInformation getMapper(int i) {
		return this.mapper_sockets.get(i);
	}
	
	public SocketInformation getReducer() {
		return this.reducer_socket;
	}
	
	public boolean setMappers(ArrayList<SocketInformation> mapper_sockets) {
		
		for (SocketInformation socket : mapper_sockets) {
			if (!(socket instanceof SocketInformation)) {
				return false;
			}
		}
		
		this.mapper_sockets = mapper_sockets;
		return true;
		
	}
	
	public boolean setReducer(SocketInformation reducer_socket) {
		
		if (!(reducer_socket instanceof SocketInformation)) {
			return false;
		}
		else {
			this.reducer_socket = reducer_socket;
			return true;
		}
		
	}
	
	public void loadConfigurationFile(String path) {
		
		super.loadConfigurationFile(path);
		
		int no_mappers = this.configuration.getInt("number_of_mappers", default_no_mappers);

		/*
		 * The mappers are read from file in the format "mapper_host.i" and "mapper_port.i".
		 * The number of mappers should also be supplied.
		 */
		if (no_mappers > 0) {
			
			this.mapper_sockets = new ArrayList<SocketInformation>();
			
			for (int i = 0; i < no_mappers; i ++) {
				
				String mapper_host_key = "mapper_host." + (i + 1);
				String mapper_port_key = "mapper_port." + (i + 1);
				
				if (!this.configuration.containsKey(mapper_host_key) ||
						!this.configuration.containsKey(mapper_port_key)) {
					
					/*
					 * If something goes wrong we remove all the mappers from the list.
					 */
					this.mapper_sockets = new ArrayList<SocketInformation>();
					break;
					
				}
				else {
					
					this.mapper_sockets.add(new SocketInformation(
							this.configuration.getString(mapper_host_key),
							this.configuration.getInt(mapper_port_key)));
					
				}
			}
		}
		
		this.setReducer(new SocketInformation(
				this.configuration.getString("reducer_host", default_reducer_host),
				this.configuration.getInt("reducer_port", default_reducer_port)));
		
	}
	
	/* 
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#initServerThread(java.net.Socket)
	 */
	protected Server initServerThread(Socket socket) throws IOException {
		
		return new MasterServer(
				socket,
				this.mapper_sockets,
				this.reducer_socket,
				this.connections_id_pool,
				this.cached_results);

	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#start()
	 */
	@Override
	public void start() {
		
		/*
		 * Both the id pool and the cache should be initialized every
		 * time the server starts.
		 */
		this.connections_id_pool = new IDPool(no_connections);
		this.cached_results      = new LRUCache<Query, Route>(this.no_connections);
		
		if (Constants.debugging) {
			if (this.configuration != null) {
				System.out.println("Master> configuration_file: " + this.configuration.getPath());
			}
			System.out.println("Master> server_port: " + this.port);
			System.out.println("Master> no_mappers: " + this.mapper_sockets.size());
			System.out.println("Master> cache_capacity: " + this.cached_results.max_capacity);
			System.out.println("Master> start()");
		}
		
		super.start();
		
	}
	
	public static void main(String args[]) {
		
		String default_conf_file = "master.properties";
		
		Master master = new Master();
		
		if (master.getConfigurationPath() == null) {
			
			if ((new File(default_conf_file).exists())) {
				master.loadConfigurationFile(default_conf_file);
			}
			else {
				System.out.println("Notice: No configuration file <" +
						default_conf_file + "> was loaded.");
			}
			
		}
		
		master.start();	
		
	}
	
}