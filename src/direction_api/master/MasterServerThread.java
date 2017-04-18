package direction_api.master;
import java.io.IOException;
import java.util.HashMap;

import direction_api.common.ServerThread;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;
import direction_api.common.structures.SocketInformation;

public class MasterServerThread extends ServerThread {
	// This thread is being called by the mapper in order to execute the request.
	
	private final SocketInformation[] mapper_sockets;
	private final SocketInformation reducer_socket;
	private final IDRange client_ids;
	private final HashMap<Query, RoutesList> cached_routes;
	
	public MasterServerThread(
			java.net.Socket request_socket, 
			SocketInformation[] mapper_sockets, 
			SocketInformation reducer_socket, 
			IDRange client_ids,
			HashMap<Query, RoutesList> cached_routes
			) throws IOException {
		super(request_socket);
		
		this.mapper_sockets = mapper_sockets;
		this.reducer_socket = reducer_socket;
		this.client_ids     = client_ids;
		this.cached_routes  = cached_routes;
		
	}
	
	protected boolean contactMapper(
			SocketInformation socket,
			int id,
			Query query,
			SocketInformation reducer_socket) {
		
		MasterClientMapperThread thread;
		
		boolean success = false;
		
		try {
			
			thread = new MasterClientMapperThread(
					socket, id, query, reducer_socket);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(1000);
			}
			
			success = true;
			
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		
		return success;
		
	}
	
	protected RoutesList contactReducer(SocketInformation socket, int id) {
		
		MasterClientReducerThread thread;
		
		RoutesList routes = null;
		
		try {
			
			thread = new MasterClientReducerThread(socket, id);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(1000);
			}
			
			routes = thread.getRoutes();
			
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		
		return routes;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		int id;
		synchronized (this.client_ids) {
			id = this.client_ids.getAvailableID();
		}
		
		RoutesList routes = null;
		
		try {
			
			Query query = (Query)(this.in.readObject()); // TODO: Check class type first.
			
			synchronized (this.cached_routes) {
				
				if (this.cached_routes.containsKey(query)) {
					routes = this.cached_routes.get(query);
				}
				
			}
			
			if (!(routes instanceof RoutesList)) {
				
				if (this.contactMapper(
						this.mapper_sockets[query.hashCode() % this.mapper_sockets.length],
						id, query, reducer_socket)) {
					
					routes = this.contactReducer(reducer_socket, id);
					
					// TODO: Release ID.
					
					synchronized (this.cached_routes) {
						this.cached_routes.put(query, routes);
						
						// TODO: Keep only recent.
					}
					
				}

			}
			
			if (!(routes instanceof RoutesList)) {
				routes = new RoutesList();
			}
			
			this.out.writeObject(routes);
			this.out.flush();
			
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();			
		}
		
	}

}
