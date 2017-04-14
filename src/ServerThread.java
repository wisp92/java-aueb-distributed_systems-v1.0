import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ServerThread extends Thread {
	// An abstract class that implements a server's side socket via a thread.
	
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected Socket request_socket;
	
	public ServerThread(Socket request_socket) throws IOException {
		
		this.request_socket = request_socket;
		
		this.in  = new ObjectInputStream(this.request_socket.getInputStream());
		this.out = new ObjectOutputStream(this.request_socket.getOutputStream());
		
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
	
	public void run() {
		
		try {
			
			task();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			
			this.closeSocket();
			
		}
	
	}
	
	protected abstract void task() throws IOException;	
	
}