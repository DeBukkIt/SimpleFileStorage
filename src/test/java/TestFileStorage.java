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
import org.junit.jupiter.api.Test;

import com.blogspot.debukkitsblog.util.FileStorage;

class TestFileStorage {
	
	static File testFile = new File(System.getProperty("java.io.tempdir"), "filestoragetest.temp");
	
	static String testString1 = "Test String to generate a unique UUID";
	static UUID testObject1;
	
	@BeforeAll
	static void prepareTests() {
		testFile.delete();
		testObject1 = UUID.randomUUID();
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
	void testB_Remove() {
		
		try {
			FileStorage fs = new FileStorage(testFile, new Class[] { String.class, UUID.class } );
			fs.remove((Object) "TestKey1");
			
			FileStorage fs2 = new FileStorage(testFile, new Class[] { String.class, UUID.class } );
			assertNull(fs2.get("TestKey1"));

			return; // passed
			
		} catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
			fail(e);
		}
		
	}

	@Test
	void testC_Read() {
		
		try {
			FileStorage fs = new FileStorage(testFile, new Class[] { String.class, UUID.class } );
			Object o1 = fs.get("TestKey1");
			Object o2 = fs.get("TestKey2");
			
			UUID uuidRead = (UUID) o2;
			
			assertNull(o1);
			assertEquals(testObject1.toString(), uuidRead.toString());
			
			return; // passed
			
		} catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
			fail(e);
		}		
		
	}

	@Test
	void testD_ReadNonWhitelistedObject() {
		assertThrows(InvalidClassException.class, () ->	new FileStorage(testFile, new Class[] { String.class }));				
	}
	
	@AfterAll
	static void clearAfterTests() {
		testFile.delete();
	}

}
