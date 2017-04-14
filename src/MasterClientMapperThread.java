import java.io.IOException;
import java.net.UnknownHostException;

public class MasterClientMapperThread extends ClientThread {
	
	protected Query route_query;
	protected SocketStructure reducer_socket;
	protected int id;
	
	public MasterClientMapperThread(
		SocketStructure socket,
		int id,
		Query route_query,
		SocketStructure reducer_socket
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
		
		// TODO: Remove this.
		System.out.println("Mapper replied: " + this.in.readBoolean());
		
		this.setCompleted();
		
	}

}
