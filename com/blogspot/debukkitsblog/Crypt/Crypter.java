package com.blogspot.debukkitsblog.Crypt;

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

public class Crypter {

	private static final String ALGO = "AES";

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

	public static Object decrypt(CryptedObject co, String password) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(decrypt(co.getBytes(), password));
			ObjectInput in = new ObjectInputStream(bis);
			return in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] encrypt(byte[] Data, String rawKey) throws Exception {
		Key key = generateKey(rawKey);
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Data);
		return encVal;
	}

	public static byte[] decrypt(byte[] encryptedData, String rawKey) throws Exception {
		Key key = generateKey(rawKey);
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decValue = c.doFinal(encryptedData);
		return decValue;
	}

	private static Key generateKey(String key) throws Exception {
		while (key.length() < 16) {
			key += key;
		}
		while (key.length() > 16) {
			key = key.substring(0, key.length() - 1);
		}

		byte[] keyValue = key.getBytes(Charset.forName("UTF-8"));
		return new SecretKeySpec(keyValue, ALGO);
	}

}
