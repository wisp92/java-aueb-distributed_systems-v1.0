import java.io.*;
import java.net.*;

public class Mapper {

	private Configuration configuration;
	private ServerSocket server_socket;
	private RoutesDB routes_db;
	
	public Mapper() {
		// Creates a worker based on the default configuration file.
		
		this("mapper.properties");
		
	}
	
	public Mapper(String configuration_filename) {
		// Creates a worker based on a properties file.
		
		try {
			
			this.configuration = new Configuration(configuration_filename);
			
			this.routes_db = new RoutesDB(
				this.configuration.getString("routes_db", "routes_db")
			);
			
			initialize(
				// The mapper's listening port.
				this.configuration.getInt("port", 4321),
				// The maximum allowed number of connections.
				this.configuration.getInt("number_of_allowed_connections", 0)
			);
			 
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public Mapper(int server_socket, String routes_db) {
		this(server_socket, 0, routes_db);
	}
	
	public Mapper(int server_socket, int number_of_allowed_connections, String routes_db) {
		
		this.routes_db = new RoutesDB(routes_db);
		
		initialize(server_socket, number_of_allowed_connections);
	}
	
	private void initialize(int port, int number_of_allowed_connections) {
	// Is responsible for creating and closing the server socket.
		
		try {
			
			this.server_socket = new ServerSocket(port, number_of_allowed_connections);
			
			waitForTaskThread();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			
			try {
				this.server_socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
	}
	
	void waitForTaskThread() throws IOException {
	// Waits for connections.
		
		while (true) {
			
			MapperServerThread t = new MapperServerThread(this.server_socket.accept(), this.routes_db);
			t.start();
			
		}

	}

	public static void main(String args[]) {
		
		new Mapper();
		
	}
	
}
