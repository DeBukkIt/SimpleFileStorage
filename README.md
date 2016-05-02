# SimpleFileStorage
A very, very, very light-weight utility for Java applications allowing you to store all kind of serializable object in one or more files under a certain key.

**Make a new instance of this class, tell it where to store your data and store them!**
* **public FileStorage(File)** Creates a FileStorage.

* **store(String, Object)** Stores an Object o using a String key for later identification

* **get(String)** Returns your object from the storage

* **getAll()** Returns all stored objects in an ArrayList of Objects

* **remove(String)** Removes an Key-Object pair from the storage

* **hasKey(String)** Checks whether a key is registerd

* **hasObject(Object)** Checks whether an object is stored at all
