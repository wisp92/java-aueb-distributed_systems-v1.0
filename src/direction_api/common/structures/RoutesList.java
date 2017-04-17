package direction_api.common.structures;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author p3100161, [Jenny's ID]
 *
 */

/*
 * Creates RoutesList object that is serializable array of routes.
 * A route is represented by s String object in JSON format as
 * provided by the Google's direction API.
 */
public class RoutesList extends ArrayList<String> implements Serializable {

	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = 1350219233099093423L;
	
}
