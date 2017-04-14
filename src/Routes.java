import java.io.Serializable;
import java.util.ArrayList;

public class Routes implements Serializable {

	private static final long serialVersionUID = 1350219233099093423L;

	protected ArrayList<String> routes;
	
	public Routes (ArrayList<String> routes) {
		this.routes = routes;
	}
	
}
