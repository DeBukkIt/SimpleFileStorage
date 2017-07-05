package com.blogspot.debukkitsblog.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides methods to encrypt and decrypt Objects
 * @author DeBukkIt
 *
 */
public class Crypter {
	
	private static final String ALGO = "AES";

	/**
	 * Encrypts an Object o and returns it's encrypted equivalent CryptedObject
	 * @param o The object to encrypt
	 * @param password The password to use for encryption
	 * @return The CryptedObject equivalent of the given object
	 */
	public static CryptedObject encrypt(Object o, String password) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(o);
			return new CryptedObject(encrypt(bos.toByteArray(), password));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decrypts a CryptedObject using a password and returns it's decrypted equivalent Object
	 * @param co The CryptedObject to decrypt
	 * @param password The password to use for decryption
	 * @return The decrypted object
	 * @throws DecryptionFailedException If decryption fails. A wrong password will cause this.
	 */
	public static Object decrypt(CryptedObject co, String password) throws DecryptionFailedException {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(decrypt(co.getBytes(), password));
			ObjectInput in = new ObjectInputStream(bis);
			return in.readObject();
		} catch (Exception e) {
			throw new DecryptionFailedException();
		}
	}

	/**
	 * Encrypts an array of bytes using the AES algorithm
	 * @param data The array of bytes to encrypt
	 * @param rawKey The password to use for encryption.
	 * Warning: This password will be extended or shortened to match a length of 16 bytes (128 bit).
	 * Therefore be careful! Different passwords, starting with the same sixteen characters, result in the same AES key.
	 * @return An array of bytes containing the encrypted data
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, String rawKey) throws Exception {
		Key key = generateKey(rawKey);
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(data);
		return encVal;
	}

	/**
	 * Decrypts an array of bytes using the AES algorithm
	 * @param encryptedData The array of bytes to decrtypt
	 * @param rawKey The password to use for decryption.
	 * Warning: This password will be extended or shortened to match a length of 16 bytes (128 bit).
	 * Therefore be careful! Different passwords, starting with the same sixteen characters, result in the same AES key.
	 * @return An array of bytes containing the decrypted data
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptedData, String rawKey) throws Exception {
		Key key = generateKey(rawKey);
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decValue = c.doFinal(encryptedData);
		return decValue;
	}

	/**
	 * Generates a key to use with the AES algorithm using a String <i>key</i>.<br>
	 * Warning: This <i>key</i> will be extended or shortened to match a length of 16 bytes (128 bit).
	 * Therefore be careful! Different passwords, starting with the same sixteen characters, result in the same AES key.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private static Key generateKey(String key) throws Exception {
		String resultKey = key;
		while (key.length() < 16) {
			resultKey += resultKey;
		}
		while (resultKey.length() > 16) {
			resultKey = resultKey.substring(0, resultKey.length() - 1);
		}

		byte[] keyValue = resultKey.getBytes(Charset.forName("UTF-8"));
		return new SecretKeySpec(keyValue, ALGO);
	}
	
	/**
	 * The exception thrown when a decryption fails.<br>
	 * This might happen if the password is wrong.
	 */
	public static class DecryptionFailedException extends Exception {		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4687669054682421679L;

		@Override
		public String getMessage() {
			return "Decryption failed. Is the password correct?";
		}
	}

}
