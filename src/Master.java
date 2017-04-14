import java.io.IOException;
import java.net.ServerSocket;

public class Master {

	private Configuration configuration;
	private ServerSocket server_socket;
	private SocketStructure[] mapper_sockets;
	private SocketStructure reducer_socket;
	private IDRange client_ids;
	
	public Master() {
		// Creates a master based on the default configuration file.
		
		this("master.properties");
		
	}
	
	public Master(String configuration_filename) {
		// Creates a master based on a properties file.
		
		try {
			
			this.configuration    = new Configuration(configuration_filename);
			int number_of_mappers = this.configuration.getInt("number_of_mappers", 0);
			// The number of mappers is given explicitly for validation.
			
			int number_of_allowed_connections = this.configuration.getInt(
				"number_of_allowed_connections",
				100
			);
			
			this.client_ids = new IDRange(number_of_allowed_connections);
			
			this.reducer_socket = new SocketStructure(
				this.configuration.getString("reducer_host", "127.0.0.1"),
				this.configuration.getInt("reducer_port", 4322)
			);
			
			if (number_of_mappers > 0) {
				
				this.mapper_sockets = new SocketStructure[number_of_mappers];
				
				for (int i = 0; i < number_of_mappers; i ++) {
					// If a mapper's setting are not given the validation is going to fail.
					
					String mapper_host_key = "mapper_host." + (i + 1);
					String mapper_port_key = "mapper_port." + (i + 1);
					
					if (
						   !this.configuration.containsKey(mapper_host_key)
						|| !this.configuration.containsKey(mapper_port_key)
					) {
						throw new IOException("Undefined Mapper: " + (i + 1));
					}
					else {
						this.mapper_sockets[i] = new SocketStructure(
							this.configuration.getString(mapper_host_key),
							this.configuration.getInt(mapper_port_key)
						); 
					}
				}
			}
			
			initialize(
				// The mapper's listening port.
				this.configuration.getInt("port", 4323),
				// The maximum allowed number of connections.
				this.configuration.getInt("number_of_allowed_connections", 0)
			);
			
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
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
			
			MasterServerThread t = new MasterServerThread(
				this.server_socket.accept(),
				this.mapper_sockets,
				this.reducer_socket,
				this.client_ids
			);
			t.start();
			
		}

	}

	public static void main(String args[]) {
		
		new Master();
		
	}
	
}
