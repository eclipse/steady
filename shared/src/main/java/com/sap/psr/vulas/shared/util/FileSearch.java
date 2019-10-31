package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Searches for all files having a given extension below one or multiple directories. */
public class FileSearch extends AbstractFileSearch {

  private static final Log log = LogFactory.getLog(FileSearch.class);

  private String[] suffixes = null;

  /**
   * Constructor for FileSearch.
   *
   * @param _s an array of {@link java.lang.String} objects.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public FileSearch(String[] _s) throws IllegalArgumentException {
    if (_s == null || _s.length == 0)
      throw new IllegalArgumentException("At least one file extension must be provided");
    this.suffixes = _s.clone();
  }

  /**
   * Getter for the field <code>suffixes</code>.
   *
   * @return an array of {@link java.lang.String} objects.
   */
  public String[] getSuffixes() {
    return this.suffixes.clone();
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult visitFile(Path _f, BasicFileAttributes attrs) {
    if (!this.foundFile(_f) && FileUtil.hasFileExtension(_f, this.suffixes)) this.addFile(_f);
    return FileVisitResult.CONTINUE;
  }
}
