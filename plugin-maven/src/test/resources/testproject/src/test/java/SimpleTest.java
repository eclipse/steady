import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.junit.Test;

import com.acme.Simple;

public class SimpleTest {

    @Test
    public void callHttpClientTest() {
        Simple p = null;
        try {
            p = new Simple();
            p.callHttpClient("https://example.com");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}