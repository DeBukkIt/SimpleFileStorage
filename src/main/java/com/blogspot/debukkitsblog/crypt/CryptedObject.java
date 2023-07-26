package com.blogspot.debukkitsblog.crypt;

import java.io.Serializable;

/**
 * A CryptedObject stores the encrypted data generated using the
 * <code>Crypter</code> utility
 * 
 * @author DeBukkIt
 * @version 2.3.0
 */
@Deprecated
public class CryptedObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5575861158027167628L;

	private byte[] bytes;

	/**
	 * Constructs a CryptedObject by taking an array of bytes and storing it
	 * 
	 * @param bytes The array of bytes to be stored
	 */
	@Deprecated
	public CryptedObject(byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * Returns the bytes stored in the CryptedObject
	 * 
	 * @return The bytes stored in the CryptedObject
	 */
	@Deprecated
	public byte[] getBytes() {
		return bytes;
	}

}
