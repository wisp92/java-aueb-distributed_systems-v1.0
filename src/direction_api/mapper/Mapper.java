package direction_api.mapper;
import java.io.*;
import java.net.*;
import java.util.ArrayDeque;

import direction_api.common.Configuration;

public class Mapper {

	public static final int default_port = 4321;
	public static final int default_no_connections = 0;
	public static final String default_database_name = "mapper.sqlite3";
	public static final String default_conf_file = "mapper.properties";
	
	private Configuration configuration;
	private ServerSocket server_socket;
	private MapperDatabase database;
	
	public Mapper() {
		this(Mapper.class.getResource(default_conf_file));
	}
	
	public Mapper(URL resource) {
			
		this.configuration = new Configuration(resource);
		
		this.database = new MapperDatabase(
				this.configuration.getString("database_name", default_database_name));
		
		this.start(
				this.configuration.getInt("port", default_port),
				this.configuration.getInt("number_of_allowed_connections", default_no_connections));
		
	}
	
	private void start(int port, int no_connections) {
		
		ArrayDeque<MapperServerThread> threads = new ArrayDeque<MapperServerThread>();
		
		try {
			
			this.server_socket = new ServerSocket(port, no_connections);
			
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
		new Mapper();
	}
	
}
