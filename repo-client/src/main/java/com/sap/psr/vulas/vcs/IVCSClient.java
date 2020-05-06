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
package com.sap.psr.vulas.vcs;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import com.sap.psr.vulas.shared.json.model.FileChange;

/**
 * Client of a versioning control system (VCS), e.g., SVN or GIT.
 */
public interface IVCSClient {

	/**
	 * <p>setRepoUrl.</p>
	 *
	 * @param _url a {@link java.net.URL} object.
	 * @throws com.sap.psr.vulas.vcs.RepoMismatchException if any.
	 */
	public void setRepoUrl(URL _url) throws RepoMismatchException;

	/**
	 * <p>setWorkDir.</p>
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 */
	public void setWorkDir(Path _dir);

	/**
	 * Identifies the VCS type(s) covered by the respective implementation (e.g., GIT).
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType();

	/**
	 * Returns revisions whose commit message contains the provided search
	 * string.
	 *
	 * @param _str a {@link java.lang.String} object.
	 * @param _asOf a {@link java.util.Date} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> searchCommitLog(String _str, Date _asOf);

	/**
	 * Returns revisions with given IDs (if any).
	 *
	 * @param _revs a {@link java.util.Set} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getCommitLogEntries(Set<String> _revs); //String[] _str);

	/**
	 * Performs a checkout.
	 *
	 * @param _rev a {@link java.lang.String} object.
	 * @throws java.lang.Exception
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path checkout(String _rev) throws Exception;

	/**
	 * Returns a set of files for a given revision and type of change (Added,
	 * Modified, Deleted)
	 *
	 * @param _rev a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<FileChange> getFileChanges(String _rev);

    /**
     * Returns a path relative to the repo's root for a given absolute path.
     * E.g., /commons/proper/fileupload/tags for http://svn.apache.org/asf/repos/commons/proper/fileupload/tags
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepoRelativePath();

	/**
	 * Delivers all entries of a gtiven directory (including the respective revisions).
	 *
	 * @param path a {@link java.lang.String} object.
	 * @param _asof a {@link java.lang.String} object.
	 * @param _until a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> listEntries(String path, String _asof, String _until);

    /**
     *  Performs a checkout of a whole directory
     *  Returns a single specific file which is in the checked-out directory
     *
     * @param _rev a {@link java.lang.String} object.
     * @param _rel_path a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public File checkoutFile(String _rev, String _rel_path);

	/**
	 * Deletes temp. files created in the course of the analysis.
	 */
	public void cleanup();

	/**
	 * Retrieve the time stamp for a commit/revision
	 *
	 * @param revision a {@link java.lang.String} object.
	 * @return time stamp for a commit/revision
	 */
	public long  getRevisionTimeStamp(String revision);
}
