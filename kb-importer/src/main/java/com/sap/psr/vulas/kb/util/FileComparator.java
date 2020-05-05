/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.kb.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.shared.json.model.FileChange;


/**
 * <p>
 * FileComparator class.
 * </p>
 *
 */
public class FileComparator {

  private static final Logger log = LoggerFactory.getLogger(ConstructSet.class);
  private File def, fix = null;

  private FileAnalyzer defAnalyzer, fixAnalyzer = null;

  private Set<ConstructChange> changes = null;
  private String revision = null;
  private String repo = null;
  private String repoPath = null;
  private String timeStamp = null;

  /**
   * Constructor using a FileChange (as produced by IVCSClient). Potential refactoring: Make
   * FileChange include both revision and repository path, adopt this constructor and delete the
   * other one below. As a result, fewer classes carry repository information. Right now, revision
   * and path info is kept redundantly in several classes.
   *
   * @param _c the FileChange holding the old and new file to be compared (+ the relative repo path)
   * @param _rev the revision in which the file was changed
   * @throws java.io.IOException
   * @param _time_stamp a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.FileAnalysisException if any.
   */
  public FileComparator(FileChange _c, String _rev, String _time_stamp)
      throws FileAnalysisException, IOException {
    this(_c.getOldFile(), _c.getNewFile(), _rev, _c.getRepo(), _c.getRepoPath(), _time_stamp);
  }

  /**
   * Potential refactoring: Delete constructor (or make private), see proposal of prev. constructor.
   *
   * @param _def a {@link java.io.File} object.
   * @param _fix a {@link java.io.File} object.
   * @param _rev a {@link java.lang.String} object.
   * @param _repo_path a {@link java.lang.String} object.
   * @throws java.io.IOException
   * @param _repo a {@link java.lang.String} object.
   * @param _time_stamp a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.FileAnalysisException if any.
   */
  public FileComparator(File _def, File _fix, String _rev, String _repo, String _repo_path,
      String _time_stamp) throws IOException, FileAnalysisException {
    if (_def == null && _fix == null)
      throw new IllegalArgumentException(
          "The comparator requires at least one file as input, either the defective file, the fixed file or both");
    this.revision = _rev;
    this.repo = _repo;
    this.repoPath = _repo_path;
    this.timeStamp = _time_stamp;

    if (_def != null) {
      this.def = _def;
      this.defAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(_def);
      this.defAnalyzer.getConstructs();
    }
    if (_fix != null) {
      this.fix = _fix;
      this.fixAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(_fix);
      this.fixAnalyzer.getConstructs();
    }
  }

  /**
   * Identifies whether programming constructs changed between the defective and fixed version.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructChange> identifyChanges() {
    if (this.changes == null) {
      this.changes = new TreeSet<ConstructChange>();

      // Loop constructs of defective file (if any)
      // Add to changes if construct has been modified (!equals) or does not exist any longer
      if (this.defAnalyzer != null) {
        try {
          for (ConstructId key : this.defAnalyzer.getConstructs().keySet()) {
            if (this.fixAnalyzer != null && this.fixAnalyzer.containsConstruct(key)) {
              if (!this.defAnalyzer.getConstruct(key).equals(this.fixAnalyzer.getConstruct(key))) {
                // Modification (MOD)
                final ConstructChange c = new ConstructChange(this.repo, this.repoPath,
                    this.defAnalyzer.getConstruct(key), this.fixAnalyzer.getConstruct(key),
                    this.revision, this.timeStamp);

                // Always add the construct change, no matter the signature change (if any)
                // if(c.hasSignatureChange())
                this.changes.add(c);
              }
            } else {
              this.changes.add(new ConstructChange(this.repo, this.repoPath,
                  this.defAnalyzer.getConstruct(key), null, this.revision, this.timeStamp));
            }
          }
        } catch (FileAnalysisException e) {
          FileComparator.log
              .error("Error while comparing defective and fixed version: {}", e.getMessage(), e);
        }
      }
      // Loop constructs of fixed file (if any)
      // Add to changes if construct has been modified (!equals) or does not exist any longer
      if (this.fixAnalyzer != null) {
        try {
          for (ConstructId key : this.fixAnalyzer.getConstructs().keySet()) {
            if (this.defAnalyzer != null && this.defAnalyzer.containsConstruct(key)) {
              if (!this.fixAnalyzer.getConstruct(key).equals(this.defAnalyzer.getConstruct(key))) {
                // Modification (MOD)
                final ConstructChange chg = new ConstructChange(this.repo, this.repoPath,
                    this.defAnalyzer.getConstruct(key), this.fixAnalyzer.getConstruct(key),
                    this.revision, this.timeStamp);

                // Always add the construct change if not existing yet, no matter the signature
                // change (if any)
                if (!this.changes.contains(chg))// && chg.hasSignatureChange())
                  this.changes.add(chg);
              }
            } else {
              this.changes.add(new ConstructChange(this.repo, this.repoPath, null,
                  this.fixAnalyzer.getConstruct(key), this.revision, this.timeStamp));
            }
          }
        } catch (FileAnalysisException e) {
          FileComparator.log
              .error("Error while comparing defective and fixed version: {}",e.getMessage(), e);
        }
      }
    }
    return changes;
  }
}
