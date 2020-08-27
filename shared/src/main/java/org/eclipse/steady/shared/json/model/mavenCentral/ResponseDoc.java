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
package org.eclipse.steady.shared.json.model.mavenCentral;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.LibraryId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 * This class is used to de-serialize requests from Maven central and to represent artifacts to be
 * downloaded from Maven central.
 */
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"c"})
public class ResponseDoc implements Comparable {

  static final Pattern VERSION_PATTERN = Pattern.compile("([\\d\\.]*)(.*)", Pattern.DOTALL);

  private static Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private String id;

  private String g;

  private String a;

  private String v;

  private String c;

  private String p;

  private long timestamp;

  private Collection<String> ec = null;

  private Collection<String> tags = null;

  /**
   * <p>Constructor for ResponseDoc.</p>
   */
  public ResponseDoc() {}

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return id;
  }
  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.String} object.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>g</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getG() {
    return g;
  }
  /**
   * <p>Setter for the field <code>g</code>.</p>
   *
   * @param g a {@link java.lang.String} object.
   */
  public void setG(String g) {
    this.g = g;
  }

  /**
   * <p>Getter for the field <code>a</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getA() {
    return a;
  }
  /**
   * <p>Setter for the field <code>a</code>.</p>
   *
   * @param a a {@link java.lang.String} object.
   */
  public void setA(String a) {
    this.a = a;
  }

  /**
   * <p>Getter for the field <code>v</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getV() {
    return v;
  }
  /**
   * <p>Setter for the field <code>v</code>.</p>
   *
   * @param v a {@link java.lang.String} object.
   */
  public void setV(String v) {
    this.v = v;
  }

  /**
   * <p>Getter for the field <code>c</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getC() {
    return c;
  }
  /**
   * <p>Setter for the field <code>c</code>.</p>
   *
   * @param c a {@link java.lang.String} object.
   */
  public void setC(String c) {
    // TODO check that c is among ec
    this.c = c;
  }

  /**
   * <p>Getter for the field <code>p</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getP() {
    return p;
  }
  /**
   * <p>Setter for the field <code>p</code>.</p>
   *
   * @param p a {@link java.lang.String} object.
   */
  public void setP(String p) {
    this.p = p;
  }

  /**
   * <p>Getter for the field <code>timestamp</code>.</p>
   *
   * @return a long.
   */
  public long getTimestamp() {
    return timestamp;
  }
  /**
   * <p>Setter for the field <code>timestamp</code>.</p>
   *
   * @param timestamp a long.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * <p>Getter for the field <code>ec</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<String> getEc() {
    return ec;
  }
  /**
   * <p>Setter for the field <code>ec</code>.</p>
   *
   * @param ec a {@link java.util.Collection} object.
   */
  public void setEc(Collection<String> ec) {
    this.ec = ec;
  }

  /**
   * <p>Getter for the field <code>tags</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<String> getTags() {
    return tags;
  }

  /**
   * <p>Setter for the field <code>tags</code>.</p>
   *
   * @param tags a {@link java.util.Collection} object.
   */
  public void setTags(Collection<String> tags) {
    this.tags = tags;
  }

  /**
   * <p>availableWith.</p>
   *
   * @param _classifier a {@link java.lang.String} object.
   * @param _packaging a {@link java.lang.String} object.
   * @return a boolean.
   */
  @JsonIgnore
  public boolean availableWith(String _classifier, String _packaging) {
    // final String filter_ec = (_classifier!=null && !_classifier.equals("") ? _classifier + "-" :
    // "") + "." + _packaging;
    for (String ec : this.getEc()) {

      if (_classifier != null && _packaging != null) {
        if (ec.equals("-" + _classifier + "." + _packaging)) return true;
      } else if (_classifier == null && _packaging != null) {
        if (ec.endsWith("." + _packaging)) return true;
      } else if (_classifier != null && _packaging == null) {
        if (ec.startsWith("-" + _classifier)) return true;
      } else return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * Compares the respective timestamps.
   */
  @Override
  public int compareTo(Object other) {
    if (other instanceof ResponseDoc) {
      final ResponseDoc other_doc = (ResponseDoc) other;
      int c = Long.compare(this.timestamp, other_doc.getTimestamp());
      if (c == 0) {
        // c = this.v.compareTo(other_doc.v);

        Matcher this_m = VERSION_PATTERN.matcher(this.v);
        Matcher other_m = VERSION_PATTERN.matcher(other_doc.v);

        String this_v = (this_m.matches() ? this_m.group(1) : this.v);
        String other_v = (other_m.matches() ? other_m.group(1) : other_doc.v);

        ResponseDoc.log.debug(
            "Compare artifact versions: Original ["
                + this.v
                + ", "
                + other_doc.v
                + "], modified for comparison ["
                + this_v
                + ", "
                + other_v
                + "]");
        c = this_v.compareTo(other_v);

        if (c == 0) {
          String this_v_tag = (this_m.matches() ? this_m.group(2) : this.v);
          String other_v_tag = (other_m.matches() ? other_m.group(2) : other_doc.v);

          if (this_v_tag.equals("") && !other_v_tag.equals("")) return 1;
          else if (!this_v_tag.equals("") && other_v_tag.equals("")) return -1;
          else {
            ResponseDoc.log.debug(
                "Compare artifact versions: Original ["
                    + this.v
                    + ", "
                    + other_doc.v
                    + "], modified for comparison based on tag (if any)["
                    + this_v_tag
                    + ", "
                    + other_v_tag
                    + "]");
            // comparison in order to obtain X.X > X.X-RC > X.X-beta
            c = this_v_tag.compareToIgnoreCase(other_v_tag);
          }
        }
      }
      return c;
    } else {
      throw new IllegalArgumentException(
          "Expected ResponseDoc, got [" + other.getClass().getName() + "]");
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append("[").append(id);
    if (this.c != null && !this.c.equals("")) b.append(":").append(this.getC());
    b.append(":").append(p).append("]");
    return b.toString();
  }

  //	/**
  //	 * Returns the path of the directory where the artifact is stored, relative to the local M2
  // repository.
  //	 * E.g, com/jolira/guice/3.0.0.
  //	 * @return
  //	 */
  //	@JsonIgnore
  //	private Path getRelM2Dir() {
  //		final StringBuilder b = new StringBuilder();
  //		b.append(this.getG().replace('.',  '/')).append("/");
  //		b.append(this.getA()).append("/");
  //		b.append(this.getV());
  //		return Paths.get(b.toString());
  //	}
  //
  //	/**
  //     * Returns the artifact's filename root, e.g., guice-3.0.0
  //     * To be completed with one of the available postfix in this.ec
  //	 * @return
  //	 */
  //	@JsonIgnore
  //	public String getM2Filename() {
  //		final StringBuilder b = new StringBuilder();
  //		b.append(this.getA()).append("-").append(this.getV());
  //		if(this.c!=null && !this.c.equals(""))
  //			b.append("-").append(this.getC());
  //		b.append(".").append(this.getP());
  //		return b.toString();
  //	}

  //	/**
  //	 * http://search.maven.org/remotecontent?filepath=com/jolira/guice/3.0.0/guice-3.0.0.pom
  //	 * @return
  //	 */
  //	@JsonIgnore
  //	public Path getRelM2Path() {
  //		return Paths.get(this.getRelM2Dir().toString(), this.getM2Filename());
  //	}

  /**
   * Returns a {@link LibraryId} corresponding to this {@link ResponseDoc}.
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   */
  public Artifact toArtifact() {
    Artifact r = new Artifact(g, a, v);
    r.setTimestamp(timestamp);
    r.setProgrammingLanguage(ProgrammingLanguage.JAVA);
    return r;
  }
}
