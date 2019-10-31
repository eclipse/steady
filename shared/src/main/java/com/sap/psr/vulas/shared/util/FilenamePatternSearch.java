package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;

/** Searches for all files whose name follows a given pattern. */
public class FilenamePatternSearch extends AbstractFileSearch {

  private Pattern pattern = null;

  /**
   * Constructor for FilenamePatternSearch.
   *
   * @param _regex a {@link java.lang.String} object.
   */
  public FilenamePatternSearch(@NotNull String _regex) {
    this(Pattern.compile(_regex));
  }

  /**
   * Constructor for FilenamePatternSearch.
   *
   * @param _pattern a {@link java.util.regex.Pattern} object.
   */
  public FilenamePatternSearch(@NotNull Pattern _pattern) {
    this.pattern = _pattern;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult visitFile(Path _f, BasicFileAttributes attrs) {
    if (!_f.toFile().isDirectory() && !this.foundFile(_f) && _f.getFileName() != null) {
      final Matcher m = this.pattern.matcher(_f.getFileName().toString());
      if (m.matches()) {
        this.addFile(_f);
      }
    }
    return FileVisitResult.CONTINUE;
  }
}
