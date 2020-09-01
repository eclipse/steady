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
package org.eclipse.steady.shared.json.model;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.enums.AffectedVersionSource;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

/**
 * Exemptions are used to prevent that {@link VulnerableDependency}s result in build exceptions during the execution of the report goal.
 *
 * Exemptions can be created for bug identifiers and libraries (by specifying their digests) using the following format:
 *
 * vulas.report.exemptBug.&lt;vuln-id&gt;.reason = &lt;reason&gt;
 * vulas.report.exemptBug.&lt;vuln-id&gt;.libraries = [ * | &lt;digest&gt; | &lt;package-url&gt; ] [, &lt;digest&gt; | &lt;package-url&gt; ]
 *
 * The wildcard * can be used to indicate that a bug is exempted for all libraries (no matter the digest or package URL).
 */
public class ExemptionBug implements IExemption {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final String ALL = "*";

  private static final String PURL_PREFIX = "pkg:";

  /** Deprecated configuration prefix. **/
  public static final String DEPRECATED_CFG_PREFIX = "vulas.report.exceptionExcludeBugs";

  /** Deprecated configuration key in backend (where the vulas prefix was removed before 3.1.12). **/
  public static final String DEPRECATED_KEY_BACKEND = "report.exceptionExcludeBugs";

  /** New configuration prefix (as of 3.1.12). **/
  public static final String CFG_PREFIX = "vulas.report.exemptBug";

  /** The identifier of a bug, or star (*), which means that the exemption applies to all bugs. */
  private String bugId = null;

  /** The digest of a library or star (*), which means that the exemption applies to all libraries. */
  private String library = null;

  private String reason = null;

  /**
   * Creates a new exemption, whereby parameters equal to null will be interpreted as star (*).
   *
   * @param _bug_id
   * @param _library
   * @param _reason
   */
  public ExemptionBug(String _bug_id, String _library, String _reason) {
    this.bugId = (_bug_id == null ? ALL : _bug_id);
    this.library = (_library == null ? ALL : _library);
    this.reason = _reason;
  }

  public String getBugId() {
    return bugId;
  }

  public String getLibrary() {
    return library;
  }

  @Override
  public String getReason() {
    return reason;
  }

  @Override
  public boolean isExempted(VulnerableDependency _vd) {
    // Bug ID
    boolean is_exempted =
        ALL.equals(this.bugId) || this.bugId.equalsIgnoreCase(_vd.getBug().getBugId());

    // Library
    if (is_exempted) {
      // All
      if (ALL.equals(this.library)) {
        ;
      }

      // Package URL according to https://github.com/package-url/purl-spec
      else if (this.library.startsWith(PURL_PREFIX)
          && _vd.getDep().getLib().getLibraryId() != null) {
        try {
          final LibraryId libid = _vd.getDep().getLib().getLibraryId();
          final PackageURL purl = ExemptionBug.createPackageUrl(this.library);
          is_exempted =
              is_exempted
                  && (purl.getNamespace() == null
                      || libid.getMvnGroup().equals(purl.getNamespace()))
                  && // No purl.namespace || purl.namespace==libid.mvnGroup
                  libid.getArtifact().equals(purl.getName())
                  && (purl.getVersion() == null
                      || libid
                          .getVersion()
                          .equals(
                              purl.getVersion())); // No purl.version || purl.version==libid.version
        } catch (MalformedPackageURLException e) {
          log.error(e.getMessage());
          is_exempted = false;
        }
      }

      // Digest
      else {
        is_exempted = is_exempted && this.library.equals(_vd.getDep().getLib().getDigest());
      }
    }

    return is_exempted;
  }

  /**
   * Creates an instance of {@link AffectedLibrary}, which can be used to mark libraries as non-vulnerable with regards to
   * a given vulnerability.
   * @return
   */
  public AffectedLibrary createAffectedLibrary(VulnerableDependency _vd) {
    AffectedLibrary al = null;

    if (ALL.equals(this.bugId) || ALL.equals(this.library)) {
      log.warn(
          "Cannot create library assessment for the wildcard ("
              + ALL
              + ") exemption ["
              + this.toShortString()
              + "]");
    }

    // Create for libraryId
    else if (this.library.startsWith(PURL_PREFIX)) {
      try {
        final PackageURL purl = ExemptionBug.createPackageUrl(this.library);
        if (purl.getVersion() == null) {
          log.warn(
              "Cannot create affected library for Package URL exemptions without version"
                  + " identifier: ["
                  + this.toShortString()
                  + "]");
        } else {
          al = new AffectedLibrary();
          al.setSource(AffectedVersionSource.MANUAL);
          al.setLibraryId(_vd.getDep().getLib().getLibraryId());
          al.setExplanation(this.reason);
          al.setBugId(_vd.getBug());
          al.setAffected(false);
        }
      } catch (MalformedPackageURLException e) {
        log.error(
            "Cannot create affected library from exemption ["
                + this.toShortString()
                + "]:"
                + e.getMessage());
      } catch (Exception e) {
        log.error(
            "Cannot create affected library from exemption ["
                + this.toShortString()
                + "]:"
                + e.getMessage());
      }
    }

    // Create for lib
    else {
      al = new AffectedLibrary();
      al.setSource(AffectedVersionSource.MANUAL);
      al.setLib(_vd.getDep().getLib());
      al.setExplanation(this.reason);
      al.setBugId(_vd.getBug());
      al.setAffected(false);
    }

    return al;
  }

  /**
   * Reads all {@link Configuration} settings starting with {@link ExemptionBug#CFG_PREFIX} in order to create {@link ExemptionBug}s.
   * Also considers the deprecated settings {@link ExemptionBug#DEPRECATED_CFG_PREFIX} and {@link ExemptionBug#CFG_PREFIX_EXEMPTED_SCOPES} for backward compatibility.
   *
   * @param _cfg
   * @return
   */
  public static ExemptionSet readFromConfiguration(Configuration _cfg) {
    final ExemptionSet exempts = new ExemptionSet();

    // New format
    final Iterator<String> iter = _cfg.getKeys(CFG_PREFIX);
    while (iter.hasNext()) {
      final String k = iter.next();
      if (k.endsWith("." + "reason")) {
        final String[] key_elements = k.split("\\.");
        if (key_elements.length == 5) {
          final String vuln = key_elements[3];
          final String reason = _cfg.getString(CFG_PREFIX + "." + vuln + "." + "reason");
          final String[] libs = _cfg.getStringArray(CFG_PREFIX + "." + vuln + "." + "libraries");

          if (libs == null || libs.length == 0) {
            exempts.add(new ExemptionBug(vuln, ExemptionBug.ALL, reason));
          } else {
            for (String lib : libs) {
              if (lib.startsWith(PURL_PREFIX)) {
                try {
                  ExemptionBug.createPackageUrl(lib);
                  exempts.add(new ExemptionBug(vuln, lib, reason));
                } catch (MalformedPackageURLException e) {
                  log.error(e.getMessage());
                  continue;
                }
              } else {
                exempts.add(new ExemptionBug(vuln, lib, reason));
              }
            }
          }
        } else {
          log.error("Invalid exemption format [" + CFG_PREFIX + "." + k + "]");
        }
      }
    }

    // Deprecated format
    final String[] bugs = _cfg.getStringArray(DEPRECATED_CFG_PREFIX);
    if (bugs != null && bugs.length > 0) {
      log.warn(
          "Exemption with key [" + DEPRECATED_CFG_PREFIX + "] is deprecated, switch to new format");
      for (String b : bugs) {
        final String reason = _cfg.getString(DEPRECATED_CFG_PREFIX + "." + b, null);
        exempts.add(new ExemptionBug(b, null, (reason == null ? "No reason provided" : reason)));
      }
    }

    return exempts;
  }

  /**
   * Reads all {@link Configuration} settings starting with {@link ExemptionBug#CFG_PREFIX} in order to create {@link ExemptionBug}s.
   * Also considers the deprecated settings {@link ExemptionBug#DEPRECATED_CFG_PREFIX} and {@link ExemptionBug#CFG_PREFIX_EXEMPTED_SCOPES} for backward compatibility.
   *
   * @param _map
   * @return
   */
  public static ExemptionSet readFromConfiguration(Map<String, String> _map) {
    final ExemptionSet exempts = new ExemptionSet();

    // New format
    for (String k : _map.keySet()) {
      if (k.startsWith((CFG_PREFIX) + ".") && k.endsWith("." + "reason")) {
        final String[] key_elements = k.split("\\.");
        if (key_elements.length == 5) {
          final String vuln = key_elements[3];
          final String reason = _map.get(CFG_PREFIX + "." + vuln + "." + "reason");
          final String libs = _map.get(CFG_PREFIX + "." + vuln + "." + "libraries");

          if (libs == null || libs.equals("")) {
            exempts.add(new ExemptionBug(vuln, ExemptionBug.ALL, reason));
          } else {
            for (String lib : libs.split(",")) {
              if (lib.startsWith(PURL_PREFIX)) {
                try {
                  ExemptionBug.createPackageUrl(lib);
                  exempts.add(new ExemptionBug(vuln, lib, reason));
                } catch (MalformedPackageURLException e) {
                  log.error(e.getMessage());
                  continue;
                }
              } else {
                exempts.add(new ExemptionBug(vuln, lib, reason));
              }
            }
          }
        } else {
          log.error("Invalid exemption format: [" + CFG_PREFIX + "." + k + "]");
        }
      }
    }

    // Deprecated configuration format
    if (_map.containsKey(DEPRECATED_CFG_PREFIX)) {
      final String[] bugs = _map.get(DEPRECATED_CFG_PREFIX).split(",");
      if (bugs != null && bugs.length > 0) {
        log.warn(
            "Exemption with key ["
                + DEPRECATED_CFG_PREFIX
                + "] is deprecated, switch to new format");
        for (String b : bugs) {
          b = b.trim();
          final String reason = _map.get(DEPRECATED_CFG_PREFIX + "." + b);
          exempts.add(new ExemptionBug(b, null, (reason == null ? "No reason provided" : reason)));
        }
      }
    }

    // Deprecated key value from backend (to support backward compatibility with results already
    // existing in backend for apps that scanned with client versions <3.1.12)
    if (_map.containsKey(DEPRECATED_KEY_BACKEND)) {
      final String[] bugs = _map.get(DEPRECATED_KEY_BACKEND).split(",");
      if (bugs != null && bugs.length > 0) {
        for (String b : bugs) {
          b = b.trim();
          final String reason = _map.get(DEPRECATED_KEY_BACKEND + "." + b);
          exempts.add(new ExemptionBug(b, null, (reason == null ? "No reason provided" : reason)));
        }
      }
    }

    return exempts;
  }

  /**
   * Creates a {@link PackageURL} from the given {@link String}, whereby URLs of type 'maven' require
   * namespace and name, and URLs of type 'pypi' require a name. All other types are not supported and
   * will result in a {@link MalformedPackageURLException}.
   *
   * @param _url
   * @return
   * @throws MalformedPackageURLException
   */
  public static final PackageURL createPackageUrl(String _url) throws MalformedPackageURLException {
    final PackageURL purl = new PackageURL(_url);

    // PURL type == maven
    if ("maven".equalsIgnoreCase(purl.getType())) {
      if (purl.getNamespace() == null
          || purl.getNamespace().equals("")
          || purl.getName() == null
          || purl.getName().equals("")) {
        throw new MalformedPackageURLException(
            "Package URLs of type ["
                + purl.getType()
                + "] require a valid namespace and name: ["
                + purl
                + "]");
      }
    }

    // PURL type == pypi
    else if ("pypi".equalsIgnoreCase(purl.getType())) {
      if (purl.getName() == null || purl.getName().equals("")) {
        throw new MalformedPackageURLException(
            "Package URLs of type [" + purl.getType() + "] require a valid name: [" + purl + "]");
      }
    }

    // Other types are not supported
    else {
      throw new MalformedPackageURLException(
          "Package URLs of type [" + purl.getType() + "] are not supported: [" + purl + "]");
    }

    return purl;
  }

  @Override
  public String toString() {
    return "[bug=" + this.bugId + ", libs=" + this.library + "]";
  }

  public String toShortString() {
    return this.bugId + " (" + this.library + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
    result = prime * result + ((library == null) ? 0 : library.hashCode());
    result = prime * result + ((reason == null) ? 0 : reason.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ExemptionBug other = (ExemptionBug) obj;
    if (bugId == null) {
      if (other.bugId != null) return false;
    } else if (!bugId.equals(other.bugId)) return false;
    if (library == null) {
      if (other.library != null) return false;
    } else if (!library.equals(other.library)) return false;
    if (reason == null) {
      if (other.reason != null) return false;
    } else if (!reason.equals(other.reason)) return false;
    return true;
  }
}
