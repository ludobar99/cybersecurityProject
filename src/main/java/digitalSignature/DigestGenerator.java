package digitalSignature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * This class generates a digest from a given string and returns a byte array.
 */
public class DigestGenerator {
	
	public static byte[] generateDigest(String string) throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		// passing data to the created MessageDigest Object
		md.update(string.getBytes());
		
		// compute the message digest
		byte[] digest = md.digest();
		
		return digest;
		
	}

}
