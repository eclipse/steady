package com.sap.psr.vulas.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.FileAnalysisException;

public class AarAnalyzer extends JarAnalyzer {

	private static final Log log = LogFactory.getLog(AarAnalyzer.class);
	
	private JarFile aar;
	private JarWriter aarWriter;
	
	private Path tmpDir = null; // To where the AAR is extracted

	
	@Override
	public String[] getSupportedFileExtensions() { return new String[] { "aar" }; }
	
	@Override
	public void analyze(final File _file) throws FileAnalysisException {
		try {
			
			this.aar = new JarFile(_file, false, java.util.zip.ZipFile.OPEN_READ);
			this.aarWriter = new JarWriter(_file.toPath());
			this.url = _file.getAbsolutePath().toString();
			
			try {
				this.tmpDir = java.nio.file.Files.createTempDirectory("aar_analysis_");
			} catch (IOException e) {
				throw new IllegalStateException("Unable to create temp directory", e);
			}
		
			this.aarWriter.extract(this.tmpDir);
			
			
			// TODO: what if no classes.jar
			// TODO: is aar or classes.jar uploaded
			File classesJar = this.tmpDir.resolve("classes.jar").toFile();
			
			this.jar = new JarFile(classesJar, false, java.util.zip.ZipFile.OPEN_READ);
			this.jarWriter = new JarWriter(classesJar.toPath());
			
		} catch (IllegalStateException e) {
			log.error("IllegalStateException when analyzing file [" + _file + "]: " + e.getMessage());
			throw new FileAnalysisException("Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("IOException when analyzing file [" + _file + "]: " + e.getMessage());
			throw new FileAnalysisException("Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
		} catch (Exception e) {
			log.error("Exception when analyzing file [" + _file + "]: " + e.getMessage());
			throw new FileAnalysisException("Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the SHA1 digest of the AAR by computing it on the fly.
	 * @return the SHA1 digest of the AAR
	 */
	@Override
	public synchronized String getSHA1() { return this.aarWriter.getSHA1(); }
	
	public String getFileName() {
		return this.aarWriter.getOriginalJarFileName().toString();
	}
}
