package direction_api.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author p3100161, p3130029
 *
 * Creates a Client object responsible for communicating with
 * Server object running on a local or remote host.
 */

public abstract class Client extends Connection {
	
	protected final ObjectInputStream in;
	protected final ObjectOutputStream out;
	
	/**
	 * A ClientThread object can be initialized by providing the Server
	 * object's Listening socket specifications.
	 * An exception may be raised if the provided host is unknown or if the
	 * streams could not be initialized properly.
	 * @param socket
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public Client(direction_api.common.structures.SocketInformation socket)
			throws IOException, UnknownHostException {
		super(new Socket(socket.getHost(), socket.getPort()));
		
		/*
		 * As a convention ObjectOutputsStream should be initialized before
		 * the ObjectInputStream of a Client object in order to be
		 * possible the communication with the corresponding Server object.
		 */
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in  = new ObjectInputStream(this.socket.getInputStream());
		
	}
	
	/*
	 * We should make sure that both streams and client's
	 * socket get closed after execution.
	 * (non-Javadoc)
	 * @see direction_api.common.Connection#close()
	 */
	@Override
	public void close() {
		
		try {
			
			if (this.in instanceof ObjectInputStream) {
				this.in.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
		try {
			
			if (this.out instanceof ObjectOutputStream) {
				this.out.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
		super.close();
		
	}	
	
}
