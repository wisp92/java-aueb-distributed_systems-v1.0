import java.io.IOException;
import java.net.Socket;

public class MasterServerThread extends ServerThread {
	// This thread is being called by the mapper in order to execute the request.
	
	private SocketStructure[] mapper_sockets;
	private SocketStructure reducer_socket;
	private IDRange client_ids;
	
	public MasterServerThread(
		Socket request_socket,
		SocketStructure[] mapper_sockets,
		SocketStructure reducer_socket,
		IDRange client_ids
	) throws IOException{
		super(request_socket);
		
		this.mapper_sockets = mapper_sockets;
		this.reducer_socket = reducer_socket;
		this.client_ids     = client_ids;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		int id;
		boolean completed = true;
		
		synchronized (this.client_ids) {
			id = this.client_ids.getAvailableClientID();
		}
		
		try {
			
			Query route_query = (Query)(this.in.readObject());
			
			// TODO: Should check cache first.
			
			MasterClientMapperThread t = new MasterClientMapperThread(
				// TODO: Should choose mapper according to hash.
				this.mapper_sockets[0],
				id,
				route_query,
				reducer_socket
			);
			t.start();
			
			while (!t.isCompleted()) {
				sleep(1000);
			}
			
			// TODO: Should communicate with reducer to get routes and release the id (synchronized).
			
		} catch (IOException | InterruptedException | ClassNotFoundException ex) {
			ex.printStackTrace();
			
			completed = false;
			
		} finally {
			
			// TODO: Should return routes.
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}

}
