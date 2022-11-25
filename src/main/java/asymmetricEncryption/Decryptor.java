package asymmetricEncryption;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
/*
 * 
 * This class provides a method to decrypt data.
 * 
 */
public class Decryptor {
	
	/*
	 * Decrypts data with a given key. Takes an encrypted byte array as input and returns it decrypted.
	 */
	public static byte[] decrypt(byte[] encryptedTest, Key key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			return cipher.doFinal(encryptedTest);
		
	}
	
}
