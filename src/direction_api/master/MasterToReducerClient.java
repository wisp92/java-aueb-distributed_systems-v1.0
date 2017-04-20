package direction_api.master;

import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.Constants;
import direction_api.common.Constants.MsgType;
import direction_api.common.structures.Route;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 * 
 * Creates a Client object responsible for retrieving the results
 * of the reducer.
 */
public class MasterToReducerClient extends Client {
	
	/*
	 * Because the reducer is able to receive messages both from a mapper and
	 * a master it's necessary to determine first the type of the connection.
	 * This can be done through the use of an enumeration type that is sent
	 * at the beginning of the communication.
	 */
	public static final MsgType default_type_of_connection = MsgType.MSG_GET_ROUTE;
	
	/*
	 * The results of the communication should be a single Route object.
	 */
	protected Route route;
	protected final int id;
	
	public MasterToReducerClient(SocketInformation socket, int id)
			throws IOException, UnknownHostException {
		super(socket);
		
		this.id    = id;
		this.route = null;
		
	}

	public Route getRoute() {
		return this.route;
	}
	
	/*
	 * (non-Javadoc)
	 * @see direction_api.common.Client#task()
	 */
	@Override
	protected void task() throws IOException {
		
		if (Constants.debugging) {
			System.out.println("Master(RC)> type() = " + default_type_of_connection.name());
			System.out.println("Master(RC)> connection_id() = " + this.id);
			System.out.println("Master(RC)> send()");
		}
		
		/*
		 * We first sent the enumeration type of the connection
		 * and the identification number of the initial connection.
		 */
		this.out.writeObject(default_type_of_connection);
		this.out.writeInt(this.id);
		this.out.flush();
		
		try {
			
			/*
			 * If something goes wrong the server is responsible to
			 * catch the exception.
			 */
			this.route = this.readObject(this.in.readObject(), Route.class);
		
			if (Constants.debugging) {
				System.out.println("Master(RC)> original_query: " + this.route.getQuery().toString());
				System.out.println("Master(RC)> route: " + this.route.toString());
				System.out.println("Master(RC)> return()");
			}
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.	
		}
		
	}

}
