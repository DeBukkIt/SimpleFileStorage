package com.blogspot.debukkitsblog.crypt;

import java.io.Serializable;

public class CryptedObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8303052061596400734L;
	
	private byte[] bytes;
	
	public CryptedObject(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public byte[] getBytes(){
		return bytes;
	}

}
