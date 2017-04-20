package direction_api.mapper;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import direction_api.common.Constants;
import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.Route;
import direction_api.common.structures.QueryResults;

/**
 * @author p3100161, p3130029
 *
 * Creates a MapperDatabase object that implements a connection
 * with a local SQLite database.
 * It is responsible for adding routes to the database and execute
 * queries from the mapper server's.
 */

public class MapperDatabase implements Closeable {

	/*
	 * Used to calculate the area of search for a relevant route
	 * during the execution of a query.
	 */
	//public static final int default_no_digits_truncate = 2;
	
	private final String database_name;
	protected Connection connection;
	
	public MapperDatabase(String database_name) {
		
		this.database_name = database_name;
		
		try {
			
			/*
			 * Just checking the necessary libraries are available.
			 */
			Class.forName("org.sqlite.JDBC");
			
			this.initialize();
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		
	}	
	
	public void initialize() {
		
		/*
		 * First we create the database's file If not present.
		 */
		try {
			
			this.connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.database_name
			);
			this.connection.setAutoCommit(false);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		/*
		 * Then we create the necessary tables.
		 */
		try (Statement statement = this.connection.createStatement()) {;
		
			// TODO: Should also check if the structure is correct and drop the tables if needed.
		
			ResultSet result_set = statement.executeQuery(
				"SELECT `name` FROM `sqlite_master` " +
					"WHERE `type` = 'table' AND `name` = 'routes'"
			);
			
			if (!result_set.isBeforeFirst()) {
					
				statement.executeUpdate(
					"CREATE TABLE `routes` (" +
						"`lat_src` INTEGER, " +
						"`lon_src` INTEGER, " +
						"`lat_dst` INTEGER, " +
						"`lon_dst` INTEGER, " +
						"`route`   VARCHAR, " +
						"PRIMARY KEY(" +
							"`lat_src`, " +
							"`lon_src`, " +
							"`lat_dst`, " +
							"`lon_dst` " +
						")" +
					")"
				);
				
			}
			
			this.connection.commit();
			
		} catch (SQLException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
	}
	
	/**
	 * Searches the database for the routes with source and destination close
	 * to the query.
	 * The size of the area that is considered a valid result is determined by
	 * the search_size argument which controls the number of decimal digits
	 * that are going to be ignored.
	 * @param route_query
	 * @param search_size
	 * @return
	 */
	public QueryResults searchRoute(Query query, int search_size) {
		
		ArrayList<Route> routes = new ArrayList<Route>();
		Coordinates source      = query.getSource();
		Coordinates destination = query.getDestination();
		
		double lat_src = source.getLatitude(search_size);
		double lon_src = source.getLongitude(search_size);
		double lat_dst = destination.getLatitude(search_size);
		double lon_dst = destination.getLongitude(search_size);
		
		try (Statement statement = connection.createStatement()) {
			
			ResultSet result_set = statement.executeQuery(
				"SELECT * FROM `routes` " +
					"WHERE `lat_src` >= " + lat_src + " AND `lat_src` < " + (lat_src + 1) + " AND "+
					      "`lon_src` >= " + lon_src + " AND `lon_src` < " + (lon_src + 1) + " AND "+
					      "`lat_dst` >= " + lat_dst + " AND `lat_dst` < " + (lat_dst + 1) + " AND "+
					      "`lon_dst` >= " + lon_dst + " AND `lon_dst` < " + (lon_dst + 1)
			);
			
			while (result_set.next()) {	
				routes.add(new Route(
						new Query(
								new Coordinates(
										result_set.getDouble("lat_src"),
										result_set.getDouble("lon_src")),
								new Coordinates(
										result_set.getDouble("lat_dst"),
										result_set.getDouble("lon_dst"))),
						result_set.getString("route")));
			}
			
			connection.commit();
			
		} catch (SQLException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
		return new QueryResults(query, routes);
		
	}
	
	public QueryResults searchRoute(Query query) {
		return this.searchRoute(query, Constants.no_ignored_decimals_in_search);
	}
	
	/**
	 * The route is inserted to the database indexed by the coordinates of its
	 * source and destination.
	 * @param route_query
	 * @param route
	 * @return
	 */
	public boolean insertRoute(Route route) {
		
		Query query             = route.getQuery();
		Coordinates source      = query.getSource();
		Coordinates destination = query.getDestination();
		boolean inserted        = false;
		
		try (Statement statement = connection.createStatement()) {
			
			int no_updates = statement.executeUpdate(
				"INSERT INTO `routes`(`lat_src`, `lon_src`, `lat_dst`, `lon_dst`, `route`) " +
					"VALUES ( " +
						source.getLatitude() + ", " +
						source.getLongitude() + ", " +
						destination.getLatitude() + ", " +
						destination.getLongitude() + ", " +
						"'" + route.toString() + "'" +
					")"
			);
			
			if (no_updates > 0) {
				inserted = true;
			}
			
			connection.commit();
			
		} catch (SQLException ex) {
			ex.printStackTrace(); // TODO: Should be checked later on.
		}
		
		return inserted;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		
		try {
			
			if (!this.connection.isClosed()) {
				this.connection.close();
			}
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}
	
}
