package direction_api.mobile;
import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.Constants;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.Route;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 *
 * A temporary implementation that plays the role of the user communicating
 * with the master server.
 */

public class MobileToMasterClient extends Client {
	
	protected final Query query;
	
	public MobileToMasterClient(SocketInformation socket, Query query)
			throws IOException, UnknownHostException {
		super(socket);
		
		this.query = query;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Client#task()
	 */
	@Override
	protected void task() throws IOException {
		
		if (Constants.debugging) {
			System.out.println("User> query: " + this.query.toString());
		}
		
		this.out.writeObject(this.query);
		this.out.flush();
			
		try {
			
			Route route = this.readObject(this.in.readObject(), Route.class);
			
			if (Constants.debugging) {
				System.out.println("User> route: " + route.toString());
			}
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String args[]) {
		
		MobileToMasterClient thread;
		
		String host = "127.0.0.1";
		int    port = 4500;
		
		try {
			
			if (Constants.debugging) {
				System.out.println("User> connect() " + host + ":" + port);
			}
			
			thread = new MobileToMasterClient(
					new SocketInformation(host, port), new Query(
							new Coordinates(37.983810, 23.727539),
							new Coordinates(38.012097, 23.772572)));
			thread.start();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
}