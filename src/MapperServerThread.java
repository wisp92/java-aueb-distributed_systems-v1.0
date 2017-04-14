import java.io.IOException;
import java.net.Socket;

public class MapperServerThread extends ServerThread {
	
	private RoutesDB routes_db;	
	
	public MapperServerThread(Socket request_socket, RoutesDB routes_db) throws IOException{
		super(request_socket);
		
		this.routes_db = routes_db;
	}
	
	@Override
	protected void task() throws IOException {
		
		boolean completed = true;
		
		try {
			
			// Get information from master.
			int id                         = this.in.readInt();
			Query route_query              = (Query)(this.in.readObject());
			SocketStructure reducer_socket = (SocketStructure)(this.in.readObject());
			
			Routes routes = this.routes_db.searchRoute(route_query);
			
			// TODO: If routes is empty should communicate with Google API.
			
			MapperClientReducerThread t = new MapperClientReducerThread(reducer_socket, id, routes);
			t.start();
			
			while (!t.isCompleted()) {
				sleep(1000);
			}
			
		} catch (ClassNotFoundException | InterruptedException ex) {
			
			completed = false;
			
			ex.printStackTrace();
			
		} finally {
			
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
		
	}

}
