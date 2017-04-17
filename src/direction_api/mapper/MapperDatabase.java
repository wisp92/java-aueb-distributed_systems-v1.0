package direction_api.mapper;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import direction_api.common.structures.Coordinates;
import direction_api.common.structures.Query;
import direction_api.common.structures.RoutesList;

public class MapperDatabase implements Closeable {

	public static final int default_no_digits_truncate = 2;
	
	private final String database_name;
	protected Connection connection;
	
	public MapperDatabase(String database_name) {
		
		this.database_name = database_name;
		
		try {
			
			Class.forName("org.sqlite.JDBC");
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		this.initialize();
		
	}	
	
	public void initialize() {
		
		try {
			
			this.connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.database_name
			);
			this.connection.setAutoCommit(false);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		try (Statement statement = this.connection.createStatement()) {;
		
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
			ex.printStackTrace();
		}
		
	}
	
	public void close() {
		
		try {
			
			if (!this.connection.isClosed()) {
				this.connection.close();
			}
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}
	
	public RoutesList searchRoute(Query route_query) {
		
		RoutesList routes       = new RoutesList();
		Coordinates source      = route_query.getSource();
		Coordinates destination = route_query.getDestination();
		
		double lat_src = source.getLatitude(default_no_digits_truncate);
		double lon_src = source.getLongitude(default_no_digits_truncate);
		double lat_dst = destination.getLatitude(default_no_digits_truncate);
		double lon_dst = destination.getLongitude(default_no_digits_truncate);
		
		try (Statement statement = connection.createStatement()) {
			
			ResultSet result_set = statement.executeQuery(
				"SELECT `route` FROM `routes` " +
					"WHERE `lat_src` >= " + lat_src + " AND `lat_src` < " + (lat_src + 1) + " AND "+
					      "`lon_src` >= " + lon_src + " AND `lon_src` < " + (lon_src + 1) + " AND "+
					      "`lat_dst` >= " + lat_dst + " AND `lat_dst` < " + (lat_dst + 1) + " AND "+
					      "`lon_dst` >= " + lon_dst + " AND `lon_dst` < " + (lon_dst + 1)
			);
			
			while (result_set.next()) {	
				routes.add(result_set.getString("route"));
			}
			
			connection.commit();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return routes;
		
	}
	
	public boolean insertRoute(Query route_query, String route) {
		
		Coordinates source      = route_query.getSource();
		Coordinates destination = route_query.getDestination();
		boolean inserted        = false;
		
		try (Statement statement = connection.createStatement()) {
			
			int no_updates = statement.executeUpdate(
				"INSERT INTO `routes`(`lat_src`, `lon_src`, `lat_dst`, `lon_dst`, `route`) " +
					"VALUES ( " +
						source.getLatitude() + ", " +
						source.getLongitude() + ", " +
						destination.getLatitude() + ", " +
						destination.getLongitude() + ", " +
						"'" + route + "'" +
					")"
			);
			
			if (no_updates > 0) {
				inserted = true;
			}
			
			connection.commit();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return inserted;
		
	}
	
}
