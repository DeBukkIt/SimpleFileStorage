package com.blogspot.debukkitsblog.util;

import java.io.*;
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
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storageFile))) {
			out.writeObject(this);
		}
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
	public void load() throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(storageFile))) {
			FileStorage loaded = (FileStorage) in.readObject();
			this.clear();
			this.putAll(loaded);
		}
	}

	/**
	 * Adds classes to the list of allowed classes that can be stored or loaded.
	 *
	 * @param classes Array of classes to allow
	 */
	public void addAllowedClasses(Class<?>[] classes) {
		if (classes != null) {
			for (Class<?> cls : classes) {
				if (!allowedClasses.contains(cls)) {
					allowedClasses.add(cls);
				}
			}
		}
	}

	/**
	 * Adds classes that are necessary for the FileStorage functionality itself.
	 * <p>
	 * This typically includes standard Java classes such as HashMap and ArrayList
	 * to ensure that the internal structure can be serialized correctly.
	 */
	private void allowNecessaryClasses() {
		allowedClasses.add(HashMap.class);
		allowedClasses.add(ArrayList.class);
	}

	/**
	 * Handles exceptions that occur during automatic saving.
	 * <p>
	 * By default, this method prints the stack trace. Subclasses can override this
	 * method to provide custom exception handling behavior.
	 *
	 * @param e The exception that occurred
	 */
	protected void onException(Exception e) {
		e.printStackTrace();
	}

	/**
	 * Returns whether autosave is enabled.
	 *
	 * @return true if autosave is enabled, false otherwise
	 */
	public boolean isAutosave() {
		return autosave;
	}

	/**
     * Sets whether autosave is enabled.
     * <p>
     * When enabled, changes to this FileStorage are saved automatically to the file.
     *
     * @param autosave true to enable autosave, false to disable it
     */
    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
    }
    
}
