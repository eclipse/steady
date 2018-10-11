package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;

import org.junit.Test;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;

public class FileUtilTest {

	@Test
	public void testGetDigest() {
		final File f = new File("./src/test/resources/foo.txt");
		
		final String sha1 = FileUtil.getSHA1(f).toLowerCase();
		final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
		assertEquals(expected_sha1, sha1);
		
		final String md5 = FileUtil.getDigest(f, DigestAlgorithm.MD5).toLowerCase();
		final String expected_md5 = "ab07acbb1e496801937adfa772424bf7";
		assertEquals(expected_md5, md5);
	}
	
	@Test
	public void testGetCharset() {
		VulasConfiguration.getGlobal().setProperty(VulasConfiguration.CHARSET, "foo");
		final Charset cs = FileUtil.getCharset();
		assertEquals("UTF-8", cs.name());
	}
}
