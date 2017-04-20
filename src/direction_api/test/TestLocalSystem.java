package direction_api.test;

import java.io.IOException;

import direction_api.common.Configuration;
import direction_api.common.Constants;
import direction_api.common.ServerManager;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.SocketInformation;
import direction_api.mapper.Mapper;
import direction_api.master.Master;
import direction_api.mobile.MobileMasterClient;
import direction_api.reducer.Reducer;

/**
 * @author p3100161, p3130029
 *
 * Creates a object that runs at checks the functionality of the system
 * distributes on a local machine.
 */

public class TestLocalSystem {

	public static void main (String[] args) {
		
		String host = "127.0.0.1";
		
		MobileMasterClient thread;
		
		try {
			
			Start<Master> master   = new Start<Master>(new Master());
			master.start();
			Start<Mapper> mapper   = new Start<Mapper>(new Mapper(
					(new Configuration("google_api_key")).getString("google_api_key")));
			mapper.start();
			Start<Reducer> reducer = new Start<Reducer>(new Reducer());
			reducer.start();
			
			if (Constants.debugging) {
				System.out.println("User> connect(" + host + ":" + master.getServer().getPort() + ")");
			}
			
			thread = new MobileMasterClient(
					new SocketInformation(host, master.getServer().getPort()), new Query(
							new Coordinates(37.983810, 23.727539),
							new Coordinates(38.012097, 23.772572)));
			thread.start();
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
	}
	
}

class Start<T extends ServerManager> extends Thread {
	
	private final T server;
	
	public Start(T server) {
		this.server = server;
	}
	
	public T getServer() {
		return this.server;
	}
	
	public void run() {
		this.server.start();
	}
	
}
