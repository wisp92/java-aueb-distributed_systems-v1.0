package direction_api.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.UnexpectedException;

/**
 * @author p3100161, p3130029
 *
 * Creates a Server object responsible for serving a
 * ClientThread object trying to communicate with the local host.
 */

/*
 * Creates a Server object responsible for serving a
 * ClientThread object trying to communicate with the local host.
 */
public abstract class Server extends Thread implements Closeable {
	
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
		
		this.socket = socket;
		
		/*
		 * As a convention ObjectInputStream should be initialized before
		 * the ObjectOutputStream of a Server object in order to be
		 * possible the communication with the corresponding ClientThread object.
		 */
		this.in  = new ObjectInputStream(this.socket.getInputStream());
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		
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
	 * We should make sure that both streams and the temporary
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
			ex.printStackTrace();
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
			throw new UnexpectedException("Unexpected type of object.");
		}
	}
	
}