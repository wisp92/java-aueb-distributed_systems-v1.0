package direction_api.common;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.rmi.UnexpectedException;

/**
 * @author p3100161, p3130029
 *
 * Creates an abstract Connection object the represents
 * on end of a line connection.
 */

abstract class Connection extends Thread implements Closeable {
	
	protected final Socket socket;
	private boolean is_completed = false;
	
	/**
	 * Implements the actual communication between the sockets.
	 * Because all communication happens between the streams an
	 * IOException is always possible.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract void task() throws IOException, InterruptedException;
	
	public Connection(Socket socket) throws IOException {
		
		this.socket = socket;
		
	}
	
	/*
	 * Should be called when the thread finished without an
	 * interruption.
	 */
	protected final void setCompleted() {
		this.is_completed = true;
	}
	
	public boolean isCompleted() {
		return this.is_completed;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		
		
		/*
		 * We should make sure to call the close() method
		 * after the interruption.
		 */
		this.close();
		super.interrupt();
		
	}
	
	/*
	 * We should make sure that the connection's socket gets closed
	 * after execution.
	 */
	public void close() {
		
		try {
			
			if (!this.socket.isClosed()) {
				
				if (Constants.debugging) {
					System.out.println("Destructor> manual_close(" + this.getClass().getSimpleName() + ")");
				}
				
				this.socket.close();
				
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}	
	
	public void run() {
		
		try {
			
			this.task();
			
			if (Constants.debugging) {
				System.out.println("Destructor> completed(" + this.getClass().getSimpleName() + ")");
			}
			
		} catch (IOException | InterruptedException ex) {
			
			if (Constants.debugging) {
				System.out.println("Destructor> failed(" + this.getClass().getSimpleName() + ")");
			}
			
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
		
		if (object_class.isInstance(object)) {
			return object_class.cast(object);
		}
		else {
			throw new UnexpectedException("Unexpected type of object");
		}
		
	}
	
}
