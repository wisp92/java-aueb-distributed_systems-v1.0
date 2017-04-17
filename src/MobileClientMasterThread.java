import java.io.IOException;
import java.net.UnknownHostException;

public class MobileClientMasterThread extends ClientThread {
	// Communicates with a MapperThread in order to send the request.
	
	public MobileClientMasterThread(SocketStructure socket) throws IOException, UnknownHostException {
		super(socket);
	}
	
	@Override
	protected void task() throws IOException {
		
		Query query = new Query(
			new Position(37.983810, 23.727539),
			new Position(38.012097, 23.772572)
		);
		
		this.out.writeObject(query);
		this.out.flush();
			
		// TODO: Should read routes and present them.
		System.out.println("Master replied: " + this.in.readBoolean());
		
		this.isCompleted();
		
	}
	
	public static void main(String args[]) {
		
		try {
			
			MobileClientMasterThread t = new MobileClientMasterThread(
				new SocketStructure("127.0.0.1", 4323)
			);
			t.start();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
}