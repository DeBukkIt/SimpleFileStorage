import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
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
		testObject1 = UUID.randomUUID();
	}
	
	@Test
	void testA_Write() {
		
		try {
			FileStorage fs = new FileStorage(testFile);
			fs.put("TestKey1", testString1);
			fs.put("TestKey2", testObject1);

			return; // passed
			
		} catch (IllegalArgumentException | IOException e) {
			fail(e);
		}
		
	}
	
	@Test
	void testB_Remove() {
		
		try {
			FileStorage fs = new FileStorage(testFile);
			fs.remove((Object) "TestKey1");
			
			FileStorage fs2 = new FileStorage(testFile);
			assertNull(fs2.get("TestKey1"));

			return; // passed
			
		} catch (IllegalArgumentException | IOException e) {
			fail(e);
		}
		
	}

	@Test
	void testC_Read() {
		
		try {
			FileStorage fs = new FileStorage(testFile);
			Object o1 = fs.get("TestKey1");
			Object o2 = fs.get("TestKey2");
			
			UUID uuidRead = (UUID) o2;
			
			assertNull(o1);
			assertEquals(testObject1.toString(), uuidRead.toString());
			
			return; // passed
			
		} catch (IllegalArgumentException | IOException e) {
			fail(e);
		}		
		
	}
	
	@AfterAll
	static void clearAfterTests() {
		testFile.delete();
	}

}
