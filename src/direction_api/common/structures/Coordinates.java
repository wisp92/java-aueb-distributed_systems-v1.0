package direction_api.common.structures;
import java.io.Serializable;

import direction_api.common.Hash;

/**
 * 
 * @author p3100161, [Jenny's ID]
 *
 */

public class Coordinates implements Serializable {

	/**
	 * Defined by the Serializable interface.
	 */
	private static final long serialVersionUID = 2403507581180987332L;
	
	private final double latitude;
	private final double longitude;
	
	/**
	 * A Coordinates object can be initialized by providing a latitude an
	 * longitude numbers.
	 * @param latitude
	 * @param longitude
	 */
	public Coordinates(double latitude, double longitude) {
		
		this.latitude  = latitude;
		this.longitude = longitude;
		
	}
	
	/*
	 * We provide some basic and some more practical getters used
	 * mainly for searching by the mapper.
	 */
	
	public double getLatitude(int digits) {
		return getFlooredDouble(this.getLatitude(), digits);	
	}
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public double getLongitude(int digits) {
		return getFlooredDouble(this.getLongitude(), digits);	
	}
	
	public double getLongitude() {
		return this.longitude;
	}

	/*
	 * Return the position in LatLong format.
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getLatitude() + "," + this.getLongitude();
	}
	
	@Override
	public boolean equals(Object object) {
		
		boolean is_equal = false;
		
		if (this == object) {
			is_equal =  true;
		}
		else if (object instanceof Coordinates) {
				
			Coordinates coordinates = (Coordinates)object;
			is_equal = ((coordinates.getLatitude() == this.getLatitude()) &&
					(coordinates.getLongitude() == this.getLongitude()));
			
		}

		return is_equal;
	
	}
	
	@Override
	public int hashCode() {
		return this.getSHA1().hashCode();
	}
	
	public String getSHA1() {
		return Hash.getHash(this.toString(), "SHA-1");
	}
	
	/*
	 * Returns a a double having truncate some of the finals decimals digits.
	 */
	private static double getFlooredDouble(double value, int no_decimals) {
		
		double multiplier = Math.pow(10, no_decimals);
		return (double)((int)(value * multiplier)) / multiplier;
		
	}
	
}
