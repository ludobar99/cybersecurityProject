package client;

import asymmetricEncryption.KeysGenerator;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

public class User {
	
	String email;
	String password;
	
	PublicKey publicKey;
	

	public User(String email, String pwd) {
		this.email = email;
		this.password = pwd;
	}

	/*
	 * Writes privateKey on a file and returns user's publicKey
	 */
	public PublicKey createKeys(String filePath) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		KeysGenerator keyGen = new KeysGenerator(1024);
		keyGen.createKeys();
		keyGen.writePrivateKeyToFile(filePath, keyGen.getPrivateKey().getEncoded());

		return keyGen.getPublicKey();
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String name) {
		this.email = name;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	

}
