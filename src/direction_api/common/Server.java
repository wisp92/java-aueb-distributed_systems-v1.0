package direction_api.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author p3100161, p3130029
 *
 * Creates a Server object responsible for serving a
 * ClientThread object trying to communicate with the local host.
 */

public abstract class Server extends Connection {
	
	protected final ObjectInputStream in;
	protected final ObjectOutputStream out;
	
	/**
	 * A Server object can be initialized by providing a temporary
	 * local socket of the connection. 
	 * The provided socket since temporary is going to be closed at the
	 * end of the communication.
	 * An exception may be raised if the streams could not be initialized
	 * properly.
	 * @param socket
	 * @throws IOException
	 */
	public Server(Socket socket) throws IOException {
		super(socket);
		
		/*
		 * As a convention ObjectInputStream should be initialized before
		 * the ObjectOutputStream of a Server object in order to be
		 * possible the communication with the corresponding ClientThread object.
		 */
		this.in  = new ObjectInputStream(this.socket.getInputStream());
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		
	}
	
	/*
	 * We should make sure that both streams and the temporary
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