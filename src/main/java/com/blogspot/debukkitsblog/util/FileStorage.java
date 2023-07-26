package com.blogspot.debukkitsblog.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

public class FileStorage extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1049797586593808789L;

	private File storageFile;

	private boolean autosave = true;

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.
	 * 
	 * @param autosave Whether every <code>store</code> operation shall
	 *                 automatically write the whole FileStorage to disk. If false,
	 *                 you will need to call the <code>save</code> method manually.
	 * @param filepath The path of the file your data shall be stored in
	 * @throws IOException              if the file cannot be created
	 * @throws IllegalArgumentException if the file is a directory
	 */
	public FileStorage(String filepath, boolean autosave) throws IllegalArgumentException, IOException {
		this(new File(filepath), autosave);
	}

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.<br>
	 * All data <code>store</code>d in this FileStorage will instantly be stored in
	 * the given file. This might cause many write operations on disk.
	 * 
	 * @param file The file your data shall be stored in
	 * @throws IOException              if your file cannot be created
	 * @throws IllegalArgumentException if your file is a directory
	 */
	public FileStorage(String filepath) throws IOException, IllegalArgumentException {
		this(new File(filepath));
	}

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.
	 * 
	 * @param autosave Whether every <code>store</code> operation shall
	 *                 automatically write the whole FileStorage to disk. If false,
	 *                 you will need to call the <code>save</code> method manually.
	 * @param file     The file your data shall be stored in
	 * @throws IOException              if the file cannot be created
	 * @throws IllegalArgumentException if the file is a directory
	 */
	public FileStorage(File file, boolean autosave) throws IllegalArgumentException, IOException {
		this(file);
		this.autosave = autosave;
	}

	/**
	 * Creates a FileStorage. It allows you to store your serializable object in a
	 * file using a key for identification and to read it somewhen later.<br>
	 * All data <code>store</code>d in this FileStorage will be stored in the given
	 * file. This might cause many write operations on disk. Disable the autosave
	 * option with the appropriate constructor to prevent this and instead
	 * <code>save</code> manually at desired times.
	 * 
	 * @param file The file your data shall be stored in
	 * @throws IOException              if your file cannot be created
	 * @throws IllegalArgumentException if your file is a directory
	 */
	public FileStorage(File file) throws IOException, IllegalArgumentException {
		this.storageFile = file;

		if (storageFile.isDirectory()) {
			throw new IllegalArgumentException("FileStorage file must not be a directory");
		}

		if (storageFile.length() == 0 || storageFile.createNewFile()) {
			save();
		} else {
			load();
		}
	}
	
	/**
	 * Associates the specified value with the specified key in this map.If the map
	 * previously contained a mapping for the key, the oldvalue is replaced.<br>
	 * If autosave is enabled (see constructor options), the FileStorage is
	 * immediately written to the file system after putting; if not, use
	 * <code>save()</code> to write it manually whenever you want.
	 */
	@Override
	public Object put(String key, Object value) {
		Object result = super.put(key, value);
		if (autosave) {
			try {
				save();
			} catch (IOException e) {
				onException(e);
			}
		}
		return result;
	}
	
	/**
	 * Removes the mapping for the specified key from this map if present.
	 * If autosave is enabled (see constructor options), the FileStorage is
	 * immediately written to the file system after removing; if not, use
	 * <code>save()</code> to write it manually whenever you want.
	 */
	@Override
	public Object remove(Object key) {
		Object result = super.remove(key);
		if (autosave) {
			try {
				save();
			} catch (IOException e) {
				onException(e);
			}
		}		
		return result;
	}

	/**
	 * Stores the FileStorage in the file on disk
	 */
	public void save() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(storageFile)));
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}

	/**
	 * Loads the FileStorage from the file
	 */
	private void load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(storageFile)));
			FileStorage fileStorageFromFile = (FileStorage) ois.readObject();
			ois.close();
			
			// Clear, then populate the inherited HashMap with objects
			this.clear();
			for(String remoteKey : fileStorageFromFile.keySet()) {
				this.put(remoteKey, fileStorageFromFile.get(remoteKey));
			}
			
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
	 * @param key The key as String.
	 * @param o   The Object.
	 * @deprecated Use put(String key, Object value) instead
	 */
	@Deprecated
	public void store(String key, Object o) throws IOException {
		this.put(key, o);
	}

	/**
	 * Stores an Object <i>o</i> using a String <i>key</i> for later
	 * identification.<br>
	 * Use <code>store(String key, Object o)</code> for storing your data without
	 * encryption.
	 * 
	 * @param key      The key as String.
	 * @param o        The Object.
	 * @param password The password.
	 * @deprecated Encryption probably very insecure, this method will be removed in a future release
	 */
	@Deprecated
	public void store(String key, Object o, String password) throws IOException {
		store(key, Crypter.encrypt(o, password));
	}

	/**
	 * Reads your object from the storage.<br>
	 * <br>
	 * Use <u>get(String key, String password)</u> for AES encrypted objects.
	 * 
	 * @param key The key the object is available under
	 * @return your Object or null if nothing was found for <i>key</i>
	 */
	public Object get(String key) {
		return super.get(key);
	}
	
	/**
	 * Reads your AES encrypted object from the storage.<br>
	 * <br>
	 * Use <u>get(String key)</u> instead for unencrypted objects.
	 * 
	 * @param key      The key the object is available under
	 * @param password The password to use for decryption
	 * @return your object or null if nothing was found for <i>key</i> or if
	 *         decryption failed (wrong password)
	 * @throws DecryptionFailedException This usually happens if the password is
	 *                                   wrong
	 * @deprecated Encryption probably very insecure, this method will be removed in a future release
	 */
	@Deprecated
	public Object get(String key, String password) throws DecryptionFailedException {
		Object result = super.get(key);
		if (result instanceof CryptedObject) {
			return Crypter.decrypt((CryptedObject) result, password);
		} else {
			return get(key);
		}
	}

	/**
	 * All stored objects in an ArrayList of Objects
	 * 
	 * @return all stored objects in an ArrayList of Objects
	 * @deprecated Use values() instead 
	 */
	@Deprecated
	public ArrayList<Object> getAllAsArrayList() {
		return new ArrayList<Object>(super.values());
	}

	/**
	 * All stored objects in a HashMap of Strings and Objects
	 * 
	 * @return all stored objects in a HashMap of Strings and Objects
	 * @deprecated Typecast this instance to a HashMap instead (if you need one at all)
	 */
	@Deprecated
	public HashMap<String, Object> getAll() {
		return this;
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
	 * @param key The key of the object
	 * @deprecated Use remove(Object key) instead
	 */
	@Deprecated
	public void remove(String key) throws IOException {
		super.remove(key);
		if (autosave) {
			try {
				save();
			} catch (IOException e) {
				onException(e);
			}
		}
	}

	/**
	 * Checks whether a key is registerd
	 * 
	 * @param key The Key.
	 * @return true if an object is available for that key
	 * @deprecated Use containsKey(Object value) instead
	 */
	@Deprecated
	public boolean hasKey(String key) {
		return super.containsKey(key);
	}
	
	/**
	 * Checks whether an object is stored at all
	 * 
	 * @param o The Object.
	 * @return true if the object is stored
	 * @deprecated Use containsValue(Object value) instead
	 */
	@Deprecated
	public boolean hasObject(Object o) {
		return super.containsValue(o);
	}

	/**
	 * Returns the number of objects (elements) stored
	 * 
	 * @return The number of objects (elements) stored
	 * @deprecated Use size() instead
	 */
	@Deprecated
	public int getSize() {
		return super.size();
	}

	/**
	 * @return a String representation of the HashMap containing all the key-object
	 *         pairs.
	 */
	@Override
	public String toString() {
		String result = "FileStorage @ " + storageFile.getAbsolutePath() + "\n";
		for (String cKey : super.keySet()) {
			if (super.get(cKey) instanceof CryptedObject) {
				result += cKey + " :: (Encrypted)\n";
			} else {
				result += cKey + " :: " + super.get(cKey) + "\n";
			}
		}
		return result.trim();
	}
	
	/**
	 * Executed whenever an exception is thrown within this class. By default, the
	 * printStackTrace() method of the respective exception is executed. Override
	 * this method to handle exceptions differently.
	 * 
	 * @param e The Exception thrown
	 */
	public void onException(Exception e) {
		e.printStackTrace();
	}

}
