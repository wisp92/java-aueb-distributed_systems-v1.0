package direction_api.common;

/**
 * @author p3100161, p3130029
 * 
 * A static class containing global projects used
 * through out the project.
 */

public class Constants {

	/*
	 * An enumeration of the available message types that
	 * the reducer can receive.
	 */
	public static enum MsgType {
		
		MSG_PUT_ROUTE,
		MSG_GET_ROUTE;
		
	}
	
	/*
	 * An enumeration of the available distance calculation algorithms
	 * used to compare the similar routes.
	 */
	public static enum DistanceAlgorithm {
		
		EUCLIDEAN_DISTANCE,
		SQUARED_EUCLIDIAN_DITANCE,
		CHEBYSHEV_DISTANCE,
		MANHATTAN_DISTNACE;
		
	}
	
	public final static DistanceAlgorithm default_distance_algorithm =
			DistanceAlgorithm.SQUARED_EUCLIDIAN_DITANCE;
	
	/*
	 * If debugging is enabled then debug messages are printed
	 * in each server about the current state.
	 */
	public final static boolean debugging = true;
	
	/*
	 * Defines the number of digits that are going to be ignores from the
	 * coordinates to optimize the queries.
	 */
	// TODO: Can be added through the configuration file of the mapper.
	public final static int no_ignored_decimals_in_search = 2;
	
	/*
	 * Holds the maximum allowed time tha a thread is going to sleep if needed
	 * before checking again for its request is available.
	 */
	public final static int max_sleep_time = 1000;
	
}
