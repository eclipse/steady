/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.cve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.steady.shared.cache.CacheException;
import org.junit.Test;

public class CveReader2Test {

  static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  /**
   * Create several instances of {@link Cve} from local JSON data.
   * @throws CacheException
   */
  @Test
  public void testBuildFromJson() throws CacheException {
    NvdRestServiceMockup.create();
    try {
      final Cve cve_2014_0050 = CveReader2.read("CVE-2014-0050");
      assertEquals(Float.valueOf("7.5"), cve_2014_0050.getCvssScore());
      assertEquals("2.0", cve_2014_0050.getCvssVersion());
      assertEquals("AV:N/AC:L/Au:N/C:P/I:P/A:P", cve_2014_0050.getCvssVector());
      assertEquals(
          "MultipartStream.java in Apache Commons FileUpload before 1.3.1, as used in Apache"
              + " Tomcat, JBoss Web, and other products, allows remote attackers to cause a denial"
              + " of service (infinite loop and CPU consumption) via a crafted Content-Type header"
              + " that bypasses a loop's intended exit conditions.",
          cve_2014_0050.getSummary());
      final Calendar publ = new GregorianCalendar();
      publ.setTime(format.parse("2014-04-01T06:27Z"));
      assertEquals(publ, cve_2014_0050.getPublished());
      final Calendar modi = new GregorianCalendar();
      modi.setTime(format.parse("2018-10-09T19:35Z"));
      assertEquals(modi, cve_2014_0050.getModified());

      final Cve cve_2018_0123 = CveReader2.read("CVE-2018-0123");
      assertEquals(Float.valueOf("5.5"), cve_2018_0123.getCvssScore());
      assertEquals("3.0", cve_2018_0123.getCvssVersion());
      assertEquals("CVSS:3.0/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:H/A:N", cve_2018_0123.getCvssVector());
      assertEquals(
          "A Path Traversal vulnerability in the diagnostic shell for Cisco IOS and IOS XE"
              + " Software could allow an authenticated, local attacker to use certain diagnostic"
              + " shell commands that can overwrite system files. These system files may be"
              + " sensitive and should not be able to be overwritten by a user of the diagnostic"
              + " shell. The vulnerability is due to lack of proper input validation for certain"
              + " diagnostic shell commands. An attacker could exploit this vulnerability by"
              + " authenticating to the device, entering the diagnostic shell, and providing"
              + " crafted user input to commands at the local diagnostic shell CLI. Successful"
              + " exploitation could allow the attacker to overwrite system files that should be"
              + " restricted. Cisco Bug IDs: CSCvg41950.",
          cve_2018_0123
              .getSummary()); // lang was changed to 'de', thus, no english description is found
      publ.setTime(format.parse("2018-02-08T07:29Z"));
      assertEquals(publ, cve_2018_0123.getPublished());
      modi.setTime(format.parse("2018-03-13T13:53Z"));
      assertEquals(modi, cve_2018_0123.getModified());

      final Cve cve_2018_1000865 = CveReader2.read("CVE-2018-1000865");
      assertEquals(Float.valueOf("8.8"), cve_2018_1000865.getCvssScore());
      assertEquals("3.0", cve_2018_1000865.getCvssVersion());
      assertEquals(
          "CVSS:3.0/AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:H", cve_2018_1000865.getCvssVector());
      assertEquals(
          "A sandbox bypass vulnerability exists in Script Security Plugin 1.47 and earlier in"
              + " groovy-sandbox/src/main/java/org/kohsuke/groovy/sandbox/SandboxTransformer.java"
              + " that allows attackers with Job/Configure permission to execute arbitrary code on"
              + " the Jenkins master JVM, if plugins using the Groovy sandbox are installed.",
          cve_2018_1000865.getSummary());
      publ.setTime(format.parse("2018-12-10T14:29Z"));
      assertEquals(publ, cve_2018_1000865.getPublished());
      modi.setTime(format.parse("2019-02-06T19:02Z"));
      assertEquals(modi, cve_2018_1000865.getModified());

      // Check CVSS version is properly read (3.1)
      final Cve cve_2019_0047 = CveReader2.read("CVE-2019-0047");
      assertEquals(Float.valueOf("8.8"), cve_2019_0047.getCvssScore());
      assertEquals("3.1", cve_2019_0047.getCvssVersion());
      assertEquals("CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:H", cve_2019_0047.getCvssVector());
      assertEquals(
          "A persistent Cross-Site Scripting (XSS) vulnerability in Junos OS J-Web interface may"
              + " allow remote unauthenticated attackers to perform administrative actions on the"
              + " Junos device. Successful exploitation requires a Junos administrator to first"
              + " perform certain diagnostic actions on J-Web. This issue affects: Juniper"
              + " Networks Junos OS 12.1X46 versions prior to 12.1X46-D86; 12.3 versions prior to"
              + " 12.3R12-S13; 12.3X48 versions prior to 12.3X48-D80; 14.1X53 versions prior to"
              + " 14.1X53-D51; 15.1 versions prior to 15.1F6-S13, 15.1R7-S4; 15.1X49 versions"
              + " prior to 15.1X49-D171, 15.1X49-D180; 15.1X53 versions prior to 15.1X53-D497,"
              + " 15.1X53-D69; 16.1 versions prior to 16.1R7-S5; 16.2 versions prior to 16.2R2-S9;"
              + " 17.1 versions prior to 17.1R3; 17.2 versions prior to 17.2R1-S8, 17.2R2-S7,"
              + " 17.2R3-S1; 17.3 versions prior to 17.3R3-S6; 17.4 versions prior to 17.4R1-S7,"
              + " 17.4R2-S4, 17.4R3; 18.1 versions prior to 18.1R3-S5; 18.2 versions prior to"
              + " 18.2R1-S5, 18.2R2-S3, 18.2R3; 18.3 versions prior to 18.3R1-S3, 18.3R2, 18.3R3;"
              + " 18.4 versions prior to 18.4R1-S2, 18.4R2.",
          cve_2019_0047.getSummary());
    } catch (ParseException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /**
   * Fetches CVE data.
   * Requires that system property vulas.backend.cveCache.serviceUrl is set.
   * @throws CacheException
   */
  @Test
  public void testFetch() throws CacheException {
    NvdRestServiceMockup.create();
    final CveReader2 reader = new CveReader2();
    reader.fetch("CVE-2019-17531");
  }
}
