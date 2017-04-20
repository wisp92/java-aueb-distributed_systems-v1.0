package direction_api.mapper;

import java.io.IOException;
import java.net.UnknownHostException;

import direction_api.common.Client;
import direction_api.common.Constants;
import direction_api.common.Constants.MsgType;
import direction_api.common.structures.QueryResults;
import direction_api.common.structures.SocketInformation;

/**
 * @author p3100161, p3130029
 *
 * Creates a Client object responsible for sending to the reducer all
 * routes the the mapper was able to find for the specified query.
 */

public class MapperToReducerClient extends Client {
	
	/*
	 * Because the reducer is able to receive messages both from a mapper and
	 * a master it's necessary to determine first the type of the connection.
	 * This can be done through the use of an enumeration type that is sent
	 * at the beginning of the communication.
	 */
	public static final MsgType default_type_of_connection = MsgType.MSG_PUT_ROUTE;
	
	protected final QueryResults results;
	protected final int id;
	
	public MapperToReducerClient(
			SocketInformation socket,
			int id,
			QueryResults results)
			throws IOException, UnknownHostException {
		super(socket);
		
		this.id     = id;
		this.results = results;
		
	}
	
	@Override
	protected void task() throws IOException {
		
		if (Constants.debugging) {
			System.out.println("Mapper(RC)> type() = " + default_type_of_connection.name());
			System.out.println("Mapper(RC)> connection_id: " + this.id);
			System.out.println("Mapper(RC)> original_query: " + this.results.getQuery().toString());
			System.out.println("Mapper(RC)> no_results: " + this.results.getResults().size());
			System.out.println("Mapper(RC)> send()");
		}
		
		/*
		 * We first sent the enumeration type of the connection
		 * and the identification number of the initial connection.
		 */
		this.out.writeObject(default_type_of_connection);
		this.out.writeInt(this.id);
		/*
		 * We then sent the original query used to for reduction
		 * and the actual results.
		 */
		this.out.writeObject(this.results);
		this.out.flush();
		
		/*
		 * We wait for the reducer's response and notify the server.
		 * If something goes wrong then the IOException is handled by
		 * the server so the actual type of the response does not
		 * concern us at this point.
		 */
		this.in.readBoolean();
		
		if (Constants.debugging) {
			System.out.println("Mapper(RC)> return()");
			
		}
	}

}
