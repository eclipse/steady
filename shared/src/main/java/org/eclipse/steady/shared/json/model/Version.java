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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.shared.json.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

/**
 * This class represents software versions as major.minor[.maintenance[.build]].
 * See https://en.wikipedia.org/wiki/Software_versioning for more information.
 */
public class Version implements Comparable<Version> {

  private static Logger log = org.apache.logging.log4j.LogManager.getLogger(Version.class);

  static final Pattern VERSION_PATTERN = Pattern.compile("([\\d\\.]*)(.*)", Pattern.DOTALL);

  private String version;

  /**
   * <p>Constructor for Version.</p>
   *
   * @param _v a {@link java.lang.String} object.
   */
  public Version(String _v) {
    this.version = _v;
  }

  /**
   * <p>Getter for the field <code>version</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * This method returns the major release of the library, i.e., the string before the first '.', if any; 0 otherwise
   *
   * @return the string (usually a digit) before the first '.' if any; 0 otherwise
   */
  public String getMajorRelease() {
    String[] versions = this.version.split("\\.");
    return (versions.length > 0) ? versions[0] : "0";
  }

  /**
   * This method returns the minor release, i.e., major.minor if minor exists; major.0 otherwise
   *
   * @return major.minor if minor exists; major.0 otherwise
   */
  public String getMinorRelease() {
    String[] versions = this.version.split("\\.");
    String majorRelease = (versions.length > 0) ? versions[0] : "0";
    // check whether the minor starts with numbers: if it contains also letter just consider the
    // numbers

    if (versions.length > 1) {
      Pattern p = Pattern.compile("^[0-9]*");
      Matcher m = p.matcher(versions[1]);
      if (m.find()) {
        return majorRelease.concat(".".concat(m.group()));
      }
    }
    return majorRelease.concat(".0");
  }

  /**
   * This method returns the maintenance release, i.e., major.minor.maintenance, where minor and maintenance are set to 0 if they do not exist in this.version
   *
   * @return major.minor.maintenance
   */
  public String getMaintenanceRelease() {
    String[] versions = this.version.split("\\.");
    String majorRelease = (versions.length > 0) ? versions[0] : "0";
    String minorRelease =
        majorRelease.concat(".".concat((versions.length > 1) ? versions[1] : "0"));
    return minorRelease.concat(".".concat((versions.length > 2) ? versions[2] : "0"));
  }

  /**
   * This method returns true if the Version is a maintenance release, i.e., only has 3 fields (major.minor.maintenance) or the fourth is 0 or"RELEASE" (major.minor.maintenance.0||RELEASE)
   *
   * @return major.minor.maintenance
   */
  public Boolean isMaintenanceRelease() {
    String[] versions = this.version.split("\\.");
    if (versions.length == 3
        || (versions.length > 3 && (versions[3].equals("0") || versions[3].equals("RELEASE"))))
      return true;
    return false;
  }

  /**
   * This method compares versions that only contains numbers. It returns 0 if they are equal or contains letters
   *
   * @return a int.
   * @param other a {@link org.eclipse.steady.shared.json.model.Version} object
   */
  public int compareNumberVersion(Version other) {

    Matcher this_m = VERSION_PATTERN.matcher(this.getVersion());
    Matcher other_m = VERSION_PATTERN.matcher(other.getVersion());

    // Extract the numeric part of the version
    String this_v = (this_m.matches() ? this_m.group(1) : this.getVersion());
    String other_v = (other_m.matches() ? other_m.group(1) : other.getVersion());

    String[] varray = this_v.split("\\.");
    String[] otherVersions = other_v.split("\\.");
    for (int v = 0; v < varray.length && v < otherVersions.length; v++) {
      if (!varray[v].equals(otherVersions[v])) {
        // check that it only contains numbers
        Pattern p = Pattern.compile("^[0-9]*$");
        Matcher m = p.matcher(varray[v]);
        Matcher m1 = p.matcher(otherVersions[v]);
        // we proceed if the version matches the pattern with some non-empty groups (i.e., the
        // version contains numbers at all)
        if (m.matches() && m.start() > 0 && m1.matches() && m1.start() > 0) {
          if (Integer.parseInt(varray[v]) == Integer.parseInt(otherVersions[v])) break;
          return (Integer.parseInt(varray[v]) > Integer.parseInt(otherVersions[v])) ? 1 : -1;
        }
      }
    }
    return 0;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.version;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Version other = (Version) obj;
    if (version == null) {
      if (other.version != null) return false;
    } else if (!version.equals(other.version)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Version o) {

    Matcher this_m = VERSION_PATTERN.matcher(this.getVersion());
    Matcher other_m = VERSION_PATTERN.matcher(o.getVersion());

    // Extract the numeric part of the version
    String this_v = (this_m.matches() ? this_m.group(1) : this.getVersion());
    String other_v = (other_m.matches() ? other_m.group(1) : o.getVersion());

    Version.log.debug(
        "Versions to compare: ["
            + this.getVersion()
            + ", "
            + o.getVersion()
            + "], remove suffix for comparison ["
            + this_v
            + ", "
            + other_v
            + "]");

    // compare the numeric part for each block separated by .
    String[] varray = this_v.split("\\.");
    String[] otherVersions = other_v.split("\\.");
    for (int v = 0; v < varray.length && v < otherVersions.length; v++) {
      if (!varray[v].equals(otherVersions[v])) {
        // check that it only contains numbers
        // this check should always be true and could now be simplified
        Pattern p = Pattern.compile("^[0-9]*$");
        Matcher m = p.matcher(varray[v]);
        Matcher m1 = p.matcher(otherVersions[v]);
        if (m.matches() && m1.matches()) {
          try {
            return (Integer.parseInt(varray[v]) > Integer.parseInt(otherVersions[v])) ? 1 : -1;
          } catch (NumberFormatException e) {
            log.warn(
                "Cannot compare version ["
                    + varray[v]
                    + "] against version "
                    + otherVersions[v]
                    + ": no number");
          }
        }
      }
    }

    if (varray.length > otherVersions.length) {
      log.debug(this.version + " is greater than " + o.getVersion());
      return 1;
    } else if (varray.length < otherVersions.length) {
      log.debug(this.version + " is smaller than " + o.getVersion());
      return -1;
    }

    // If we arrive here, it means that the numeric part is equal
    String this_v_tag = (this_m.matches() ? this_m.group(2) : this.getVersion());
    String other_v_tag = (other_m.matches() ? other_m.group(2) : o.getVersion());

    if (this_v_tag.equals("") && !other_v_tag.equals("")) {
      log.debug(this.version + " is greater than " + o.getVersion());
      return 1;
    } else if (!this_v_tag.equals("") && other_v_tag.equals("")) {
      log.debug(this.version + " is smaller than " + o.getVersion());
      return -1;
    } else {
      Version.log.debug(
          "Compare artifact versions: Original ["
              + this.getVersion()
              + ", "
              + o.getVersion()
              + "], modified for comparison based on tag (if any)["
              + this_v_tag
              + ", "
              + other_v_tag
              + "] returns : "
              + this_v_tag.compareToIgnoreCase(other_v_tag));
      // comparison in order to obtain X.X > X.X-RC > X.X-beta
      return this_v_tag.compareToIgnoreCase(other_v_tag);
    }
  }
}
