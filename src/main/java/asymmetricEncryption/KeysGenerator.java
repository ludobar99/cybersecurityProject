package asymmetricEncryption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
/*
 * 
 * This class generates the public and private key of a user. Keys are generated on the client side.
 * It provides a method to write the private key in a local file.
 * 
 */
public class KeysGenerator {
	
		private KeyPairGenerator keyGen;
		private KeyPair pair;
		private PrivateKey privateKey;
		private PublicKey publicKey;

		public KeysGenerator(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
			this.keyGen = KeyPairGenerator.getInstance("RSA");
			this.keyGen.initialize(keylength);
		}

		public void createKeys() {
			this.pair = this.keyGen.generateKeyPair();
			this.privateKey = pair.getPrivate();
			this.publicKey = pair.getPublic();
		}

		public PrivateKey getPrivateKey() {
			return this.privateKey;
		}

		public PublicKey getPublicKey() {
			return this.publicKey;
		}

		public void writePrivateKeyToFile(String path, byte[] privateKey) throws IOException {
			
			File f = new File(path);
				
			f.getParentFile().mkdirs();

			FileOutputStream fos = new FileOutputStream(f);
			
			fos.write(privateKey);
			fos.flush();
			fos.close();
		
		}
		
		/*
		 * TODO: delete once finished
		 * main method to test keys. It generates keys, encrypts and decrypts a text
		 */
//		public static void main(String[] args) throws SQLException, InvalidKeySpecException, IOException, NoSuchAlgorithmException {
//
//			KeysGenerator k = null;
//
//			try {
//
//				k = new KeysGenerator(1024);
//				k.createKeys();
//
//			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			/*
//			 * key pair
//			 */
//			PublicKey pubk = k.getPublicKey();
//			PrivateKey privk = k.getPrivateKey();
//
//			String text = "Ciao, questo è un testo segreto!";
//			byte[] encryptedTest = null;
//			byte[] decryptedTest = null;
//
//
//			/*
//			 *  using public key from database.
//			 */
//			KeyGetter.init();
//			byte[] publickey = KeyGetter.getPublicKeyBytes("alice@ex.com");
//
//			System.out.println("public key: " + pubk);
//
//			try {
//
//				PublicKey key = FromBytesToKeyConverter.getPublicKeyFromBytes(publickey);
//				encryptedTest = Encryptor.encrypt(text.getBytes(), key);
//
//			} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
//					| NoSuchAlgorithmException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			System.out.println("Encrypted text: "+ encryptedTest);
//
//			byte[] privateKey = KeyGetter.getPrivateKeyBytes("alice@ex.com");
//
//			PrivateKey pKey = FromBytesToKeyConverter.getPrivateKeyfromBytes(privateKey);
//			System.out.println("private key: " + privk);
//
//			try {
//
//				decryptedTest = Decryptor.decrypt(encryptedTest, pKey);
//
//			} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException
//					| NoSuchAlgorithmException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			String decrypted = new String(decryptedTest);
//			System.out.println(decrypted);
//
//
//		}
		
}
	
	


