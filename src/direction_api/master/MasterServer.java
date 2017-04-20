package direction_api.master;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import direction_api.common.Constants;
import direction_api.common.IDPool;
import direction_api.common.LRUCache;
import direction_api.common.Server;
import direction_api.common.structures.Query;
import direction_api.common.structures.Route;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 *
 * Creates a server object responsible for supervising the communication with the
 * user and the other servers.
 */

public class MasterServer extends Server {
	
	protected final ArrayList<SocketInformation> mapper_sockets;
	protected final SocketInformation reducer_socket;
	protected final IDPool connections_id_pool;
	protected final LRUCache<Query, Route> cached_results;
	
	private int id;
	
	public MasterServer(
			Socket request_socket, 
			ArrayList<SocketInformation> mapper_sockets, 
			SocketInformation reducer_socket, 
			IDPool connections_id_pool,
			LRUCache<Query, Route> cached_results
			) throws IOException {
		super(request_socket);
		
		this.mapper_sockets      = mapper_sockets;
		this.reducer_socket      = reducer_socket;
		this.connections_id_pool = connections_id_pool;
		this.cached_results      = cached_results;
		/*
		 * It is important to set id to -1 before we requested
		 * one in order to know when to release it the close() method.
		 */
		this.id = -1;
	}
	
	protected boolean communicateWithMapper(
			SocketInformation socket,
			Query query,
			SocketInformation reducer_socket) {
		
		MasterToMapperClient thread;
		
		boolean success = false;
		
		try {
			
			/*
			 * We create a client thread to communicate with the selected
			 * mapper and wait for it to give some results.
			 */
			thread = new MasterToMapperClient(
					socket, this.id, query, reducer_socket);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(Constants.max_sleep_time);
			}
			
			/*
			 * If something went wrong we can know by checking the output
			 * of the isSuccess() method.
			 */
			success = thread.isSuccess();
			
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		
		return success;
		
	}
	
	protected Route communicateWithReducer(SocketInformation socket) {
		
		MasterToReducerClient thread;
		
		Route route = null;
		
		try {
			
			/*
			 * If all ent well so far then we create a client thread to
			 * communicate with the reducer and retrieve the route.
			 * If something goes wrong we are going to catch an IOException
			 * and the route is going to be null.
			 */
			thread = new MasterToReducerClient(socket, this.id);
			thread.start();
			
			while (!thread.isCompleted()) {
				sleep(Constants.max_sleep_time);
			}
			
			route = thread.getRoute();
			
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		
		return route;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Server#task()
	 */
	@Override
	protected void task() throws IOException {
		
		Route route = null;
		
		try {
			
			/*
			 * First we should check if the result is available in the cache.
			 * If It is we can return it immediately without the need to request
			 * a connection id from the pool.
			 */
			Query query = this.readObject(this.in.readObject(), Query.class);
			
			if (Constants.debugging) {
				System.out.println("Master(S)> query: " + query.toString());
			}
			
			synchronized (this.cached_results) {
				
				if (this.cached_results.containsKey(query)) {
					route = this.cached_results.get(query, true);
				}
				
			}
			
			if (Constants.debugging) {
				System.out.println("Master(S)> cache_results: " + (route instanceof Route));
			}
			
			if (!(route instanceof Route)) {
				
				/*
				 * If we are not so lucky with the cache we request a unique id from the pool.
				 * If one is not available then we wait until one is released.
				 */
				
				if (Constants.debugging) {
					System.out.println("Master(S)> request_id()");
				}
				
				while (this.id < 0) {
					
					synchronized (this.connections_id_pool) {
						this.id = this.connections_id_pool.getAvailableID();
					}
					
					if (this.id < 0) {
						sleep(Constants.max_sleep_time);
					}
					
				}
				
				if (Constants.debugging) {
					System.out.println("Master(S)> connection_id: " + this.id);
				}
				
				/*
				 * We then select the correct mapper based on the query's hash an the
				 * mapper's id.
				 */
				
				int selected_mapper = query.hashCode() % this.mapper_sockets.size();
				
				if (Constants.debugging) {
					System.out.println("Master(S)> selected_mapper: " +
							this.mapper_sockets.get(selected_mapper).toString() +
							"(" + selected_mapper + ")");
					System.out.println("Master(S)> connect(selected_mapper)");
				}
				
				if (this.communicateWithMapper(
						this.mapper_sockets.get(selected_mapper), query, reducer_socket)) {
					
					/*
					 * If all went well we use the same id to retrieve the result from the reducer.
					 * Notice that until the reducer actually has stored the result we are not going
					 * to get a reply from the mapper.
					 */
					
					if (Constants.debugging) {
						System.out.println("Master(S)> connect(reducer_socket)");
					}
					
					route = this.communicateWithReducer(reducer_socket);
					
					if (Constants.debugging) {
						System.out.println("Master(S)> route: " +
								((route instanceof Route) ? route.toString() : route));
					}
					
					/*
					 * If the route is note null we should add the result to the cache.
					 */
					if (route instanceof Route) {
						// TODO: Consider If it is best to do not cache empty results also.
						
						if (Constants.debugging) {
							System.out.println("Master(S)> cache(route)");
						}
						
						synchronized (this.cached_results) {
							this.cached_results.put(query, route);
						}
						
					}
					
				}

			}
			
			/*
			 * Finally we sentd the result to the user.
			 */
			
			if (Constants.debugging) {
				System.out.println("System(S)> return()");
			}
			
			this.out.writeObject(route);
			this.out.flush();
			
		} catch (IOException | ClassNotFoundException | InterruptedException ex) {
			ex.printStackTrace();			
		}
		
	}
	
	@Override
	public void close() {
		
		/*
		 * Whatever happens we should release the id if we requested one.
		 */
		if (this.id >= 0) {
			
			if (Constants.debugging) {
				System.out.println("System(S)> release_id(" + this.id + ")");
			}
			
			synchronized (this.connections_id_pool) {
				/* 
				 * TODO: In a later version we should register the thread to the
				 * pool and have another thread release the id when needed.
				 */
				this.connections_id_pool.release(this.id);
			}
			
		}
		
		super.close();
	}

}
