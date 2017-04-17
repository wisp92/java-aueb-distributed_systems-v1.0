import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RoutesDB {

	private String db_name;
	
	public RoutesDB(String db_name) {
		
		this.db_name = db_name;
		
		this.initialize();
		
	}	
	
	public void initialize() {
		
		try {
			
			Class.forName("org.sqlite.JDBC");
			
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}	
		
		try {
			
			Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.db_name
			);
			connection.setAutoCommit(false);
			
			Statement statement = connection.createStatement();
			ResultSet result_set = statement.executeQuery(
				"SELECT name FROM sqlite_master " +
					"WHERE type = 'table' AND name = 'routes'"
			);
			
			if (!result_set.next()) {
					
				statement.executeUpdate("CREATE TABLE routes (" +
						"lat_src INTEGER, " +
						"lon_src INTEGER, " +
						"lat_dst INTEGER, " +
						"lon_dst INTEGER, " +
						"route   VARCHAR, " +
						"PRIMARY KEY(" +
							"lat_src, " +
							"lon_src, " +
							"lat_dst, " +
							"lon_dst " +
						")" +
					")"
				);
				
			}
			
			statement.close();
			
			connection.commit();
			connection.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public Routes searchRoute(Query route_query) {
		
		Routes routes        = new Routes();
		Position source      = route_query.getSource();
		Position destination = route_query.getDestination();
		
		double lat_src = source.getLat(2);
		double lon_src = source.getLon(2);
		double lat_dst = destination.getLat(2);
		double lon_dst = destination.getLon(2);
		
		try {
			
			Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.db_name
			);
			connection.setAutoCommit(false);
			
			Statement statement = connection.createStatement();
			ResultSet result_set = statement.executeQuery(
				"SELECT route FROM routes " +
					"WHERE lat_src >= " + lat_src + " AND lat_src < " + (lat_src + 1) + " AND "+
					"      lon_src >= " + lon_src + " AND lon_src < " + (lon_src + 1) + " AND "+
					"      lat_dst >= " + lat_dst + " AND lat_dst < " + (lat_dst + 1) + " AND "+
					"      lon_dst >= " + lon_dst + " AND lon_dst < " + (lon_dst + 1)
			);
			
			while (result_set.next()) {	
				routes.add(result_set.getString("route"));
			}
			
			statement.close();
			
			connection.commit();
			connection.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return routes;
		
	}
	
	public boolean insertRoute(Query route_query, String route) {
		
		Position source      = route_query.getSource();
		Position destination = route_query.getDestination();
		boolean inserted     = false;
		try {
			
			Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.db_name
			);
			connection.setAutoCommit(false);
			
			Statement statement = connection.createStatement();
			int no_updates = statement.executeUpdate(
				"INSERT INTO routes(lat_src, lon_src, lat_dst, lon_dst, route) " +
					"VALUES ( " +
						source.getLat() + ", " +
						source.getLon() + ", " +
						destination.getLat() + ", " +
						destination.getLon() + ", " +
						"'" + route + "'" +
					")"
			);
			
			if (no_updates > 0) {
				inserted = true;
			}
			
			statement.close();
			
			connection.commit();
			connection.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return inserted;
		
	}
	
}
