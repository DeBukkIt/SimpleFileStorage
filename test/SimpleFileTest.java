import com.blogspot.debukkitsblog.util.FileStorage;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileTest {
    @Test
    public void save10Objects() throws IOException {
        Map<String, Object> data = new HashMap<>();
        FileStorage storage = new FileStorage(File.createTempFile("s", "f"), true);
        for(int i =0; i < 10; i++){
            TestSerializableObject obj = new TestSerializableObject();
            String id = String.valueOf(i);
            data.put(id,obj);
            storage.store(id, obj);
        }
        Assert.assertEquals(data,storage.getAll());
    }
}
