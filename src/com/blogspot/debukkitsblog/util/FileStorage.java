package com.blogspot.debukkitsblog.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.blogspot.debukkitsblog.crypt.CryptedObject;
import com.blogspot.debukkitsblog.crypt.Crypter;
import com.blogspot.debukkitsblog.crypt.Crypter.DecryptionFailedException;

public class FileStorage {

	private File storageFile;
	private HashMap<String, Object> storageMap;

	private boolean autosave;

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.<br>
	 * All data <code>store</code>d in this FileStorage will instantly be stored in
	 * the given file. This might cause many write operations on disk.
	 * 
	 * @param autosave
	 *            Whether every <code>store</code> operation shall automatically
	 *            write the whole FileStorage to disk. If false, you will need to
	 *            call the <code>save</code> method manually.
	 * @param file
	 *            The file your data shall be stored in
	 * @throws IOException
	 *             if the file cannot be created
	 * @throws IllegalArgumentException
	 *             if the file is a directory
	 */
	public FileStorage(File file, boolean autosave) throws IllegalArgumentException, IOException {
		this(file);
		this.autosave = autosave;
	}

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.<br>
	 * All data <code>store</code>d in this FileStorage will instantly be stored in
	 * the given file. This might cause many write operations on disk.
	 * 
	 * @param file
	 *            The file your data shall be stored in
	 * @throws IOException
	 *             if your file cannot be created
	 * @throws IllegalArgumentException
	 *             if your file is a directory
	 */
	public FileStorage(File file) throws IOException, IllegalArgumentException {
		this.storageFile = file;
		autosave = true;

		if (storageFile.isDirectory()) {
			throw new IllegalArgumentException("FileStorage file must not be a directory");
		}

		if (storageFile.createNewFile()) {
			storageMap = new HashMap<String, Object>();
			save();
		} else {
			load();
		}
	}

	/**
	 * Stores the FileStorage in the file on disk
	 */
	public void save() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storageFile));
		oos.writeObject(storageMap);
		oos.flush();
		oos.close();
	}

	/**
	 * Loads the FileStorage from the file
	 */
	@SuppressWarnings("unchecked")
	private void load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storageFile));
			storageMap = (HashMap<String, Object>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores an Object <i>o</i> using a String <i>key</i> for later
	 * identification.<br>
	 * Use <code>store(String key, Object o, String password)</code> for storing
	 * your data using AES encryption.
	 * 
	 * @param key
	 *            The key as String.
	 * @param o
	 *            The Object.
	 */
	public void store(String key, Object o) {
		storageMap.put(key, o);
		if (autosave)
			save();
	}

	/**
	 * Stores an Object <i>o</i> using a String <i>key</i> for later
	 * identification.<br>
	 * Use <code>store(String key, Object o)</code> for storing your data without
	 * encryption.
	 * 
	 * @param key
	 *            The key as String.
	 * @param o
	 *            The Object.
	 * @param password
	 *            The password.
	 */
	public void store(String key, Object o, String password) {
		store(key, Crypter.encrypt(o, password));
	}

	/**
	 * Reads your object from the storage.<br>
	 * <br>
	 * Use <u>get(String key, String password)</u> for AES encrypted objects.
	 * 
	 * @param key
	 *            The key the object is available under
	 * @return your Object or null if nothing was found for <i>key</i>
	 */
	public Object get(String key) {
		return storageMap.get(key);
	}

	/**
	 * Reads your AES encrypted object from the storage.<br>
	 * <br>
	 * Use <u>get(String key)</u> instead for unencrypted objects.
	 * 
	 * @param key
	 *            The key the object is available under
	 * @param password
	 *            The password to use for decryption
	 * @return your object or null if nothing was found for <i>key</i> or if
	 *         decryption failed (wrong password)
	 * @throws DecryptionFailedException
	 *             This usually happens if the password is wrong
	 */
	public Object get(String key, String password) throws DecryptionFailedException {
		if (storageMap.get(key) instanceof CryptedObject) {
			return Crypter.decrypt((CryptedObject) get(key), password);
		} else {
			return get(key);
		}
	}

	/**
	 * All stored objects in an ArrayList of Objects
	 * 
	 * @return all stored objects in an ArrayList of Objects
	 */
	public ArrayList<Object> getAllAsArrayList() {
		ArrayList<Object> result = new ArrayList<Object>();
		for (Object c : storageMap.values()) {
			result.add(c);
		}
		return result;
	}

	/**
	 * All stored objects in a HashMap of Strings and Objects
	 * 
	 * @return all stored objects in a HashMap of Strings and Objects
	 */
	public HashMap<String, Object> getAll() {
		return storageMap;
	}

	/**
	 * Prints all stored keys with corresponding objects
	 */
	public void printAll() {
		System.out.println(this);
	}

	/**
	 * Removes an Key-Object pair from the storage
	 * 
	 * @param key
	 *            The key of the object
	 */
	public void remove(String key) {
		storageMap.remove(key);
		if (autosave)
			save();
	}

	/**
	 * Checks whether a key is registerd
	 * 
	 * @param key
	 *            The Key.
	 * @return true if an object is available for that key
	 */
	public boolean hasKey(String key) {
		return storageMap.containsKey(key);
	}

	/**
	 * Checks whether an object is stored at all
	 * 
	 * @param o
	 *            The Object.
	 * @return true if the object is stored
	 */
	public boolean hasObject(Object o) {
		return storageMap.containsValue(o);
	}

	/**
	 * Returns the number of objects (elements) stored
	 * 
	 * @return The number of objects (elements) stored
	 */
	public int getSize() {
		return storageMap.size();
	}

	/**
	 * @return a String representation of the HashMap containing all the key-object
	 *         pairs.
	 */
	@Override
	public String toString() {
		String result = "FileStorage @ " + storageFile.getAbsolutePath() + "\n";
		for (String cKey : storageMap.keySet()) {
			if (storageMap.get(cKey) instanceof CryptedObject) {
				result += cKey + " :: (Encrypted)\n";
			} else {
				result += cKey + " :: " + storageMap.get(cKey) + "\n";
			}
		}
		return result.trim();
	}

}
