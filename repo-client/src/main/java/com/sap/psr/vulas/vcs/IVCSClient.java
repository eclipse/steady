package com.sap.psr.vulas.vcs;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Client of a versioning control system (VCS), e.g., SVN or GIT.
 *
 *
 */
public interface IVCSClient {

	public void setRepoUrl(URL _url) throws RepoMismatchException;

	public void setWorkDir(Path _dir);

	/**
	 * Identifies the VCS type(s) covered by the respective implementation (e.g., GIT).
	 *
	 * @param search string
	 * @return
	 */
	public String getType();

	/**
	 * Returns revisions whose commit message contains the provided search
	 * string.
	 *
	 * @param search string
	 * @return
	 */
	public Map<String, String> searchCommitLog(String _str, Date _asOf);

	/**
	 * Returns revisions with given IDs (if any).
	 *
	 * @param search string
	 * @return
	 */
	public Map<String, String> getCommitLogEntries(Set<String> _revs); //String[] _str);

	/**
	 * Performs a checkout.
	 *
	 * @param _rev
	 * @param _dir
	 * @throws Exception 
	 */
	public Path checkout(String _rev) throws Exception;

	/**
	 * Returns a set of files for a given revision and type of change (Added,
	 * Modified, Deleted)
	 *
	 * @param _rev
	 * @param typeOfChange
	 * @return
	 */
	public Set<FileChange> getFileChanges(String _rev);

	/**
     * Returns a path relative to the repo's root for a given absolute path.
     * E.g., /commons/proper/fileupload/tags for http://svn.apache.org/asf/repos/commons/proper/fileupload/tags
     * @return
     */
    public String getRepoRelativePath();

	/**
	 * Delivers all entries of a gtiven directory (including the respective revisions).
	 * @param path
	 * @return
	 */
	public Map<String, String> listEntries(String path, String _asof, String _until);

    /**
     *  Performs a checkout of a whole directory
     *  Returns a single specific file which is in the checked-out directory
     * @param _rev
     * @param _rel_path
     * @return
     */
    public File checkoutFile(String _rev, String _rel_path);

	/**
	 * Deletes temp. files created in the course of the analysis.
	 */
	public void cleanup();

	/**
	 * Retrieve the time stamp for a commit/revision
	 * @param revision
	 * @return time stamp for a commit/revision
	 */
	public long  getRevisionTimeStamp(String revision);
}
