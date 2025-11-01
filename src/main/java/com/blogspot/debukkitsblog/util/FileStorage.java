package com.blogspot.debukkitsblog.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileStorage extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6375762323691191760L;

	private File storageFile;
	private List<Class<?>> allowedClasses;

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
	 * @throws ClassNotFoundException 
	 */
	public FileStorage(String filepath, boolean autosave, Class<?>[] allowedClasses) throws IllegalArgumentException, IOException, ClassNotFoundException {
		this(new File(filepath), autosave, allowedClasses);
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
	 * @throws ClassNotFoundException 
	 */
	public FileStorage(String filepath, Class<?>[] allowedClasses) throws IOException, IllegalArgumentException, ClassNotFoundException {
		this(new File(filepath), allowedClasses);
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
	 * @throws ClassNotFoundException 
	 */
	public FileStorage(File file, boolean autosave, Class<?>[] allowedClasses) throws IllegalArgumentException, IOException, ClassNotFoundException {
		this(file, allowedClasses);
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
	 * @throws ClassNotFoundException 
	 */
	public FileStorage(File file, Class<?>[] allowedClasses) throws IOException, IllegalArgumentException, ClassNotFoundException {
		// Enforce canonical file paths for security reasons
		this.storageFile = file.getCanonicalFile();
		this.allowedClasses = new ArrayList<>();
		
		allowNecessaryClasses();
		addAllowedClasses(allowedClasses);

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
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected void load() throws IOException, ClassNotFoundException {
		if(allowedClasses.isEmpty()) {
			throw new IllegalStateException("No classes whitelisted, cannot load anything from file for security reasons. Use addAllowedClass(...) to whitelist classes.");
		}
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(storageFile)))) {
			ois.setObjectInputFilter(generateObjectInputFilter());
			FileStorage fileStorageFromFile = (FileStorage) ois.readObject();
			ois.close();

			// Clear, then populate the inherited HashMap with objects
			this.clear();
			for (String remoteKey : fileStorageFromFile.keySet()) {
				this.put(remoteKey, fileStorageFromFile.get(remoteKey));
			}
		}
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
	
	public void addAllowedClasses(Class<?>[] allowedClasses) {
		for(Class<?> c : allowedClasses) {
			this.allowedClasses.add(c);
		}
	}
	
	public void removeAllowedClasses(Class<?>[] allowedClasses) {
		for(Class<?> c : allowedClasses) {
			this.allowedClasses.remove(c);
		}
	}
	
	protected void allowNecessaryClasses() {
		addAllowedClasses(new Class[] {
			FileStorage.class,
			HashMap.class,
			ArrayList.class,
			File.class
		});
	}
	
	protected ObjectInputFilter generateObjectInputFilter() {
		return (filterInfo) -> {
			Class<?> serialClass = filterInfo.serialClass();
			
			if (serialClass == null) {
				return ObjectInputFilter.Status.UNDECIDED;
			}

			if (serialClass.isArray() || serialClass.isPrimitive() || allowedClasses.contains(serialClass)) {
				return ObjectInputFilter.Status.ALLOWED;
			}
			
			return ObjectInputFilter.Status.REJECTED;
		};
	}

	/**
	 * Prints all stored keys with corresponding objects
	 */
	public void printAll() {
		System.out.println(this);
	}

	/**
	 * @return a String representation of the HashMap containing all the key-object
	 *         pairs.
	 */
	@Override
	public String toString() {
		String result = "FileStorage @ " + storageFile.getAbsolutePath() + "\n";
		for (String cKey : super.keySet()) {
			result += cKey + " :: " + super.get(cKey) + "\n";
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
