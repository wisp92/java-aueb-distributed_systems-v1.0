package direction_api.master;
import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.structures.Query;
import direction_api.common.structures.SocketInformation;

public class MasterClientMapperThread extends Client {
	
	protected final Query route_query;
	protected final SocketInformation reducer_socket;
	protected final int id;
	
	public MasterClientMapperThread(
			SocketInformation socket, 
			int id, 
			Query route_query, 
			SocketInformation reducer_socket
			) throws IOException, UnknownHostException {
		super(socket);
		
		this.route_query    = route_query;
		this.reducer_socket = reducer_socket;
		this.id             = id;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		this.out.writeInt(this.id);
		this.out.flush();
		
		this.out.writeObject(this.route_query);
		this.out.flush();
		
		this.out.writeObject(this.reducer_socket);
		this.out.flush();
		
		// TODO: Check if need to resent.
		this.in.readBoolean();
		
		this.setCompleted();
		
	}

}
