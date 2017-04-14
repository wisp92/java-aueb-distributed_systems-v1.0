public class IDRange {

	private boolean[] id_range;
	
	public IDRange(int number_of_allowed_connections) {
		
		this.id_range = new boolean[number_of_allowed_connections];
		
		for (int i = 0; i < this.id_range.length; i ++) {
			this.id_range[i] = true;
		}
		
	}
	
	public int getAvailableClientID() {
		
		for (int i = 0; i < this.id_range.length; i ++) {
			if (this.id_range[i]) {
				this.id_range[i] = false;
				return i;
			}
		}
		
		return -1;
		
	}
	
	public boolean releaseID(int i) {
		
		if (i >= 0 && i < this.id_range.length) {
			this.id_range[i] = true;
			
			return true;
		}
		
		return false;
		
	}
	
}
