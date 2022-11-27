package client;

import asymmetricEncryption.KeysGenerator;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

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

	public PublicKey createKeys(String filePath) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		KeysGenerator keyGen = new KeysGenerator(1024);
		keyGen.createKeys();
		keyGen.writePrivateKeyToFile(filePath, keyGen.getPrivateKey().getEncoded());

		return keyGen.getPublicKey();
	}

}
