import com.acme.Simple;
import org.junit.Test;

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
