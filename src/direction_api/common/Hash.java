package direction_api.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

	public static String getHash(String message, String algorithm) {
		 
		StringBuffer buffer = new StringBuffer();
		
	    try {
	    	
	        MessageDigest digest = MessageDigest.getInstance(algorithm);
	        byte[] hashed_bytes  = digest.digest(message.getBytes("UTF-8"));
	        
	        for (int i = 0; i < hashed_bytes.length; i ++) {
	        	buffer.append(Integer.toString(
	        			(hashed_bytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	    } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
	    	ex.printStackTrace();
	    }
	    
	    return buffer.toString();
	    
	}	
	
}
