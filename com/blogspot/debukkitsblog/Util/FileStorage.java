package com.blogspot.debukkitsblog.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.blogspot.debukkitsblog.Crypt.CryptedObject;
import com.blogspot.debukkitsblog.Crypt.Crypter;

public class FileStorage {
	
	private File storageFile;
	private HashMap<String, Object> storageMap;
	
	/**
	 * Creates a FileStorage. It allows you to store<br>
	 * your serializable object in a file using a key<br>
	 * for identification and to read it somewhen later.
	 * @param file The file your data shall be stored in
	 * @throws IOException if your File cannot be created
	 * @throws IllegalArgumentException if your File is a directory
	 */
	public FileStorage(File file) throws IOException, IllegalArgumentException{
		this.storageFile = file;
		
		if(storageFile.isDirectory()){
			throw new IllegalArgumentException("storageFile must not be a directory");
		}
		
		if(storageFile.createNewFile()){
			storageMap = new HashMap<String, Object>();
			save();
		} else {
			load();
		}
	}

	/**
	 * Saves the HashMap into the File
	 */
	private void save(){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile));
			oos.writeObject(storageMap);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Loads the File into the HashMap
	 */
	@SuppressWarnings("unchecked")
	private void load(){
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile));
			storageMap = (HashMap<String, Object>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores an Object o using a String key for later identification
	 * <br>
	 * Use <u>store(String key, Object o, String password)</u> for storing using AES encryption.
	 * @param key The key as String.
	 * @param o	The Object.
	 */
	public void store(String key, Object o){
		storageMap.put(key, o);
		save();
	}
	
	/**
	 * Stores an Object o using a String key for later identification<br>
	 * and a password for AES encryption.<br>
	 * <br>
	 * Use <u>store(String key, Object o)</u> for storing without encryption.
	 * @param key The key as String.
	 * @param o	The Object.
	 * @param password The Password.
	 */
	public void store(String key, Object o, String password) {
		store(key, Crypter.encrypt(o, password));
	}
	
	/**
	 * Reads your object from the storage.<br>
	 * <br>
	 * Use <u>get(String key, String password)</u> for AES encrypted objects.
	 * @param key The key the object is available under
	 * @return your Object or null if nothing was found for <i>key</i>
	 */
	public Object get(String key){
		return storageMap.get(key);
	}
	
	/**
	 * Reads your AES encrypted object from the storage.<br>
	 * <br>
	 * Use <u>get(String key)</u> instead for unencrypted objects.
	 * @param key The key the object is available under
	 * @param password The password to use for decryption
	 * @return your object or null if nothing was found for <i>key</i> or if decryption failed (wrong password)
	 */
	public Object get(String key, String password){
		if(storageMap.get(key) instanceof CryptedObject) {
			return Crypter.decrypt((CryptedObject) get(key), password);
		} else {
			return get(key);
		}
	}
	
	/**
	 * All stored objects in an ArrayList of Objects
	 * @return all stored objects in an ArrayList of Objects
	 */
	public ArrayList<Object> getAllAsArrayList(){
		ArrayList<Object> result = new ArrayList<Object>();
		for(Object c: storageMap.values()){
			result.add(c);
		}
		return result;
	}
	
	/**
	 * All stored objects in a HashMap of Strings and Objects
	 * @return all stored objects in a HashMap of Strings and Objects
	 */
	public HashMap<String, Object> getAll(){
		return storageMap;
	}
	
	/**
	 * Prints all stored keys with corresponding objects
	 */
	public void printAll(){
		for(String cKey : storageMap.keySet()){
			System.out.println(cKey + " :: " + storageMap.get(cKey));
		}
	}
	
	/**
	 * Removes an Key-Object pair from the storage
	 * @param key
	 */
	public void remove(String key){
		storageMap.remove(key);
		save();
	}
	
	/**
	 * Checks whether a key is registerd
	 * @param key The Key.
	 * @return true if an object is available for that key
	 */
	public boolean hasKey(String key){
		return storageMap.containsKey(key);
	}
	
	/**
	 * Checks whether an object is stored at all
	 * @param o The Object.
	 * @return true if the object is stored
	 */
	public boolean hasObject(Object o){
		return storageMap.containsValue(o);
	}
	
	/**
	 * Returns the number of objects (elements) stored
	 * @return The number of objects (elements) stored
	 */
	public int getSize() {
		return storageMap.size();
	}
	
	/**
	
	/**
	 * Return a String representation of the HashMap<br>
	 * containing all the key-object pairs.
	 */
	@Override
	public String toString() {
		String s = "";
		for(String cKey : storageMap.keySet()){
			s += cKey + " :: " + storageMap.get(cKey) + "\n";
		}
		return s.trim();
	}

}
