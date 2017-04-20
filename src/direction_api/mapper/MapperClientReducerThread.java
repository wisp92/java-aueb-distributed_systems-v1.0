package direction_api.mapper;
import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class MapperClientReducerThread extends Client {
	
	public static final int default_type_of_connection = 0;
	
	protected final RoutesList routes;
	protected final int id;
	
	public MapperClientReducerThread(SocketInformation socket, int id, RoutesList routes)
			throws IOException, UnknownHostException {
		super(socket);
		
		this.id     = id;
		this.routes = routes;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		this.out.writeInt(default_type_of_connection);
		this.out.flush();
		
		this.out.writeInt(this.id);
		this.out.flush();
		
		this.out.writeObject(this.routes);
		this.out.flush();
		
		// TODO: Check if need to resent.
		this.in.readBoolean();
		
		this.setCompleted();
		
	}

}
