package direction_api.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.UnexpectedException;

/**
 * @author p3100161, p3130029
 *
 * Creates a Client object responsible for communicating with
 * Server object running on a local or remote host.
 */

public abstract class Client extends Thread implements Closeable {
	
	protected final ObjectInputStream in;
	protected final ObjectOutputStream out;
	protected final Socket socket;
	private boolean is_completed = false;
	
	/**
	 * Implements the actual communication between the sockets.
	 * Because all communication happens between the streams an
	 * IOException is always possible.
	 * @throws IOException
	 */
	protected abstract void task() throws IOException; // TODO: Should be added in a superclass.
	
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
		
		this.socket = new Socket(socket.getHost(), socket.getPort());
		
		/*
		 * As a convention ObjectOutputsStream should be initialized before
		 * the ObjectInputStream of a Client object in order to be
		 * possible the communication with the corresponding Server object.
		 */
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in  = new ObjectInputStream(this.socket.getInputStream());
		
	}
	
	/*
	 * Should be called when the thread finished without an
	 * interruption.
	 */
	protected final void setCompleted() { // TODO: Should be added in a superclass.
		this.is_completed = true;
	}
	
	public boolean isCompleted() { // TODO: Should be added in a superclass.
		return this.is_completed;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() { // TODO: Should be added in a superclass.
		
		super.interrupt();
		/*
		 * We should make sure to call the close() method
		 * after the interruption.
		 */
		this.close();
		
	}
	
	/*
	 * We should make sure that both streams and client's
	 * socket get closed after execution.
	 */
	public void close() { // TODO: Should override a superclass' method.
		
		try {
			
			if (this.in instanceof ObjectInputStream) {
				this.in.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
		try {
			
			if (this.out instanceof ObjectOutputStream) {
				this.out.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
		try {
			
			if (!this.socket.isClosed()) {
				this.socket.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
	}	
	
	public void run() { // TODO: Should be added in a superclass.
		
		try {
			
			this.task();
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		} finally {
			
			this.setCompleted();
			
			this.close();
			
		}
	
	}
	
	/**
	 * Implements a safe cast for the remote objects and throw an
	 * exception if the cast is not possible.
	 * @param object
	 * @param object_class
	 * @return
	 * @throws UnexpectedException
	 */
	protected <E> E readObject(Object object, Class<E> object_class) throws UnexpectedException {
		// TODO: Add to a superclass.
		if (object_class.isInstance(object)) {
			return object_class.cast(object);
		}
		else {
			throw new UnexpectedException("Unexpected type of object");
		}
	}
	
}
