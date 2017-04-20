package direction_api.mapper;

import java.io.IOException;
import java.net.Socket;

import direction_api.common.ServerManager;
import direction_api.common.Configuration;
import direction_api.common.Constants;
import direction_api.common.Server;

/**
 * @author p3100161, p3130029
 *
 * Creates a Mapper object as direct implementation of ServerManager object.
 * A Mapper object should also initialize the central database that each of
 * its threads is going to search first in order to find a the possible route
 * for the query.
 */

public class Mapper extends ServerManager {

	public static final String default_database_name  = "mapper.sqlite3";
	public static final String default_conf_file      = "mapper.properties";
	
	private MapperDatabase database;
	/*
	 * Since its personal data should always be provided by
	 * the administrator.
	 */
	private final String google_api_key;
	
	public Mapper(String google_api_key) {
		super(Mapper.class.getResource(default_conf_file).getPath());
		
		this.setDatabase(default_database_name);
		this.google_api_key = google_api_key;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#loadConfigurationFile(java.lang.String)
	 */
	@Override
	public void loadConfigurationFile(String path) {
		super.loadConfigurationFile(path);
		
		if (this.configuration.contains("database_name")) {
				this.setDatabase(this.configuration.getString("database_name"));
		}
		
	}
	
	public void setDatabase(String database_name) {
		this.database = new MapperDatabase(database_name);
	}

	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#initServerThread(java.net.Socket)
	 */
	protected Server initServerThread(Socket socket) throws IOException {
		return new MapperServer(socket, this.database, this.google_api_key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.ServerManager#start()
	 */
	@Override
	public void start() {
		
		if (Constants.debugging) {
			System.out.println("Mapper> configuration_file: " + this.configuration.getPath());
			System.out.println("Mapper> server_port: " + this.port);
			System.out.println("Mapper> start()");
		}
		
		super.start();
		
	}
	
	public static void main(String args[]) {
		
		String google_api_key = null;
		
		/*
		 * We try to find the Google API key first in the program's
		 * arguments and then on a default location.
		 */
		if (args.length >= 1) {
			google_api_key = args[0];
		}
		else {
			google_api_key = (new Configuration(
					"google_api_key")).getString("google_api_key");
		}
		
		/*
		 * The server can start only by specifying the Google API key.
		 */
		if (google_api_key instanceof String) {
			
			Mapper mapper = new Mapper(google_api_key);
			mapper.start();
			
		}
		
	}
	
}
