package direction_api.master;
public class IDRange {

	private final boolean[] used;
	
	public IDRange(int no_connections) {
		
		this.used = new boolean[no_connections];
		
		for (int i = 0; i < this.used.length; i ++) {
			this.used[i] = true;
		}
		
	}
	
	public int getAvailableID() {
		
		for (int i = 0; i < this.used.length; i ++) {
			
			if (this.used[i]) {
				
				this.used[i] = false;
				return i;
				
			}
			
		}
		
		return -1;
		
	}
	
	public boolean release(int i) {
		
		if (i >= 0 && i < this.used.length) {
			
			this.used[i] = true;
			return true;
			
		}
		
		return false;
		
	}
	
}
