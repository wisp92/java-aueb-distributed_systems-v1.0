package direction_api.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author p3100161, p3130029
 *
 * @param <K>
 * @param <V>
 * 
 * Creates an LRU cache based on a LinkedHashMap object.
 */

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	/**
	 * Defined by the Serializable interface of the HashMap object.
	 */
	private static final long serialVersionUID = 8037241862445895432L;
	
	public final int max_capacity;
	
	public LRUCache(int max_capacity) {
		super(max_capacity + 1, 1);
		
		this.max_capacity = max_capacity;
	}
	
	/*
	 * Extends the get() method.
	 * If do_update is enabled ensure that the eldest entry is always
	 * the last accessed.
	 */
	public V get(K key, boolean do_update) {
		
		if (do_update && this.containsKey(key)) {
			this.put(key, this.remove(key));
		}
		
		return this.get(key);
	}
	
	/*
	 * Allow the LinkedHashMap object to remove the eldest entry if the the
	 * max_capacity was reached.
	 * (non-Javadoc)
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return this.size() > this.max_capacity;
	}
	
}
