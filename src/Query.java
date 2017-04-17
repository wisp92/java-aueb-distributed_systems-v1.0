import java.io.Serializable;

public class Query implements Serializable {

	private static final long serialVersionUID = -3481780946666495460L;
	
	private final Position source;
	private final Position destination;
	
	public Query(Position source, Position destination) {
		
		this.source      = source;
		this.destination = destination;
		
	}
	
	public Position getSource() {
		return this.source;
	}
	
	public Position getDestination() {
		return this.destination;
	}
	
}
