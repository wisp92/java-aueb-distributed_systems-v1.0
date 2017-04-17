import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = 2403507581180987332L;
	
	private final double lat;
	private final double lon;
	
	public Position(double lat, double lon) {
		
		this.lat = lat;
		this.lon = lon;
		
	}
	
	private double getTruncated(double value, int digits) {
		
		double multiplier = Math.pow(10, digits);
		return (double)((int)(value * multiplier)) / multiplier;
		
	}
	
	public double getLat(int digits) {
		return getTruncated(this.getLat(), digits);	
	}
	public double getLat() {
		return this.lat;
	}
	
	public double getLon(int digits) {
		return getTruncated(this.getLon(), digits);	
	}
	public double getLon() {
		return this.lon;
	}
	
	@Override
	public String toString() {
		return this.getLat() + "," + this.getLon();
	}
	
}
