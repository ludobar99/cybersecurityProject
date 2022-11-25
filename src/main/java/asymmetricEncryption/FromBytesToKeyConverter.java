package asymmetricEncryption;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/*
 * 
 * Both public and private keys are saved as a byte array. This class offers methods to
 * convert a byte array to a Key object
 * 
 */
public class FromBytesToKeyConverter {
	
	/*
	 * Converts a byte array to a PrivateKey object
	 */
	public static PrivateKey getPrivateKeyfromBytes(byte[] privateKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
			
			KeyFactory keyFactory = null;
			
			keyFactory = KeyFactory.getInstance("RSA");
			   
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
	       
			PrivateKey privateKey = null;
	
			privateKey = keyFactory.generatePrivate(keySpec);
			
	        return privateKey;
			
	}
	
	/*
	 * Converts a byte array to a PublicKey object
	 */
	public static PublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
			
			KeyFactory keyFactory = null;
			
			keyFactory = KeyFactory.getInstance("RSA");
			
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
	      
			PublicKey publicKey = null;
		
			publicKey = keyFactory.generatePublic(keySpec);
	
	        return publicKey;
			
	}

}
