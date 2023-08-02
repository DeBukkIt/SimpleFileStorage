# SimpleFileStorage
A very, very, very lightweight tool to persist any serialisable objects in a file in Java and, for example, load them again after restarting the application.

In principle, this is a HashMap<String, Object> that persists itself. The organisation of the stored data is therefore done via key-value pairs.

To avoid heavy load on the hard disk, `autosave` can be disabled in the constructor. Persistence then only takes place when `save()` is called manually.

## Usage Example
```java
// Init
FileStorage fs = new FileStorage("path/to/my_storage_file.dat");

// Put and store data
fs.put("key1", testObject1);
fs.put("key2", testString1);

// Restart application

// Get data
Object loadedObject = fs.get("key1");
```
