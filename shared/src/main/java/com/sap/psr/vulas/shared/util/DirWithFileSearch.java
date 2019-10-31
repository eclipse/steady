package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import javax.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Searches for all directories containing a file with a given name. */
public class DirWithFileSearch extends AbstractFileSearch {

  private static final Log log = LogFactory.getLog(DirWithFileSearch.class);

  private String filename = null;

  /**
   * Constructor for DirWithFileSearch.
   *
   * @param _filename a {@link java.lang.String} object.
   */
  public DirWithFileSearch(@NotNull String _filename) {
    this.filename = _filename;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult preVisitDirectory(Path _f, BasicFileAttributes attrs) {
    if (_f.toFile().isDirectory() && !this.foundFile(_f)) {
      if (DirUtil.containsFile(_f.toFile(), this.filename)) {
        this.addFile(_f);
      }
    }
    return FileVisitResult.CONTINUE;
  }
}
