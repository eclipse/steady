package com.sap.psr.vulas.java;

import java.io.InputStream;
import java.util.jar.JarEntry;

/*
 * Used in conjunction with a {@link JarWriter}. Instances of JarEntryWriter
 * can be registered at a {@link JarWriter} and will be called in case
 * the {@link JarWriter} comes across an entry matching the given regular expression.
 */
/** JarEntryWriter interface. */
public interface JarEntryWriter {

  /**
   * Callback used for rewriting particular JAR entries. Return null to rewrite the original JAR
   * entry.
   *
   * @param _entry a {@link java.util.jar.JarEntry} object.
   * @param _regex a {@link java.lang.String} object.
   * @return a {@link java.io.InputStream} object.
   */
  public InputStream getInputStream(String _regex, JarEntry _entry);
}
