package direction_api.common;

/**
 * @author p3100161, p3130029
 *
 * Created an IDPool object responsible for providing unique ID's
 * from a range.
 * It ensures that no to identical ID's are used at he same time.
 */

public class IDPool {

	/*
	 * Holds information about which ID are available to
	 * be used
	 */
	private final boolean[] used;
	
	public IDPool(int size) {
		
		this.used = new boolean[size];
		
		/*
		 * At the beginning all ID should be available to be used.
		 */
		for (int i = 0; i < this.used.length; i ++) {
			this.used[i] = false;
		}
		
	}
	
	public int getAvailableID() {
		
		for (int i = 0; i < this.used.length; i ++) {
			
			if (!this.used[i]) {
				
				/*
				 * Returns the first available ID of the range.
				 */
				this.used[i] = true;
				return i;
				
			}
			
		}
		
		return -1;
		
	}
	
	public boolean release(int i) {
		
		if (i >= 0 && i < this.used.length) {
			
			this.used[i] = false;
			return true;
			
		}
		
		return false;
		
	}
	
}
