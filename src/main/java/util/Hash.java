package util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hash {
	
	/*
	 * Takes password as input and generates hash. The salt is saved at a precise position in
	 * the hash.
	 */
	public static String generateHash(String string) throws NoSuchAlgorithmException, InvalidKeySpecException {
		  
		int iterations = 1000;
	    char[] chars = string.toCharArray();
	    byte[] salt = getSalt();

	    PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
	   
	    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

	    byte[] hash = skf.generateSecret(spec).getEncoded();
	     
	    return iterations + ":" + toHex(salt) + ":" + toHex(hash);

	}

	/*
	 * Generates a random salt.
	 */
	private static byte[] getSalt() throws NoSuchAlgorithmException {
		
	    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
	    
	    byte[] salt = new byte[16];
	    
	    sr.nextBytes(salt);
	   
	    return salt;
	
	}

	
	
	/*
	 * Checks correctness of the password. It generates an hash from the input password and 
	 * compares it with the password in the database.
	 */
	public static boolean validatePassword(String password, String hashedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
		   
			String[] parts = hashedPassword.split(":");
		    int iterations = Integer.parseInt(parts[0]);

		    // taking the salt from the specified position 
		    byte[] salt = fromHex(parts[1]);
		    byte[] hash = fromHex(parts[2]);

		    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
		  
		    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		    
		    byte[] testHash = skf.generateSecret(spec).getEncoded();
		    
		    // ^ is XOR
		    int diff = hash.length ^ testHash.length;
		    
		    for(int i = 0; i < hash.length && i < testHash.length; i++) {
		    	
		    	// |= is Bitwise or Assignment
		        diff |= hash[i] ^ testHash[i];
		    }
		    
		    return diff == 0;

	}
	
	/*
	 * Converts a byte array to hex. The hash is stored in hex in the database.
	 */
	public static String toHex(byte[] array) throws NoSuchAlgorithmException {
		
	    BigInteger bi = new BigInteger(1, array);
	    String hex = bi.toString(16);
	    
	    int paddingLength = (array.length * 2) - hex.length();
	   
	    if (paddingLength > 0) {
	     
	    	return String.format("%0"  +paddingLength + "d", 0) + hex;
	   
	    } else { 
	    	
	    	return hex; 
	    }
	    
	}
	
	/*
	 * Converts hex to a byte array. 
	 */
	public static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		    
		byte[] bytes = new byte[hex.length() / 2];
		   
		for (int i = 0; i < bytes.length ;i++)	{
		       
			bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		   
		}
		   
		return bytes;
	}
}
