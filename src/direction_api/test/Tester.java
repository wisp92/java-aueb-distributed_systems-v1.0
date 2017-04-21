package direction_api.test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import direction_api.common.Configuration;
import direction_api.common.Constants;
import direction_api.common.IDPool;
import direction_api.common.ServerManager;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.SocketInformation;
import direction_api.mapper.Mapper;
import direction_api.master.Master;
import direction_api.mobile.MobileToMasterClient;
import direction_api.reducer.Reducer;

/**
 * @author p3100161, p3130029
 *
 * Creates a object that runs at checks the functionality of the system
 * distributes on a local machine.
 */

public class Tester {
	
	public static final int default_no_mappers     = 3;
	public static final int default_no_connections = 1;
	
	public static final ArrayList<Query> default_queries = getDefaultQueries();	
	
	protected String google_api_key;
	protected int    no_connections;
	protected int    no_mappers;
	
	public Tester() {
		
		this.no_mappers     = default_no_mappers;
		this.no_connections = default_no_connections;
		
	}
	
	public boolean setNumberOfMappers(int no_mappers) {
		
		if (no_mappers >= 0) {
			
			this.no_mappers = no_mappers;
			return true;
					
		}
		
		return false;
	}
	
	public boolean setNumberOfAllowedConnections(int no_connections) {
		
		if (no_connections >= 1) {
			
			this.no_connections = no_connections;
			return true;
					
		}
		
		return false;
		
	}
	
	public void setGoogleAPIKey(String google_api_key) {
		this.google_api_key = google_api_key;
	}
	
	
	/*
	 * Starts the local servers and test the execution of a
	 * list of queries.
	 */
	public void localQuery(ArrayList<Query> queries) {
		
		if (Constants.debugging) {
			System.out.println("Tester> start_servers()");
		}
		
		ServerInitializer initializer = new ServerInitializer(this.no_mappers);
		
		for (int i = 0; i < no_mappers; i ++) {
			initializer.addMapper(this.google_api_key);
		}
		
		initializer.setReducer();
		initializer.setMaster(this.no_connections);
		
		initializer.start();
		
		/*
		 * Wait some time to make sure all servers started.
		 */
		try {
			
			Thread.sleep(Constants.max_sleep_time);
			
		} catch (InterruptedException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
		if (Constants.debugging) {
			System.out.println("Tester> send(master)");
		}
		
		SocketInformation master_socket = initializer.getServerSocket();
		
		if (master_socket != null) {
			this.query(master_socket, queries);
		}
		
		if (Constants.debugging) {
			System.out.println("Tester> stop_servers() /* Errors are normal */");
		}
		
		initializer.stop();
		
	}
	
	public void localQuery(Query query) {
		
		ArrayList<Query> queries = new ArrayList<Query>();
		queries.add(query);
		
		this.localQuery(queries);
		
	}
	
	/*
	 * Sends a list of queries to the specified socket.
	 */
	public void query(SocketInformation server_socket, ArrayList<Query> queries) {
		
		if (Constants.debugging) {
			System.out.println("User> connect(" + server_socket.toString() + ")");
		}
		
		ArrayDeque<MobileToMasterClient> threads = new ArrayDeque<MobileToMasterClient>();
		
		for (Query query : queries) {
			
			try {
				
				MobileToMasterClient thread = new MobileToMasterClient(server_socket, query);
				threads.addLast(thread);
				
				thread.start();
				
			} catch (IOException ex) {
				ex.printStackTrace(); // TODO: Should be checked in the future.
			}
			
		}
		
		while (!threads.isEmpty()) {
			
			int current_size = threads.size();
			
			for (int i = 0; i < current_size; i ++) {
				
				MobileToMasterClient thread = threads.removeFirst();
				
				if (!thread.isCompleted()) {
					threads.addLast(thread);
				}
				else {
					thread.close();
				}
				
			}
			
			try {
				
				Thread.sleep(Constants.max_sleep_time);
				
			} catch (InterruptedException ex) {
				ex.printStackTrace(); // TODO: Should be checked in the future.
			}
				
		}
		
	}
	
	public void query(SocketInformation server_socket, Query query) {
		
		ArrayList<Query> queries = new ArrayList<Query>();
		queries.add(query);
		
		this.query(server_socket, queries);
		
	}
	
	public static void main (String[] args) {
		
		Tester tester   =  new Tester();
		Scanner scanner = new Scanner(System.in);
		
		if (args.length >= 1) {
			
			switch(args[0].toUpperCase()) {
			case "LOCAL":
				
				tester =  new Tester();
				tester.setGoogleAPIKey((new Configuration("google_api_key")).getString("google_api_key"));
				tester.setNumberOfAllowedConnections(100);
				tester.setNumberOfMappers(3);
				
				if (args.length >= 2) {
					
					switch (args[1].toUpperCase()) {
					case "FIXED_SINGLE":
						
						if (Constants.debugging) {
							System.out.println("Tester> start() = single_local_query");
						}
						
						if (!default_queries.isEmpty()) {
							tester.localQuery(default_queries.get(0));
						}
						
						break;
						
					case "FIXED_MULTIPLE":
						
						if (Constants.debugging) {
							System.out.println("Tester> start() = multiple_local_queries");
						}
						
						if (!default_queries.isEmpty()) {
							tester.localQuery(default_queries);
						}
						
						break;
						
					case "INTERACTIVE":
						
						Query query;
						
						do {
							
							query = getUserQuery(scanner);
							
							if (query != null) {
								tester.localQuery(query);
							}
						} while (query != null);
						
					}
					
				}
				
				break;
				
			case "REMOTE":
				
				tester =  new Tester();
				
				if (args.length >= 2) {
					
					SocketInformation socket = new SocketInformation(
							getUserInput(scanner, "host: "),
							Integer.parseInt(getUserInput(scanner, "port: ")));
					
					if (socket != null) {
						
						switch (args[1].toUpperCase()) {
						case "FIXED_SINGLE":
							
							if (Constants.debugging) {
								System.out.println("Tester> start() = single_remote_query");
							}
							
							if (!default_queries.isEmpty()) {
								tester.query(socket, default_queries.get(0));
							}
							
							break;
							
						case "FIXED_MULTIPLE":
							
							if (Constants.debugging) {
								System.out.println("Tester> start() = multiple_remote_queries");
							}
							
							if (!default_queries.isEmpty()) {
								tester.query(socket, default_queries);
							}
							
							break;
							
						case "INTERACTIVE":
							
							Query query;
							
							do {
								
								query = getUserQuery(scanner);
								
								if (query != null) {
									tester.query(socket, query);
								}
							} while (query != null);
							
						}
						
					}
					
				}

				break;
				
			}
			
		}
		
	}
	
	protected static String getUserInput(Scanner scanner, String prompt) {
		
		System.out.print("Tester> " + prompt);
		return scanner.nextLine();
		
	}
	
	protected static Query getUserQuery(Scanner scanner) {
		
		do {
			
			try {
				
				return new Query(
						new Coordinates(
								Double.parseDouble(getUserInput(scanner, "src_lat: ")),
								Double.parseDouble(getUserInput(scanner, "src_lon: "))),
						new Coordinates(
								Double.parseDouble(getUserInput(scanner, "dst_lat: ")),
								Double.parseDouble(getUserInput(scanner, "dst_lon: "))));
				
			} catch (NumberFormatException ex) {
				if (getUserInput(scanner, "Invalid input, try again (Y|n): ").toUpperCase().equals("N")) {
					return null;
				}
			}
			
		} while (true);
				
	}
	
	/*
	 * The default list of queries to be tested.
	 * Can contain duplicates.
	 */
	protected static final ArrayList<Query> getDefaultQueries() {
		
		ArrayList<Query> queries = new ArrayList<Query>();
		
		queries.add(new Query(
				new Coordinates(37.983810, 23.727539),
				new Coordinates(38.012097, 23.772572)));
		queries.add(new Query(
				new Coordinates(37.947688, 23.677652),
				new Coordinates(37.983810, 23.727539)));
		queries.add(new Query(
				new Coordinates(38.012097, 23.772572),
				new Coordinates(38.011989, 23.775551)));
		queries.add(new Query(
				new Coordinates(38.006148, 23.783695),
				new Coordinates(38.012209, 23.780540)));
		queries.add(new Query(
				new Coordinates(38.011989, 23.775551),
				new Coordinates(38.012097, 23.772572)));
		queries.add(new Query(
				new Coordinates(37.981349, 23.727926),
				new Coordinates(37.947688, 23.677652)));
		queries.add(new Query(
				new Coordinates(37.944402, 23.712691),
				new Coordinates(37.944317, 23.709623)));
		queries.add(new Query(
				new Coordinates(38.004890, 23.775594),
				new Coordinates(38.009624, 23.781903)));
		queries.add(new Query(
				new Coordinates(37.983810, 23.727539),
				new Coordinates(37.980383, 23.732884)));
		queries.add(new Query(
				new Coordinates(37.982550, 23.728462),
				new Coordinates(37.982872, 23.731853)));
		queries.add(new Query(
				new Coordinates(37.985036, 23.727089),
				new Coordinates(37.980487, 23.731230)));
		
		return queries;
		
	}
	
}

class ServerInitializer {

	public static final String localhost    = "127.0.0.1";
	public static final int    lowest_port  = 4001;
	public static final int    highest_port = 8000;
	
	protected final IDPool ports_pool;
	
	protected ArrayList<Entry<RunnableServer>> mappers;
	protected Entry<RunnableServer>            reducer;
	protected Entry<RunnableServer>            master;
	
	protected final int port_offset;
	protected final int no_mappers;	
	private boolean     is_running;
	
	public ServerInitializer(int no_mappers) {
		
		this.no_mappers = no_mappers;
		/*
		 * Create a pool to get unique ports for all servers.
		 */
		
		int pool_size = this.no_mappers + 2;
		
		this.ports_pool = new IDPool(pool_size);
		this.mappers    = new ArrayList<Entry<RunnableServer>>();
		this.is_running = false;
		this.port_offset = (new Random()).nextInt(
				(highest_port - lowest_port) - pool_size) + lowest_port;
		
	}
	
	/*
	 * Returns the master's socket information as the only server
	 * information available to the user.
	 */
	public SocketInformation getServerSocket() {
		
		if (this.master != null) {
			return this.master.getSocketInformation();
		}
		
		return null;
		
	}
	
	public boolean isRunning() {
		return this.is_running;
	}
	
	public void start() {
		
		if (Constants.debugging) {
			System.out.println("Tester> is_set(master) = " + (this.master != null));
			System.out.println("Tester> is_set(reducer) = " + (this.reducer != null));
			System.out.println("Tester> no_mappers: " + this.mappers.size());
			System.out.println("Tester> is_servers_running() = " + this.isRunning());
		}
		
		if (!this.isRunning() && this.master != null) {
			
			this.is_running = true;
			
			/*
			 * We first create the list of mappers to configure the master.
			 */
			Master master = (Master)(this.master.getServer().getServer());
			ArrayList<SocketInformation> mapper_sockets = new ArrayList<SocketInformation>();
			
			for (Entry<RunnableServer> mapper : this.mappers) {
				
				mapper_sockets.add(mapper.getSocketInformation());
				mapper.getServer().start();
				
			}
			master.setMappers(mapper_sockets);
			
			/*
			 * We configure the reducer that the master is going to use.
			 */
			if (this.reducer != null) {
				
				master.setReducer(this.reducer.getSocketInformation());
				this.reducer.getServer().start();
				
			}
			
			this.master.getServer().start();
			
		}
			
	}
	
	public void stop() {
		
		if (this.isRunning()) {
			
			this.master.getServer().interrupt();
			
			if (this.reducer != null) {
				this.reducer.getServer().interrupt();
			}
			
			for (Entry<RunnableServer> mapper : this.mappers) {
				mapper.getServer().interrupt();
			}
			
			this.is_running = false;
			
		}
		
	}
	
	/*
	 * While the initializer is not running we can set or unset
	 * the server information the it is going to use.
	 */
	
	public boolean setReducer(int port) {
		
		if (Constants.debugging) {
			System.out.println("Tester> reducer_port: " + port);
			System.out.println("Tester> set_reducer()");
		}
		
		if (!this.isRunning() && port >= lowest_port) {
			
			Reducer reducer = new Reducer();
			reducer.setPort(port);
			
			this.reducer = new Entry<RunnableServer>(
					new SocketInformation(localhost, port),
					new RunnableServer(reducer));
			
			return true;
		}
		
		return false;
		
	}
	
	public boolean setReducer() {
		
		int port = this.getAvailablePort();
		
		if (port >= 0) {
			return this.setReducer(port);
		}
		else {
			return false;
		}
		
	}
	
	public Entry<RunnableServer> unsetReducer() {
		
		if (!this.isRunning() && this.reducer != null) {
			
			Entry<RunnableServer> reducer = this.reducer;
			this.reducer = null;
			this.releasePort(reducer.getSocketInformation().getPort());
			return reducer;
			
		}
		
		return null;
		
	}
	
	public boolean addMapper(int port, String google_api_key) {
		
		if (Constants.debugging) {
			System.out.println("Tester> mapper_port: " + port);
			System.out.println("Tester> add_mapper(" + (this.mappers.size() + 1) + ")");
		}
		
		if (!this.isRunning() &&
				this.mappers.size() < this.no_mappers &&
				port >= lowest_port) {
			
			Mapper mapper = new Mapper(google_api_key);
			mapper.setPort(port);
			
			this.mappers.add(new Entry<RunnableServer>(
					new SocketInformation(localhost, port),
					new RunnableServer(mapper)));
			
			return true;
			
		}
		
		return false;
		
	}
	
	public boolean addMapper(String google_api_key) {
		
		int port = this.getAvailablePort();
		
		if (port >= 0) {
			return this.addMapper(port, google_api_key);
		}
		else {
			return false;
		}
		
	}
	
	public Entry<RunnableServer> removeMapper(int i) {
		
		if (!this.isRunning() &&
				i > 0 &&
				i < this.mappers.size()) {
			
			Entry<RunnableServer> entry = this.mappers.remove(i);
			this.releasePort(entry.getSocketInformation().getPort());
			return entry;
			
		}
		
		return null;
		
	}
	
	public boolean setMaster(int port, int no_connections) {
		
		if (Constants.debugging) {
			System.out.println("Tester> master_port: " + port);
			System.out.println("Tester> no_connections: " + no_connections);
			System.out.println("Tester> set_master()");
		}
		
		if (!this.isRunning() && port >= lowest_port) {
			
			Master master = new Master();
			master.setNumberOfAllowedConnections(no_connections);
			master.setPort(port);
			
			this.master = new Entry<RunnableServer>(
					new SocketInformation(localhost, port),
					new RunnableServer(master));
			
			return true;
		}
		
		return false;
		
	}
	
	public boolean setMaster(int no_connections) {
		
		int port = this.getAvailablePort();
		
		if (port >= 0) {
			return this.setMaster(port, no_connections);
		}
		else {
			return false;
		}
		
	}
	
	public Entry<RunnableServer> unsetMaster() {
		
		if (!this.isRunning() && this.master != null) {
			
			Entry<RunnableServer> master = this.master;
			this.master = null;
			this.releasePort(master.getSocketInformation().getPort());
			return master;
			
		}
		
		return null;
		
	}
	
	/*
	 * Uses the ID pool to return unique ports when requested.
	 */
	protected int getAvailablePort() {
		
		int id  = this.ports_pool.getAvailableID();
		
		if (id >= 0) {
			return id + this.port_offset;
		}
		else {
			return -1;
		}
		
	}
	
	protected void releasePort(int port) {
		this.ports_pool.release(port - this.port_offset);
	}

}

/*
 * Creates an overlay class to start each server as a thread.
 */
class RunnableServer extends Thread {
	
	private final ServerManager server;
	
	public RunnableServer(ServerManager server) {
		this.server = server;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		
		super.interrupt();
		server.close();
		
	}
	
	public ServerManager getServer() {
		return this.server;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		this.server.start();
	}
	
}

/*
 * Creates a structure that used to encapsulate a RunnableServer
 * and its socket information.
 */
class Entry<T extends RunnableServer> {

	protected final SocketInformation socket;
	protected final T server;
	
	public Entry(SocketInformation socket, T server) {
		
		this.socket = socket;
		this.server = server;
		
	}
	
	public SocketInformation getSocketInformation() {
		return this.socket;
	}
	
	public T getServer() {
		return this.server;
	}
	
}
