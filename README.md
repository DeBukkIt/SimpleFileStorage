# SimpleFileStorage
A very, very, very light-weight utility for Java applications allowing you to store all kind of serializable object in one or more files under a certain key.

**Make a new instance of this class, tell it where to store your data and store them!**
* **public FileStorage(File)** Creates a FileStorage.

* **store(String, Object)** Stores an Object o using a String key for later identification

* **get(String)** Returns your object from the storage

* **store(String, Object, String)** Stores an Object o using a String key for later identification. The object is AES encrypted first using the password you choose.

* **get(String, String)** Returns your encrypted object from the storage (using your password to decrypt it first).

* **getFirstKey(Object)** Returns the first key corresponding to the object

* **getAll()** Returns all stored objects in a HashMap of Strings and Objects

* **getAllAsArrayList()** Returns all stored objects in an ArrayList of Objects

* **save()** Saves the stored objects to the File

* **close()** Closes the FileStream by saving the stored objects to the File
 
* **printAll()** Prints all stored keys with corresponding objects

* **remove(String)** Removes an Key-Object pair from the storage

* **remove(Object)** Removes an Key-Object pair from the storage

* **hasKey(String)** Checks whether a key is registered

* **hasObject(Object)** Checks whether an object is stored at all

* **getSize()** Returns the number of objects stored
