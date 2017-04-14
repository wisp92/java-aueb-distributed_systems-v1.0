import java.io.IOException;
import java.net.Socket;

public class ReducerServerThread extends ServerThread {
	
	public ReducerServerThread(Socket request_socket) throws IOException{
		super(request_socket);
	}
	
	@Override
	protected void task() throws IOException {
		
		boolean completed = true;
		
		try {
			
			// TODO: First should check if the connection is from master or mapper.
			int type_of_connection = 0;
			
			int id        = this.in.readInt();
			
			switch(type_of_connection) {
				case 0:
					Routes routes = (Routes)(this.in.readObject());
					// TODO: Should store routes according to id in shared structure.
					break;
				case 1:
					// TODO: Should return the stored routes and delete the entry.
					break;
			}
			
			
		} catch (ClassNotFoundException ex) {
			
			completed = false;
			
			ex.printStackTrace();
			
		} finally { 
			
			this.out.writeBoolean(completed);
			this.out.flush();
			
		}
			
	}

}
