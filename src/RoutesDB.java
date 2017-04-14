import java.sql.*;
import java.util.ArrayList;

public class RoutesDB {

	private String db_name;
	
	public RoutesDB(String db_name) {
		
		this.db_name = db_name;
		
		this.initialize();
		
	}	
	
	public void initialize() {
		
		try {
			
			Class.forName("org.sqlite.JDBC");
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
						"source_pc       INTEGER, " +
						"destination_pc  INTEGER, " +
						"route           VARCHAR, " +
						"PRIMARY KEY(" +
							"source_pc, " +
							"destination_pc" +
						")" +
					")"
				);
				
			}
			
			statement.close();
			
			connection.commit();
			connection.close();
			
		} catch (ClassNotFoundException | SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public Routes searchRoute(Query route_query) {
		
		ArrayList<String> routes = new ArrayList<String>();
		
		try {
			
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection(
				"jdbc:sqlite:" + this.db_name
			);
			connection.setAutoCommit(false);
			
			Statement statement = connection.createStatement();
			ResultSet result_set = statement.executeQuery(
				"SELECT route FROM routes " +
					"WHERE source_pc      = '" + route_query.getSourcePostalCode() + "' AND " +
					"      destination_pc = '" + route_query.getDestinationPostalCode() + "'"
			);
			
			while (result_set.next()) {	
				routes.add(result_set.getString("route"));
			}
			
			statement.close();
			
			connection.commit();
			connection.close();
			
		} catch (ClassNotFoundException | SQLException ex) {
			ex.printStackTrace();
		}
		
		return new Routes(routes);
		
	}
	
}
