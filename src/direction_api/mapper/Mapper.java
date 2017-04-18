package direction_api.mapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayDeque;

import direction_api.common.Configuration;

/**
 * 
 * @author p3100161, p3130029
 *
 */


public class Mapper {

	public static final String default_database_name  = "mapper.sqlite3";
	public static final String default_conf_file      = "mapper.properties";
	/*
	 * If default_port is set as 0 then a random port is going
	 * to be used to create the server socket in case no port
	 * has been set.
	 */
	public static final int default_port = 0;
	/*
	 * If default_no_connections is set as 0 then no quote to
	 * the number of incoming connections is going to be set
	 * in case the number has not been provided.
	 */
	public static final int default_no_connections = 0;
	
	private Configuration  configuration;
	private ServerSocket   server_socket;
	private MapperDatabase database;
	private int            port;
	private int            no_connections;
	
	/**
	 * The default constructors initialized the the Mapper object
	 * with the default variables and according to a default
	 * configuration file if it exists.
	 */
	public Mapper() {
		
		URL resource = Mapper.class.getResource(default_conf_file);
		
		this.setPort(default_port);
		this.setNumberOfAllowedConnections(default_no_connections);
		this.setDatabase(default_database_name);
		
		if (resource instanceof URL) {
			this.loadConfigurationFile(resource.getPath());
		}

	}
	
	/**
	 * The various configuration options can also be provided through
	 * the use of a file.
	 * @param path
	 */
	public Mapper(String path) {
		this();
		
		if (this.configuration.getPath() != path) {
			this.loadConfigurationFile(path);
		}
		
	}
	
	public void loadConfigurationFile(String path) {
		
		this.configuration = new Configuration(path);
		
		if (this.configuration.contains("database_name")) {
				this.setDatabase(this.configuration.getString("database_name"));
		}
		
		this.setPort(this.configuration.getInt("port", this.port));
		this.setNumberOfAllowedConnections(
				this.configuration.getInt("number_of_allowed_connections", this.no_connections));
		
	}
	
	public void setPort(int port) {
		this.port =  port;
	}
	
	public void setNumberOfAllowedConnections(int no_connections) {
		this.no_connections = no_connections;
	}
	
	public void setDatabase(String database_name) {
		this.database = new MapperDatabase(database_name);
	}
	
	public void start() {
		
		ArrayDeque<MapperServerThread> threads = new ArrayDeque<MapperServerThread>();
		
		try {
			
			this.server_socket = new ServerSocket(this.port, this.no_connections);
			
			while (true) {
				
				MapperServerThread thread = new MapperServerThread(
						this.server_socket.accept(), this.database);
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
		
		Mapper mapper = new Mapper();
		mapper.start();
		
	}
	
}
