package direction_api.common.structures;

import java.io.Serializable;

import direction_api.common.Hash;
import direction_api.common.Constants;
import direction_api.common.Constants.DistanceAlgorithm;

/**
 * 
 * @author p3100161, p3130029
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
	 * mainly for searching the mapper's database.
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
	 * This method calculated the distance between two coordinates
	 * using one of the available distance algorithms.
	 */
	public double getDistanceFrom(Coordinates coordinates, DistanceAlgorithm distance_algorithm) {
		
		double abs_latitude_distance  =
				Math.abs(this.getLatitude() - coordinates.getLatitude());
		double abs_longitude_distance =
				Math.abs(this.getLongitude() - coordinates.getLongitude());
		
		switch(distance_algorithm) {
		case MANHATTAN_DISTNACE:
			return abs_latitude_distance + abs_longitude_distance;
		case CHEBYSHEV_DISTANCE:
			return Math.max(abs_latitude_distance, abs_longitude_distance);
		case EUCLIDEAN_DISTANCE:
			return Math.sqrt(
					Math.pow(abs_latitude_distance, 2) +
					Math.pow(abs_longitude_distance, 2));
		case SQUARED_EUCLIDIAN_DITANCE:
		default:
			return Math.abs(
					Math.pow(abs_latitude_distance, 2) +
					Math.pow(abs_longitude_distance, 2));
		}
		
	}
	
	public double getDistanceFrom(Coordinates coordinates) {
		return this.getDistanceFrom(coordinates, Constants.default_distance_algorithm);
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/*
		 * Since the getSHA1() is based on the latitude
		 * and longitude values the hashCode() complies
		 * with the corresponding equals() method.
		 */
		return this.getSHA1().hashCode();
	}
	
	/*
	 * Computes the SHA-1 hash digest of the object's
	 * coordinates.
	 */
	public String getSHA1() {
		return Hash.getHash(String.valueOf(this.getLatitude()) +
				String.valueOf(this.getLongitude()), "SHA-1");
	}
	
	/*
	 * Returns a a double having truncate some of the finals
	 * decimals digits.
	 */
	private static double getFlooredDouble(double value, int no_decimals) {
		
		double multiplier = Math.pow(10, no_decimals);
		return (double)((int)(value * multiplier)) / multiplier;
		
	}
	
}
