package client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import asymmetricEncryption.KeysGenerator;

public class User {
	
	String name;
	PublicKey publicKey;
	

	public User(String name) {
		this.name = name;
	}

	
	/*
	 * TODO: check if re-factoring is needed
	 * TODO: put relative path or something instead of the path of my computer
	 */
	
	public PublicKey createKeys(String user) {
		
		KeysGenerator keyGen = null ;
		
		String path = "/Users/ludo/cs_project/ExamProject/src/main/java/client/" + user;
						
		try {
		
			keyGen = new KeysGenerator(1024);
			keyGen.createKeys();
			keyGen.writePrivateKeyToFile(path, keyGen.getPrivateKey().getEncoded());

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return keyGen.getPublicKey();
		
		
	}

}
