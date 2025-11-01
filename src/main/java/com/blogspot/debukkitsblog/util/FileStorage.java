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

/**
 * FileStorage provides a mechanism to store and load serializable objects in a
 * file using unique string keys.
 * <p>
 * This class extends {@link HashMap} and supports optional automatic saving on
 * every modification (put/remove) or manual saving via {@link #save()}.
 * <p>
 * For security purposes, only specific classes allowed via
 * {@link #addAllowedClasses(Class[])} can be stored or loaded.
 * 
 * <p>
 * Usage example:
 * 
 * <pre>
 * FileStorage storage = new FileStorage("data.dat", true, new Class[] { MyClass.class });
 * storage.put("key1", myObject);
 * MyClass obj = (MyClass) storage.get("key1");
 * </pre>
 * 
 * @author DeBukkIt
 * @version 1.3.0
 */
public class FileStorage extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6375762323691191760L;

	private File storageFile;
	private List<Class<?>> allowedClasses;

	private boolean autosave = true;

	/**
	 * Creates a FileStorage instance with a specified file path and optional
	 * automatic saving.
	 *
	 * @param filepath       The file path where data will be stored
	 * @param autosave       If true, the data will be automatically saved after
	 *                       each {@link #put(String, Object)} operation
	 * @param allowedClasses Array of classes that are allowed to be stored and
	 *                       loaded
	 * @throws IOException              If the file cannot be created or opened
	 * @throws IllegalArgumentException If the specified file is a directory
	 * @throws ClassNotFoundException   If an unknown class is encountered when
	 *                                  loading
	 */
	public FileStorage(String filepath, boolean autosave, Class<?>[] allowedClasses)
			throws IllegalArgumentException, IOException, ClassNotFoundException {
		this(new File(filepath), autosave, allowedClasses);
	}

	/**
	 * Creates a FileStorage instance with a specified file path.
	 * <p>
	 * All changes will be automatically saved.
	 *
	 * @param filepath       The file path where data will be stored
	 * @param allowedClasses Array of classes that are allowed to be stored and
	 *                       loaded
	 * @throws IOException              If the file cannot be created or opened
	 * @throws IllegalArgumentException If the specified file is a directory
	 * @throws ClassNotFoundException   If an unknown class is encountered when
	 *                                  loading
	 */
	public FileStorage(String filepath, Class<?>[] allowedClasses)
			throws IOException, IllegalArgumentException, ClassNotFoundException {
		this(new File(filepath), allowedClasses);
	}

	/**
	 * Creates a FileStorage instance with a specified file and optional automatic
	 * saving.
	 *
	 * @param file           The file where data will be stored
	 * @param autosave       If true, the data will be automatically saved after
	 *                       each {@link #put(String, Object)} operation
	 * @param allowedClasses Array of classes that are allowed to be stored and
	 *                       loaded
	 * @throws IOException              If the file cannot be created or opened
	 * @throws IllegalArgumentException If the specified file is a directory
	 * @throws ClassNotFoundException   If an unknown class is encountered when
	 *                                  loading
	 */
	public FileStorage(File file, boolean autosave, Class<?>[] allowedClasses)
			throws IllegalArgumentException, IOException, ClassNotFoundException {
		this(file, allowedClasses);
		this.autosave = autosave;
	}

	/**
	 * Creates a FileStorage instance with a specified file.
	 * <p>
	 * All changes will be saved automatically unless autosave is disabled.
	 *
	 * @param file           The file where data will be stored
	 * @param allowedClasses Array of classes that are allowed to be stored and
	 *                       loaded
	 * @throws IOException              If the file cannot be created or opened
	 * @throws IllegalArgumentException If the specified file is a directory
	 * @throws ClassNotFoundException   If an unknown class is encountered when
	 *                                  loading
	 */
	public FileStorage(File file, Class<?>[] allowedClasses)
			throws IOException, IllegalArgumentException, ClassNotFoundException {
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
	 * Inserts an object associated with a key. If a value already exists under the
	 * key, it will be overwritten.
	 * <p>
	 * If autosave is enabled, the file will be updated immediately after insertion.
	 *
	 * @param key   The key under which the object will be stored
	 * @param value The object to store
	 * @return The previous value associated with the key, or null if none existed
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
	 * Removes an object associated with a given key.
	 * <p>
	 * If autosave is enabled, the file will be updated immediately after removal.
	 *
	 * @param key The key of the object to remove
	 * @return The removed value, or null if none existed
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
	 * Saves the current contents of this FileStorage to the underlying file.
	 * <p>
	 * Only objects of allowed classes will be written to the file. This method can
	 * throw an {@link IOException} if an I/O error occurs.
	 *
	 * @throws IOException If an error occurs while writing to the file
	 */
	public void save() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(storageFile)));
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}

	/**
	 * Loads the contents of this FileStorage from the underlying file.
	 * <p>
	 * Only objects of allowed classes will be loaded. If an object of an unknown
	 * class is encountered, a {@link ClassNotFoundException} is thrown.
	 *
	 * @throws IOException            If an error occurs while reading from the file
	 * @throws ClassNotFoundException If an object of an unknown class is
	 *                                encountered
	 */
	protected void load() throws IOException, ClassNotFoundException {
		if (allowedClasses.isEmpty()) {
			throw new IllegalStateException(
					"No classes whitelisted, cannot load anything from file for security reasons. Use addAllowedClass(...) to whitelist classes.");
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
	 * Adds classes to the list of allowed classes that can be stored or loaded.
	 *
	 * @param allowedClasses Array of classes to allow
	 */
	public void addAllowedClasses(Class<?>[] allowedClasses) {
		for (Class<?> c : allowedClasses) {
			this.allowedClasses.add(c);
		}
	}

	/**
	 * Removes classes from the list of allowed classes that can be stored or
	 * loaded.
	 * 
	 * @param allowedClasses Array of classes to remove
	 */
	public void removeAllowedClasses(Class<?>[] allowedClasses) {
		for (Class<?> c : allowedClasses) {
			this.allowedClasses.remove(c);
		}
	}

	/**
	 * Adds classes that are necessary for the FileStorage functionality itself.
	 * <p>
	 * This typically includes standard Java classes such as HashMap and ArrayList
	 * to ensure that the internal structure can be serialized correctly.
	 */
	protected void allowNecessaryClasses() {
		addAllowedClasses(new Class[] { FileStorage.class, HashMap.class, ArrayList.class, File.class });
	}

	/**
	 * Generates an ObjectInputFilter object that rejects all classes that are not
	 * explicitly allowed
	 * 
	 * @return an ObjectInputFilter object that rejects all classes that are not
	 *         explicitly allowed
	 */
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
	 * Handles exceptions that occur during automatic saving.
	 * <p>
	 * By default, this method prints the stack trace. Subclasses can override this
	 * method to provide custom exception handling behavior.
	 *
	 * @param e The exception that occurred
	 */
	public void onException(Exception e) {
		e.printStackTrace();
	}

}
