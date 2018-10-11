package com.sap.psr.vulas.java;

import java.io.InputStream;
import java.util.jar.JarEntry;

/*
 * Used in conjunction with a {@link JarWriter}. Instances of JarEntryWriter
 * can be registered at a {@link JarWriter} and will be called in case
 * the {@link JarWriter} comes across an entry matching the given regular expression.
 */
public interface JarEntryWriter {

	/**
	 * Callback used for rewriting particular JAR entries. Return null to rewrite the original JAR entry.
	 * @param _entry
	 * @return
	 */
	public InputStream getInputStream(String _regex, JarEntry _entry);
}
