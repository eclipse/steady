package com.sap.psr.vulas.backend.cve;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.categories.RequiresNetwork;
import com.sap.psr.vulas.shared.util.FileUtil;

public class CveReader2Test {
	
	final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	@Test
	public void testBuildFromJson() throws CacheException {
		try {
			final Cve cve_2014_0050 = CveReader2.buildFromJson("CVE-2014-0050", FileUtil.readFile("./src/test/resources/cves/cve-2014-0050-new.json"));
			assertEquals(Float.valueOf("7.5"), cve_2014_0050.getCvssScore());
			assertEquals("2.0", cve_2014_0050.getCvssVersion());
			assertEquals("AV:N/AC:L/Au:N/C:P/I:P/A:P", cve_2014_0050.getCvssVector());
			assertEquals("MultipartStream.java in Apache Commons FileUpload before 1.3.1, as used in Apache Tomcat, JBoss Web, and other products, allows remote attackers to cause a denial of service (infinite loop and CPU consumption) via a crafted Content-Type header that bypasses a loop's intended exit conditions.", cve_2014_0050.getSummary());
			final Calendar publ = new GregorianCalendar();
			publ.setTime(format.parse("2014-04-01T06:27Z"));
			assertEquals(publ, cve_2014_0050.getPublished());
			final Calendar modi = new GregorianCalendar();
			modi.setTime(format.parse("2018-10-09T19:35Z"));
			assertEquals(modi, cve_2014_0050.getModified());
			
			final Cve cve_2018_0123 = CveReader2.buildFromJson("CVE-2018-0123", FileUtil.readFile("./src/test/resources/cves/cve-2018-0123-new.json"));
			assertEquals(Float.valueOf("5.5"), cve_2018_0123.getCvssScore());
			assertEquals("3.0", cve_2018_0123.getCvssVersion());
			assertEquals("CVSS:3.0/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:H/A:N", cve_2018_0123.getCvssVector());
			assertEquals("A Path Traversal vulnerability in the diagnostic shell for Cisco IOS and IOS XE Software could allow an authenticated, local attacker to use certain diagnostic shell commands that can overwrite system files. These system files may be sensitive and should not be able to be overwritten by a user of the diagnostic shell. The vulnerability is due to lack of proper input validation for certain diagnostic shell commands. An attacker could exploit this vulnerability by authenticating to the device, entering the diagnostic shell, and providing crafted user input to commands at the local diagnostic shell CLI. Successful exploitation could allow the attacker to overwrite system files that should be restricted. Cisco Bug IDs: CSCvg41950.", cve_2018_0123.getSummary());
			publ.setTime(format.parse("2018-02-08T07:29Z"));
			assertEquals(publ, cve_2018_0123.getPublished());
			modi.setTime(format.parse("2018-03-13T13:53Z"));
			assertEquals(modi, cve_2018_0123.getModified());
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (ParseException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
