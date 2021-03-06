package direction_api.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayDeque;

import direction_api.common.Configuration;

/**
 * @author p3100161, p3130029
 *
 * Creates an abstract ServerManager object that acts as a
 * base for the actual servers eg. mappers.
 */

public abstract class ServerManager {

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
	
	protected Configuration configuration;
	protected ServerSocket  server_socket;
	protected int           port;
	protected int           no_connections;
	
	private ArrayDeque<Server> threads;
	
	/**
	 * This method should return the actual thread that is going 
	 * to be responsible for the communication with the client.
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	protected abstract Server initServerThread(Socket socket)
			throws IOException;
	
	/**
	 * The default constructors initialize the ServerManager object
	 * with the default variables and according to a default
	 * configuration file if it exists.
	 */
	public ServerManager() {
		
		this.setPort(default_port);
		this.setNumberOfAllowedConnections(default_no_connections);

		/*
		 * To prevent memory leaks we should keep track of the running
		 * threads in case we need to interrupt them.
		 */
		this.threads = new ArrayDeque<Server>();
		
	}
	
	/**
	 * The various configuration options can also be provided through
	 * the use of a file.
	 * @param path
	 */
	public ServerManager(String path) {
		this();

		this.loadConfigurationFile(path);
		
	}
	
	public ServerManager(URL resource) {
		this();
		
		if (resource != null) {
			this.loadConfigurationFile(resource.getPath());
		}
		
	}
	
	public void loadConfigurationFile(String path) {
		
		this.configuration = new Configuration(path);
		
		this.setPort(this.configuration.getInt("port", this.port));
		this.setNumberOfAllowedConnections(
				this.configuration.getInt("number_of_allowed_connections", this.no_connections));
		
	}
	
	public String getConfigurationPath() {
		
		if (this.configuration != null) {
			return this.configuration.getPath();
		}
		else {
			return null;
		}
		
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setPort(int port) {
		this.port =  port;
	}
	
	public void setNumberOfAllowedConnections(int no_connections) {
		this.no_connections = no_connections;
	}
	
	public void start() {
		
		try {
			
			/*
			 * We create the server socket where clients can request
			 * for a connection with the server.
			 */
			this.server_socket = new ServerSocket(this.port, this.no_connections);
			
			while (true) {
				
				Server thread = initServerThread(this.server_socket.accept());
				
				synchronized (this.threads) {
					this.threads.addLast(thread);
				}
				
				thread.start();
				
			}
			
		} catch (SocketException ex) {
			
			if (this.server_socket.isClosed()) {
				
				/*
				 * If the socket was closed we consider the event normal.
				 */
				if (Constants.debugging) {
					System.out.println("Destructor> is_stopped(" +
							this.getClass().getSimpleName() + ") = " + this.server_socket.isClosed());
				}
				
			}
			else {
				ex.printStackTrace();
			}			
			
		} catch (IOException ex) {
			
			ex.printStackTrace();
			
		} finally {
			
			this.close();
			
		}
		
	}
	
	public void close() {
		
		try {
			
			if (!this.server_socket.isClosed()) {
				this.server_socket.close();
			}
			
			synchronized (this.threads) {
				
				for (Server thread : this.threads) {
					thread.interrupt();
				}
				
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
	}
	
}
