package direction_api.master;
import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.ClientThread;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class MasterClientReducerThread extends ClientThread {
	
	public static final int default_type_of_connection = 1;
	
	protected final int id;
	private RoutesList routes;
	
	public MasterClientReducerThread(SocketInformation socket, int id)
			throws IOException, UnknownHostException {
		super(socket);
		
		this.id     = id;
		this.routes = null;
		
	}
	
	public RoutesList getRoutes() {
		return this.routes;
	}
	
	@Override
	protected void task() throws IOException {
		
		this.out.writeInt(default_type_of_connection);
		this.out.flush();
		
		this.out.writeInt(this.id);
		this.out.flush();
		
		try {
			
			this.routes = (RoutesList)this.in.readObject();
		
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();			
		}
		
		this.setCompleted();
		
	}

}
