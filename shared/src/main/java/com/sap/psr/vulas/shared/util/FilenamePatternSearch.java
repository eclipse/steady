package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

/**
 * Searches for all files whose name follows a given pattern.
 */
public class FilenamePatternSearch extends AbstractFileSearch {

	private Pattern pattern = null;

	/**
	 * <p>Constructor for FilenamePatternSearch.</p>
	 *
	 * @param _regex a {@link java.lang.String} object.
	 */
	public FilenamePatternSearch(@NotNull String _regex) {
		this(Pattern.compile(_regex));
	}

	/**
	 * <p>Constructor for FilenamePatternSearch.</p>
	 *
	 * @param _pattern a {@link java.util.regex.Pattern} object.
	 */
	public FilenamePatternSearch(@NotNull Pattern _pattern) {
		this.pattern = _pattern;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path _f, BasicFileAttributes attrs) {
		Path f = _f.getFileName();
		if(!_f.toFile().isDirectory() && !this.foundFile(_f) && f!=null) {
			final Matcher m = this.pattern.matcher(f.toString());
			if(m.matches()) {
				this.addFile(_f);
			}
		}
		return FileVisitResult.CONTINUE;
	}
}
