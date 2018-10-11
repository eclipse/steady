package com.sap.psr.vulas.vcs;

import java.io.File;

import com.sap.psr.vulas.shared.util.FileUtil;

/**
 * Represents the change of a file that is managed by a version control system.
 *
 */
public class FileChange {
	public enum Type { DEL, MOD, ADD };
	private File oldFile = null, newFile = null;
	
	/**
	 * The repo.
	 */
	private String repo = null;
	
	/**
	 * The file path in the source code repository, relative to the repo's root.
	 */
	private String repoPath = null;
		
	public FileChange(String _repo, String _path, File _o, File _n) {
		this.repo     = _repo;
		this.repoPath = _path;
		this.oldFile  = _o;
		this.newFile  = _n;
	}
	
	/**
	 * Returns the type of change, either an addition, a modification or a deletion.
	 * @return
	 */
	public Type getType() {
		if(this.oldFile == null) return Type.ADD;
		else if(this.newFile == null) return Type.DEL;
		else return Type.MOD;
	}
	
	public File getOldFile() {
		return this.oldFile;
	}
	public File getNewFile() {
		return this.newFile;
	}
	public String getRepo() {
		return this.repo;
	}
	public String getRepoPath() {
		return this.repoPath;
	}
	
	@Override
	public String toString() {
		return "FileChange [type=" + this.getType() + ", repo=" + this.repo + ", path=" + this.repoPath + ", old=" + this.oldFile + ", new=" + this.newFile + "]";
	}
	
	/**
	 * Returns the file extension of the file concerned by the change.
	 * @return
	 */
	public String getFileExtension() throws IllegalStateException {
		if(this.getOldFile()!=null)
			return FileUtil.getFileExtension(this.getOldFile());
		else if(this.getNewFile()!=null)
			return FileUtil.getFileExtension(this.getNewFile());
		else
			throw new IllegalStateException("Both old and new file are null");
	}
}
