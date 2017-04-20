package direction_api.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author p3100161, p3130029
 * 
 * Creates a Configuration object for handling the communication
 * with a properties file.
 */

/*
 * 
 */
public class Configuration extends Properties {
	
	/**
	 * Defined by the Serializable interface of the Properties superclass.
	 */
	private static final long serialVersionUID = 965338227439439750L;

	private final String path;
	
	/**
	 * A Configuration object can be initialized by providing the
	 * corresponding path.
	 * @param path
	 */
	public Configuration(String path) {
		super();
		
		this.path = path;
		
		try (FileInputStream in = new FileInputStream(path)) {
			
			this.load(in);	
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
	}

	/**
	 * A Configuration object can be initialized by providing the
	 * corresponding resource as URL object.
	 * @param resource
	 */
	public Configuration(URL resource) {
		super();		
		
		this.path = resource.getPath();
		
		try (InputStream in = resource.openStream()) {
			
			this.load(in);	
			
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO: Should be checked in the future.
		}
		
	}
	
	/*
	 * We provide some practical getters that are going to be used from
	 * most of the configurable objects.
	 */
	
	public String getPath() {
		return this.path;
	}
	
	public int getInt(String key) {
		return this.getInt(key, 0);
	}
	
	public int getInt(String key, int default_value) {
		
		if (this.containsKey(key)) {
			return Integer.parseInt(this.getString(key));
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
