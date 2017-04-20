package direction_api.mobile;
import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class MobileClientMasterThread extends Client {
	// Communicates with a MapperThread in order to send the request.
	
	public MobileClientMasterThread(SocketInformation socket)
			throws IOException, UnknownHostException {
		super(socket);
	}
	
	@Override
	protected void task() throws IOException {
		
		Query query = new Query(
			new Coordinates(37.983810, 23.727539),
			new Coordinates(38.012097, 23.772572)
		);
		
		this.out.writeObject(query);
		this.out.flush();
			
		try {
			
			for (String route : (RoutesList)this.in.readObject()) {
				System.out.println(route);
			}
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		
		this.isCompleted();
		
	}
	
	public static void main(String args[]) {
		
		MobileClientMasterThread thread;
		
		try {
			
			thread = new MobileClientMasterThread(
				new SocketInformation("127.0.0.1", 4323)
			);
			thread.start();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
}