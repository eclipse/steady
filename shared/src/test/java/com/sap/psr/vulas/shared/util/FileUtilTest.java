package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
		final VulasConfiguration cfg = new VulasConfiguration();
		cfg.setProperty(VulasConfiguration.CHARSET, "foo");
		final Charset cs = FileUtil.getCharset();
		assertEquals("UTF-8", cs.name());
	}
	
	@Test
	public void testCopyFile() {
		try {
			final VulasConfiguration cfg = new VulasConfiguration();
			final Path tmp_dir = cfg.getTmpDir();
			final Path source_file = Paths.get("./src/test/resources/Outer.jar");
			final Path target_file = FileUtil.copyFile(source_file, tmp_dir);
			assertEquals(FileUtil.getDigest(source_file.toFile(), DigestAlgorithm.SHA1), FileUtil.getDigest(target_file.toFile(), DigestAlgorithm.SHA1));
		} catch (IOException e) {
			e.printStackTrace();
			assertEquals(true, false);
		}
	}
	
	@Test
	public void testGetJarFilePathsForResources() {
		final String[] resources = new String[] { "LICENSE-junit.txt" }; // Contained in junit-4.12.jar
		final Set<String> jars = FileUtil.getJarFilePathsForResources(FileUtil.class.getClassLoader(), resources);
		assertTrue(jars.size()==1);
	}
	
	@Test
	public void testGetJarFilePaths() {
		final Set<String> jars = FileUtil.getJarFilePaths((URLClassLoader)FileUtil.class.getClassLoader());
		assertTrue(!jars.isEmpty());
	}
}
