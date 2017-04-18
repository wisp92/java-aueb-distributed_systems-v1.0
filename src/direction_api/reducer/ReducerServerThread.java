package direction_api.reducer;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import direction_api.common.ServerThread;
import direction_api.common.structures.RoutesList;

public class ReducerServerThread extends ServerThread {
	
	private final HashMap<Integer, RoutesList> stored_routes;
	
	public ReducerServerThread(Socket socket, HashMap<Integer, RoutesList> stored_routes)
			throws IOException {
		super(socket);
		
		this.stored_routes = stored_routes;
	}
	
	@Override
	protected void task() throws IOException {
		
		try {
			
			int type_of_connection = this.in.readInt();
			int id = this.in.readInt();
			
			switch(type_of_connection) {
				case 0:
					
					RoutesList routes = (RoutesList)(this.in.readObject());
					
					synchronized (this.stored_routes) {
						stored_routes.put(id, routes);
					}
					
					this.out.writeBoolean(true);
					this.out.flush();
					
					break;
					
				case 1:
					
					synchronized (this.stored_routes) { // TODO: Store to variable first.
						
						if (stored_routes.containsKey(id)) {
							this.out.writeObject(stored_routes.get(id));
						}
						else {
							this.out.writeObject(new RoutesList());
						}
						this.out.flush();
						
					}
					
					break;
					
			}
			
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
			
	}

}
