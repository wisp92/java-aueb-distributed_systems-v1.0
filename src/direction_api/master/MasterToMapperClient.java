package direction_api.master;

import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.Constants;
import direction_api.common.structures.Query;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 *
 * Creates a Cleint object responsible to communicate with the selected
 * mapper and send the user's query.
 */

public class MasterToMapperClient extends Client {
	
	protected final Query query;
	protected final SocketInformation reducer_socket;
	protected final int id;
	/*
	 * In addition to the given information we should be able to
	 * inform the server that something went wrong.
	 */
	protected boolean success;
	
	public MasterToMapperClient(
			SocketInformation socket, 
			int id, 
			Query query, 
			SocketInformation reducer_socket
			) throws IOException, UnknownHostException {
		super(socket);
		
		this.query          = query;
		this.reducer_socket = reducer_socket;
		this.id             = id;
		this.success        = false;
	}
	
	public boolean isSuccess() {
		return this.success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Client#task()
	 */
	@Override
	protected void task() throws IOException {
		
		if (Constants.debugging) {
			System.out.println("Master(MC)> connection_id: " + this.id);
			System.out.println("Master(MC)> query: " + this.query.toString());
			System.out.println("Master(MC)> after() = " + this.reducer_socket.toString());
			System.out.println("Master(MC)> send()");
		}
		
		/*
		 * First we send the the unique ID of this communication
		 * between the master, the reducer and the mapper.
		 * Used also later to retrieve the results.
		 */
		this.out.writeInt(this.id);
		/*
		 * Then we send the query of the user and information about the
		 * reducer that the mapper is going to sent the results.
		 */
		this.out.writeObject(this.query);
		this.out.writeObject(this.reducer_socket);
		this.out.flush();
		
		/*
		 * Finally we wait for the mapper's response.
		 * If all went well the isSuccess() method is going to return a
		 * positive answer to the server.
		 */
		this.success = this.in.readBoolean();
		
		if (Constants.debugging) {
			System.out.println("Master(MC)> return(" + this.success + ")");
		}
		
	}

}
