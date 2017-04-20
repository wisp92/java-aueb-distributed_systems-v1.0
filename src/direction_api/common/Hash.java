package direction_api.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author p3100161, p3130029
 */

public class Hash {

	/**
	 * Returns a hash digest of the string, based on the provided
	 * algorithm, in string format. 
	 * @param message
	 * @param algorithm
	 * @return
	 */
	public static String getHash(String string, String algorithm) {
	// TODO: Better to return a hexadecimal number
		
		StringBuffer buffer = new StringBuffer();
		
	    try {
	    	
	    	/*
	    	 * First we compute the hash in bytes format and then convert it
	    	 * to its hexadecimal equivalent and return it a string.
	    	 */
	    	
	        MessageDigest digest = MessageDigest.getInstance(algorithm);
	        byte[] hashed_bytes  = digest.digest(string.getBytes("UTF-8"));
	        
	        for (int i = 0; i < hashed_bytes.length; i ++) {
	        	buffer.append(Integer.toString(
	        			(hashed_bytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	    } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
	    	
	    	/*
	    	 * An NoSuchAlgorithmException exception may occur if the provided
	    	 * algorithm is invalid.
	    	 * An UnsupportedEncodingException exception should never occur
	    	 * since getBytes() uses UTF-8 format.
	    	 */
	    	
	    	ex.printStackTrace(); // TODO: Should be checked in the future.
	    }
	    
	    return buffer.toString();
	    
	}	
	
}
