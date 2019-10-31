package com.sap.psr.vulas.monitor.trace;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.monitor.Loader;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.StringUtil;
import java.util.HashSet;
import java.util.Set;

/** Represents the usage of a given construct. */
public class ConstructUsage {
  private long t = -1;
  private ConstructId c = null;
  private int counter = 1;
  /**
   * The URL of the resource from where the construct was loaded, can be the URL of a class file or
   * a JAR.
   */
  private String resourceURL = null;

  private String archiveDigest = null;
  private String archiveFileName = null;
  private Loader loader = null;
  private Application appContext = null;
  private String executionId = null;
  private Set<ConstructId> junitContexts = new HashSet<ConstructId>();
  /**
   * Constructor for ConstructUsage.
   *
   * @param _c a {@link com.sap.psr.vulas.ConstructId} object.
   * @param _resource_url a {@link java.lang.String} object.
   * @param _t a long.
   */
  public ConstructUsage(ConstructId _c, String _resource_url, long _t) {
    this(_c, _resource_url, null, _t, 1);
  }
  /**
   * Constructor for ConstructUsage.
   *
   * @param _c a {@link com.sap.psr.vulas.ConstructId} object.
   * @param _resource_url a {@link java.lang.String} object.
   * @param _loader a {@link com.sap.psr.vulas.monitor.Loader} object.
   * @param _t a long.
   * @param _counter a int.
   */
  public ConstructUsage(
      ConstructId _c, String _resource_url, Loader _loader, long _t, int _counter) {
    this.c = _c;
    this.resourceURL = _resource_url;
    this.loader = _loader;
    this.t = _t;
    this.counter = _counter;
  }
  /**
   * addJUnitContext.
   *
   * @param _junit a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public void addJUnitContext(ConstructId _junit) {
    this.junitContexts.add(_junit);
  }
  /**
   * getJUnitContexts.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructId> getJUnitContexts() {
    return this.junitContexts;
  }
  /**
   * Getter for the field <code>counter</code>.
   *
   * @return a int.
   */
  public int getCounter() {
    return this.counter;
  }
  /**
   * Getter for the field <code>appContext</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public Application getAppContext() {
    return this.appContext;
  }
  /**
   * Setter for the field <code>appContext</code>.
   *
   * @param _ctx a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public void setAppContext(Application _ctx) {
    this.appContext = _ctx;
  }
  /**
   * Getter for the field <code>resourceURL</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getResourceURL() {
    return this.resourceURL;
  }
  /**
   * Getter for the field <code>archiveDigest</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArchiveDigest() {
    return archiveDigest;
  }
  /**
   * Setter for the field <code>archiveDigest</code>.
   *
   * @param resourceDigest a {@link java.lang.String} object.
   */
  public void setArchiveDigest(String resourceDigest) {
    this.archiveDigest = resourceDigest;
  }
  /**
   * Getter for the field <code>archiveFileName</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArchiveFileName() {
    return archiveFileName;
  }
  /**
   * Setter for the field <code>archiveFileName</code>.
   *
   * @param archiveFileName a {@link java.lang.String} object.
   */
  public void setArchiveFileName(String archiveFileName) {
    this.archiveFileName = archiveFileName;
  }
  /**
   * Getter for the field <code>loader</code>.
   *
   * @return a {@link com.sap.psr.vulas.monitor.Loader} object.
   */
  public Loader getLoader() {
    return this.loader;
  }
  /**
   * Setter for the field <code>executionId</code>.
   *
   * @param _id a {@link java.lang.String} object.
   */
  public void setExecutionId(String _id) {
    this.executionId = _id;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append("ConstructUsage [timestamp=")
        .append(StringUtil.formatDate(this.t))
        .append(", count=")
        .append(counter);
    if (this.archiveFileName != null) b.append(", archiveFileName=").append(this.archiveFileName);
    if (this.archiveDigest != null) b.append(", archiveDigest=").append(this.archiveDigest);
    // if(this.resourceURL!=null) b.append(", resourceURL=").append(this.resourceURL);
    b.append(", construct=").append(c.toString()).append("]");
    return b.toString();
  }
  /**
   * toJSON.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toJSON() {
    final JsonBuilder jb = new JsonBuilder();
    jb.startObject();
    jb.appendObjectProperty("tracedAt", StringUtil.formatDate(this.t));
    jb.appendObjectProperty("count", new Integer(this.counter));
    jb.appendObjectProperty("executionId", this.executionId);
    if (this.appContext != null)
      jb.appendObjectProperty("app", JacksonUtil.asJsonString(this.appContext), false);
    if (this.archiveFileName != null && this.archiveDigest != null) {
      jb.appendObjectProperty("lib", this.archiveDigest);
      jb.appendObjectProperty("filename", this.archiveFileName);
    }
    if (this.junitContexts != null && this.junitContexts.size() > 0) {
      jb.startArrayProperty("junits");
      for (ConstructId junit : this.junitContexts) jb.appendJsonToArray(junit.toJSON());
      jb.endArray();
    }
    // if(this.loader!=null) b.append(", \"loader\" : ").append(this.loader.toJSON()).append("");
    jb.appendObjectProperty("constructId", c.toJSON(), false);
    jb.endObject();
    return jb.toString();
  }
  /**
   * merge.
   *
   * @param _other a {@link com.sap.psr.vulas.monitor.trace.ConstructUsage} object.
   */
  public void merge(ConstructUsage _other) {
    this.counter = Math.max(this.counter, _other.getCounter());
    this.junitContexts.addAll(_other.getJUnitContexts());
  }
  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((appContext == null) ? 0 : appContext.hashCode());
    result = prime * result + ((c == null) ? 0 : c.hashCode());
    result = prime * result + ((resourceURL == null) ? 0 : resourceURL.hashCode());
    return result;
  }
  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructUsage other = (ConstructUsage) obj;
    if (appContext == null) {
      if (other.appContext != null) return false;
    } else if (!appContext.equals(other.appContext)) return false;
    if (c == null) {
      if (other.c != null) return false;
    } else if (!c.equals(other.c)) return false;
    if (resourceURL == null) {
      if (other.resourceURL != null) return false;
    } else if (!resourceURL.equals(other.resourceURL)) return false;
    return true;
  }
}
