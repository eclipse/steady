package com.sap.psr.vulas.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ServiceLoader;

import org.junit.Test;

import com.sap.psr.vulas.DirAnalyzer;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.java.JavaFileAnalyzer2;
import com.sap.psr.vulas.python.PythonFileAnalyzer;

public class FileAnalyzerTest {
	
	/**
	 * Checks whether the well-known FileAnaylyzers can be found.
	 */
	@Test
	public void testPresenceOfWellknownFileAnalyzers() {
		int i = 0;
		final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
		for(FileAnalyzer la: loader) {
			i++;
			System.out.println("File analyzer #" + i + ": " + la.getClass().getName());
		}
		assertTrue(i>=7);
	}
	
	/**
	 * Checks whether some of the well-known FileAnaylyzer are returned by the FileAnalyzerFactory.
	 */
	@Test
	public void testWellknownFileAnalyzers() {
		final File py = new File("./src/test/resources/file.py");
		FileAnalyzer fa = FileAnalyzerFactory.buildFileAnalyzer(py);
		assertTrue(fa instanceof PythonFileAnalyzer);
		
		final File d = new File("./src/test/resources");
		fa = FileAnalyzerFactory.buildFileAnalyzer(d);
		assertTrue(fa instanceof DirAnalyzer);
		
		final File ja = new File("./src/test/resources/file.java");
		fa = FileAnalyzerFactory.buildFileAnalyzer(ja);
		assertTrue(fa instanceof JavaFileAnalyzer2);
	}
}
