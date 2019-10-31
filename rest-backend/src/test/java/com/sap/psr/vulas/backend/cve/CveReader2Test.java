package com.sap.psr.vulas.backend.cve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.util.FileUtil;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

public class CveReader2Test {

  static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  @Test
  public void testBuildFromJson() throws CacheException {
    try {
      final Cve cve_2014_0050 =
          CveReader2.buildFromJson(
              "CVE-2014-0050",
              FileUtil.readFile("./src/test/resources/cves/cve-2014-0050-new.json"));
      assertEquals(Float.valueOf("7.5"), cve_2014_0050.getCvssScore());
      assertEquals("2.0", cve_2014_0050.getCvssVersion());
      assertEquals("AV:N/AC:L/Au:N/C:P/I:P/A:P", cve_2014_0050.getCvssVector());
      assertEquals(
          "MultipartStream.java in Apache Commons FileUpload before 1.3.1, as used in Apache Tomcat, JBoss Web, and other products, allows remote attackers to cause a denial of service (infinite loop and CPU consumption) via a crafted Content-Type header that bypasses a loop's intended exit conditions.",
          cve_2014_0050.getSummary());
      final Calendar publ = new GregorianCalendar();
      publ.setTime(format.parse("2014-04-01T06:27Z"));
      assertEquals(publ, cve_2014_0050.getPublished());
      final Calendar modi = new GregorianCalendar();
      modi.setTime(format.parse("2018-10-09T19:35Z"));
      assertEquals(modi, cve_2014_0050.getModified());

      final Cve cve_2018_0123 =
          CveReader2.buildFromJson(
              "CVE-2018-0123",
              FileUtil.readFile("./src/test/resources/cves/cve-2018-0123-new.json"));
      assertEquals(Float.valueOf("5.5"), cve_2018_0123.getCvssScore());
      assertEquals("3.0", cve_2018_0123.getCvssVersion());
      assertEquals("CVSS:3.0/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:H/A:N", cve_2018_0123.getCvssVector());
      assertEquals(
          "Not available",
          cve_2018_0123
              .getSummary()); // lang was changed to 'de', thus, no english description is found
      publ.setTime(format.parse("2018-02-08T07:29Z"));
      assertEquals(publ, cve_2018_0123.getPublished());
      modi.setTime(format.parse("2018-03-13T13:53Z"));
      assertEquals(modi, cve_2018_0123.getModified());

      final Cve cve_2018_1000865 =
          CveReader2.buildFromJson(
              "CVE-2018-1000865",
              FileUtil.readFile("./src/test/resources/cves/cve-2018-1000865-new.json"));
      assertEquals(Float.valueOf("8.8"), cve_2018_1000865.getCvssScore());
      assertEquals("3.0", cve_2018_1000865.getCvssVersion());
      assertEquals(
          "CVSS:3.0/AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:H", cve_2018_1000865.getCvssVector());
      assertEquals(
          "A sandbox bypass vulnerability exists in Script Security Plugin 1.47 and earlier in groovy-sandbox/src/main/java/org/kohsuke/groovy/sandbox/SandboxTransformer.java that allows attackers with Job/Configure permission to execute arbitrary code on the Jenkins master JVM, if plugins using the Groovy sandbox are installed.",
          cve_2018_1000865.getSummary());
      publ.setTime(format.parse("2018-12-10T14:29Z"));
      assertEquals(publ, cve_2018_1000865.getPublished());
      modi.setTime(format.parse("2019-02-06T19:02Z"));
      assertEquals(modi, cve_2018_1000865.getModified());
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (ParseException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
