/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Searches for all files having a given extension below one or multiple directories.
 */
public class FileSearch extends AbstractFileSearch {

	private static final Log log = LogFactory.getLog(FileSearch.class);

	private String[] suffixes = null;
	
	private int maxSize = -1;

	/**
	 * <p>Constructor for FileSearch.</p>
	 *
	 * @param _s an array with accepted file extensions
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public FileSearch(String[] _s) throws IllegalArgumentException { this(_s, -1); }
	
	/**
	 * <p>Constructor for FileSearch.</p>
	 *
	 * @param _s an array with accepted file extensions
	 * @param _size maximum accepted file size (no limit, if a value <= 0 is provided)
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public FileSearch(String[] _s, int _size) throws IllegalArgumentException {
		if(_s==null || _s.length==0)
			throw new IllegalArgumentException("At least one file extension must be provided");
		this.suffixes = _s.clone();
		if(_size > 0)
			this.maxSize = _size;
	}
	
	/**
	 * <p>Getter for the field <code>suffixes</code>.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getSuffixes() { return this.suffixes.clone(); }

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path _f, BasicFileAttributes attrs) {
		if(!this.foundFile(_f) && FileUtil.hasFileExtension(_f, this.suffixes)) {
			if(this.maxSize==-1) {
				this.addFile(_f);
			}
			else {
				if(_f.toFile().length() < this.maxSize) {
					this.addFile(_f);
				} else {
					log.warn("File [" + _f.toAbsolutePath() + "] ignored because it exceeds the maximum accepted size [" + _f.toFile().length() + " > " + this.maxSize + "] bytes");
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}
}
