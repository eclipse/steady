package com.sap.psr.vulas.cia.model.pypi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** PypiRelease class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiRelease {

  String upload_time;
  String python_version;
  String url;
  String md5_digest;
  String filename;

  /**
   * Getter for the field <code>upload_time</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getUpload_time() {
    return upload_time;
  }
  /**
   * Setter for the field <code>upload_time</code>.
   *
   * @param upload_time a {@link java.lang.String} object.
   */
  public void setUpload_time(String upload_time) {
    this.upload_time = upload_time;
  }
  /**
   * Getter for the field <code>python_version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPython_version() {
    return python_version;
  }
  /**
   * Setter for the field <code>python_version</code>.
   *
   * @param python_version a {@link java.lang.String} object.
   */
  public void setPython_version(String python_version) {
    this.python_version = python_version;
  }
  /**
   * Getter for the field <code>url</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getUrl() {
    return url;
  }
  /**
   * Setter for the field <code>url</code>.
   *
   * @param url a {@link java.lang.String} object.
   */
  public void setUrl(String url) {
    this.url = url;
  }
  /**
   * Getter for the field <code>md5_digest</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMd5_digest() {
    return md5_digest;
  }
  /**
   * Setter for the field <code>md5_digest</code>.
   *
   * @param md5_digest a {@link java.lang.String} object.
   */
  public void setMd5_digest(String md5_digest) {
    this.md5_digest = md5_digest;
  }
  /**
   * Getter for the field <code>filename</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFilename() {
    return filename;
  }
  /**
   * Setter for the field <code>filename</code>.
   *
   * @param filename a {@link java.lang.String} object.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  /**
   * Getter for the field <code>packagetype</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPackagetype() {
    return packagetype;
  }
  /**
   * Setter for the field <code>packagetype</code>.
   *
   * @param packagetype a {@link java.lang.String} object.
   */
  public void setPackagetype(String packagetype) {
    this.packagetype = packagetype;
  }
  /**
   * Getter for the field <code>path</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPath() {
    return path;
  }
  /**
   * Setter for the field <code>path</code>.
   *
   * @param path a {@link java.lang.String} object.
   */
  public void setPath(String path) {
    this.path = path;
  }
  /**
   * Getter for the field <code>size</code>.
   *
   * @return a long.
   */
  public long getSize() {
    return size;
  }
  /**
   * Setter for the field <code>size</code>.
   *
   * @param size a long.
   */
  public void setSize(long size) {
    this.size = size;
  }

  String packagetype;
  String path;
  long size;
}
