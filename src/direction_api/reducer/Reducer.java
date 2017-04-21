package direction_api.reducer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.net.Socket;

import direction_api.common.structures.QueryResults;
import direction_api.common.ServerManager;
import direction_api.common.Constants;
import direction_api.common.Server;

/**
 * @author p3100161, p3130029
 *
 * Creates a Reducer object as direct implementation of ServerManager object.
 * A Reducer object keeps a central HashMap that all the ReducerServer object's
 * should access to store and retrieve the results of the queries.
 */

public class Reducer extends ServerManager {

	public static final String default_conf_file = "reducer.properties";
	
	protected HashMap<Integer, QueryResults> stored;
	
	public Reducer() {
		super(Reducer.class.getResource(default_conf_file));	
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#initServerThread(java.net.Socket)
	 */
	protected Server initServerThread(Socket socket) throws IOException {
		return new ReducerServer(socket, this.stored);
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#start()
	 */
	@Override
	public void start() {
		
		/*
		 * The hashmap should be initialized every time the server starts.
		 */
		this.stored = new HashMap<Integer, QueryResults>();
		
		if (Constants.debugging) {
			if (this.configuration != null) {
				System.out.println("Reducer> configuration_file: " + this.configuration.getPath());
			}
			System.out.println("Reducer> server_port: " + this.port);
			System.out.println("Reducer> start()");
		}
		
		super.start();
		
	}
	
	public static void main(String args[]) {
		
		String default_conf_file = "reducer.properties";
		
		Reducer reducer = new Reducer();
		
		if (reducer.getConfigurationPath() == null) {
			
			if ((new File(default_conf_file).exists())) {
				reducer.loadConfigurationFile(default_conf_file);
			}
			else {
				System.out.println("Notice: No configuration file <" +
						default_conf_file + "> was loaded.");
			}
			
		}
		
		reducer.start();	
		
	}
	
}