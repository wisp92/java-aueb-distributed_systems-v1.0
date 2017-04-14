import java.io.Serializable;

public class SocketStructure implements Serializable {

	private static final long serialVersionUID = 338881722851867872L;
	
	private String host;
	private int port;
	
	public SocketStructure(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getHost() {
		return this.host;
	}
	
}
