import java.io.*;
import java.net.*;

public class Reducer {

	private Configuration configuration;
	private ServerSocket server_socket;
	
	public Reducer() {
		// Creates a worker based on the default configuration file.
		
		this("reducer.properties");
		
	}
	
	public Reducer(String configuration_filename) {
		// Creates a worker based on a properties file.
		
		try {
			
			this.configuration = new Configuration(configuration_filename);
			
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
	
	public Reducer(int server_socket) {
		this(server_socket, 0);
	}
	
	public Reducer(int server_socket, int number_of_allowed_connections) {
		
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
			
			ReducerServerThread t = new ReducerServerThread(this.server_socket.accept());
			t.start();
			
		}

	}

	public static void main(String args[]) {
		
		new Reducer();
		
	}
	
}
