package com.sap.psr.vulas.cia.model.pypi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** PypiInfo class. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiInfo {

  String maintainer_email;

  String package_url;

  String author;

  String author_email;

  String download_url;

  String version;

  String release_url;

  List<String> classifiers;

  String name;

  String bugtrack_url;

  String license;

  String summary;

  String home_page;

  /**
   * Getter for the field <code>maintainer_email</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMaintainer_email() {
    return maintainer_email;
  }

  /**
   * Setter for the field <code>maintainer_email</code>.
   *
   * @param maintainer_email a {@link java.lang.String} object.
   */
  public void setMaintainer_email(String maintainer_email) {
    this.maintainer_email = maintainer_email;
  }

  /**
   * Getter for the field <code>package_url</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPackage_url() {
    return package_url;
  }

  /**
   * Setter for the field <code>package_url</code>.
   *
   * @param package_url a {@link java.lang.String} object.
   */
  public void setPackage_url(String package_url) {
    this.package_url = package_url;
  }

  /**
   * Getter for the field <code>author</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Setter for the field <code>author</code>.
   *
   * @param author a {@link java.lang.String} object.
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Getter for the field <code>author_email</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAuthor_email() {
    return author_email;
  }

  /**
   * Setter for the field <code>author_email</code>.
   *
   * @param author_email a {@link java.lang.String} object.
   */
  public void setAuthor_email(String author_email) {
    this.author_email = author_email;
  }

  /**
   * Getter for the field <code>download_url</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDownload_url() {
    return download_url;
  }

  /**
   * Setter for the field <code>download_url</code>.
   *
   * @param download_url a {@link java.lang.String} object.
   */
  public void setDownload_url(String download_url) {
    this.download_url = download_url;
  }

  /**
   * Getter for the field <code>version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Setter for the field <code>version</code>.
   *
   * @param version a {@link java.lang.String} object.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Getter for the field <code>release_url</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRelease_url() {
    return release_url;
  }

  /**
   * Setter for the field <code>release_url</code>.
   *
   * @param release_url a {@link java.lang.String} object.
   */
  public void setRelease_url(String release_url) {
    this.release_url = release_url;
  }

  /**
   * Getter for the field <code>classifiers</code>.
   *
   * @return a {@link java.util.List} object.
   */
  public List<String> getClassifiers() {
    return classifiers;
  }

  /**
   * Setter for the field <code>classifiers</code>.
   *
   * @param classifiers a {@link java.util.List} object.
   */
  public void setClassifiers(List<String> classifiers) {
    this.classifiers = classifiers;
  }

  /**
   * Getter for the field <code>name</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for the field <code>name</code>.
   *
   * @param name a {@link java.lang.String} object.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter for the field <code>bugtrack_url</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugtrack_url() {
    return bugtrack_url;
  }

  /**
   * Setter for the field <code>bugtrack_url</code>.
   *
   * @param bugtrack_url a {@link java.lang.String} object.
   */
  public void setBugtrack_url(String bugtrack_url) {
    this.bugtrack_url = bugtrack_url;
  }

  /**
   * Getter for the field <code>license</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getLicense() {
    return license;
  }

  /**
   * Setter for the field <code>license</code>.
   *
   * @param license a {@link java.lang.String} object.
   */
  public void setLicense(String license) {
    this.license = license;
  }

  /**
   * Getter for the field <code>summary</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Setter for the field <code>summary</code>.
   *
   * @param summary a {@link java.lang.String} object.
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Getter for the field <code>home_page</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getHome_page() {
    return home_page;
  }

  /**
   * Setter for the field <code>home_page</code>.
   *
   * @param home_page a {@link java.lang.String} object.
   */
  public void setHome_page(String home_page) {
    this.home_page = home_page;
  }
}
