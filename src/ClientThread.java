import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class ClientThread extends Thread {
	// An abstract class that implements a client's side socket via a thread.
	
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected Socket request_socket;
	private boolean is_completed;
	
	public ClientThread(SocketStructure socket) throws IOException, UnknownHostException {
		
		this.request_socket = new Socket(socket.getHost(), socket.getPort());
		
		this.out = new ObjectOutputStream(this.request_socket.getOutputStream());
		this.in  = new ObjectInputStream(this.request_socket.getInputStream());
		
	}
	
	protected final void setCompleted() {
		this.is_completed = true;
	}
	
	public boolean isCompleted() {
		return this.is_completed;
	}
	
	public void closeSocket() {
		
		try {
			if (this.in != null) {
				this.in.close();
			}
			if (this.out != null) {
				this.out.close();
			}
			this.request_socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	protected abstract void task() throws IOException;	
	
	public void run() {
		
		try {
			
			task();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			
			this.closeSocket();
			
		}
	
	}
	
}
