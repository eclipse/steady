package com.sap.psr.vulas.vcs;

import com.sap.psr.vulas.shared.util.FileUtil;
import java.io.File;

/** Represents the change of a file that is managed by a version control system. */
public class FileChange {
  public enum Type {
    DEL,
    MOD,
    ADD
  };

  private File oldFile = null, newFile = null;

  /** The repo. */
  private String repo = null;

  /** The file path in the source code repository, relative to the repo's root. */
  private String repoPath = null;

  /**
   * Constructor for FileChange.
   *
   * @param _repo a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param _o a {@link java.io.File} object.
   * @param _n a {@link java.io.File} object.
   */
  public FileChange(String _repo, String _path, File _o, File _n) {
    this.repo = _repo;
    this.repoPath = _path;
    this.oldFile = _o;
    this.newFile = _n;
  }

  /**
   * Returns the type of change, either an addition, a modification or a deletion.
   *
   * @return a {@link com.sap.psr.vulas.vcs.FileChange.Type} object.
   */
  public Type getType() {
    if (this.oldFile == null) return Type.ADD;
    else if (this.newFile == null) return Type.DEL;
    else return Type.MOD;
  }

  /**
   * Getter for the field <code>oldFile</code>.
   *
   * @return a {@link java.io.File} object.
   */
  public File getOldFile() {
    return this.oldFile;
  }
  /**
   * Getter for the field <code>newFile</code>.
   *
   * @return a {@link java.io.File} object.
   */
  public File getNewFile() {
    return this.newFile;
  }
  /**
   * Getter for the field <code>repo</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepo() {
    return this.repo;
  }
  /**
   * Getter for the field <code>repoPath</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepoPath() {
    return this.repoPath;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "FileChange [type="
        + this.getType()
        + ", repo="
        + this.repo
        + ", path="
        + this.repoPath
        + ", old="
        + this.oldFile
        + ", new="
        + this.newFile
        + "]";
  }

  /**
   * Returns the file extension of the file concerned by the change.
   *
   * @return a {@link java.lang.String} object.
   * @throws java.lang.IllegalStateException if any.
   */
  public String getFileExtension() throws IllegalStateException {
    if (this.getOldFile() != null) return FileUtil.getFileExtension(this.getOldFile());
    else if (this.getNewFile() != null) return FileUtil.getFileExtension(this.getNewFile());
    else throw new IllegalStateException("Both old and new file are null");
  }
}
