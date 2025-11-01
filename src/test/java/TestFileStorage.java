import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blogspot.debukkitsblog.util.FileStorage;

class TestFileStorage {
	
	static File testFile = new File(System.getProperty("java.io.tempdir"), "filestoragetest.temp");
	
	static String testString1 = "Test String to generate a unique UUID";
	static UUID testObject1;
	
	@BeforeAll
	static void prepareTests() {
		testObject1 = UUID.randomUUID();
	}
	
	@BeforeEach
	void removeFileBeforeEachTest() {
		testFile.delete();
	}
	
	@Test
	void testA_Write() {
		
		try {
			FileStorage fs = new FileStorage(testFile, new Class[] { String.class, UUID.class } );
			fs.put("TestKey1", testString1);
			fs.put("TestKey2", testObject1);

			return; // passed
			
		} catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
			fail(e);
		}
		
	}

	@Test
	void testB_Read() {
		
		try {
			FileStorage fs = new FileStorage(testFile, new Class[] { String.class, UUID.class } );
			fs.put("TestKey1", testString1);
			fs.put("TestKey2", testObject1);
			
			Object o1 = fs.get("TestKey1");
			Object o2 = fs.get("TestKey2");
			
			String stringRead = (String) o1;
			UUID uuidRead = (UUID) o2;
			
			assertEquals(testString1, stringRead);
			assertEquals(testObject1.toString(), uuidRead.toString());
			
			return; // passed
			
		} catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
			fail(e);
		}		
		
	}
	
	@Test
	void testC_Remove() {
		
		try {
			FileStorage fs = new FileStorage(testFile, new Class[] { String.class } );
			fs.put("TestKey1", testString1);
			fs.remove("TestKey1");
			
			FileStorage fs2 = new FileStorage(testFile, new Class[] { String.class } );
			assertNull(fs2.get("TestKey1"));

			return; // passed
			
		} catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
			fail(e);
		}
		
	}

	@Test
	void testD_ReadNonWhitelistedObject() {
		try {
			FileStorage fs3 = new FileStorage(testFile, new Class[] { String.class });
			fs3.put("TestKey2", testObject1);
			
			assertThrows(InvalidClassException.class, () -> new FileStorage(testFile, new Class[] { String.class }));
			
		} catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
			fail(e);
		}				
	}
	
	@AfterAll
	static void clearAfterTests() {
		testFile.delete();
	}

}
