import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration extends Properties {
	// Is used in order to communicate with a configuration file.
	
	private static final long serialVersionUID = 965338227439439750L;

	public Configuration(String filename) throws IOException {
		
		super();
		
		try (InputStream in = Files.newInputStream(Paths.get(filename))) {
			
			this.load(in);	
			
		}
		catch (FileNotFoundException ex) {
			throw ex;
		}
		
	}
	
	public int getInt(String key) {
		
		return this.getInt(key, 0);
		
	}
	
	public int getInt(String key, int default_value) {
		
		if (this.containsKey(key)) {
			return Integer.parseInt(this.getProperty(key));
		}
		
		return default_value;
		
	}
	
	public String getString(String key) {
		return this.getProperty(key);
	}
	
	public String getString(String key, String default_value) {
		return this.getProperty(key, default_value);
	}
	
} 
