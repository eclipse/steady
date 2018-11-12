package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	
	@Test
	public void testCopyFile() {
		try {
			final Path source_file = Paths.get("./src/test/resources/Outer.jar");
			final Path target_file = FileUtil.copyFile(source_file, VulasConfiguration.getGlobal().getTmpDir());
			assertEquals(FileUtil.getDigest(source_file.toFile(), DigestAlgorithm.SHA1), FileUtil.getDigest(target_file.toFile(), DigestAlgorithm.SHA1));
		} catch (IOException e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}
}
