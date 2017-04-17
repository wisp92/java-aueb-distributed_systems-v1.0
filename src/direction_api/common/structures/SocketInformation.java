package direction_api.common.structures;
import java.io.Serializable;

/**
 * 
 * @author p3100161, [Jenny's ID]
 *
 */

/* 
 * Creates a Socket object that keeps the necessary information to
 * connect to a corresponding socket.
 * The object does not actually act as real socket.
 */
public class SocketInformation implements Serializable {

	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = 338881722851867872L;
	
	private final String host;
	private final int port;
	
	/**
	 * A Socket object can be initialized by providing a host address and
	 * a port for the connection
	 * @param host
	 * @param port
	 */
	public SocketInformation(String host, int port) {
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
