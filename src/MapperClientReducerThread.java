import java.io.IOException;
import java.net.UnknownHostException;

public class MapperClientReducerThread extends ClientThread {
	
	protected Routes routes;
	protected int id;
	
	public MapperClientReducerThread(SocketStructure socket, int id, Routes routes) throws IOException, UnknownHostException {
		super(socket);
		
		this.id     = id;
		this.routes = routes;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		this.out.writeInt(this.id);
		this.out.flush();
		
		this.out.writeObject(this.routes);
		this.out.flush();
		
		// TODO: Remove this.
		System.out.println("Reducer replied: " + this.in.readBoolean());
		
		this.setCompleted();
		
	}

}
