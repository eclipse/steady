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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.kb.model.Commit;
import com.sap.psr.vulas.kb.model.FileChange;
import com.sap.psr.vulas.shared.enums.ConstructType;

/**
 * Identifying all the constructs
 */
public class ConstructSet {
  private static final String BEFORE_FOLDER = "before";
  private static final String AFTER_FOLDER = "after";
  
  private static final Logger log = LoggerFactory.getLogger(ConstructSet.class);

  /**
   * 
   * Identifies all constructs that have been changed in the given revision. Getting the changed
   * sets of before and after commit, and afterwards compared according to the syntax of the
   * respective programming language.
   * 
   * @param _commit a {@link com.sap.psr.vulas.kb.model.Commit} object.
   * @param _changes a {@link java.util.Set} object.
   * @return a {@link java.util.Set} object.
   */
  public static Set<ConstructChange> identifyConstructChanges(Commit _commit, Map<String, Set<ConstructChange>> _changes) {
    String commitId = _commit.getCommitId();
    String branch = _commit.getBranch();
    String timeStamp = _commit.getTimestamp();
    String repoUrl = _commit.getRepoUrl();
    String commitDir = _commit.getDirectory();

    String rev = commitId+branch;
    Set<ConstructChange> ch = _changes.get(rev);
    if (ch == null) {
      ch = new TreeSet<ConstructChange>();

      // Get and loop over all changed files
      final Set<FileChange> file_changes = getFileChanges(commitDir, repoUrl);
      for (FileChange c : file_changes) {
        try {
          // Check if the file ext is supported
          if (FileAnalyzerFactory.isSupportedFileExtension(c.getFileExtension())) {
            final FileComparator comparator = new FileComparator(c, commitId, timeStamp);
            ch.addAll(comparator.identifyChanges());
          }
        } catch (Exception e) {
          ConstructSet.log.error("Error while analyzing {} : {}", c, e.getMessage());
        }
      }

      // remove MOD classes if no MOD method is included (excluding modification in inner classes)
      // ConstructChange[] cc = (ConstructChange[]) ch.toArray();
      ConstructChange[] ch_array = ch.toArray(new ConstructChange[ch.size()]);
      for (ConstructChange c : ch_array) {
        ConstructId tocheck = c.getConstruct().getId();
        boolean toDelete = true;
        if (tocheck.getSharedType() == ConstructType.CLAS) {
          for (ConstructChange in : ch_array) {
            if (tocheck.equals(in.getConstruct().getId().getDefinitionContext())) {
              toDelete = false;
              break;
            }
          }
          if (toDelete) {
            ch.remove(c);
            ConstructSet.log.info("Class [{}] removed from changeList as no METH/CONS included", tocheck.toString());
          }
        }
      }
      _changes.put(rev, ch);
    }
    return ch;
  }


  /**
   * get file changes for a commit
   * 
   * @param _path a {@link java.lang.String} object.
   * @param _rev a {@link java.lang.String} object.
   * @param _url a {@link java.lang.String} object.
   * @return a {@link java.util.Set} object.
   */
  private static Set<FileChange> getFileChanges(final String _path, String _url) {
    final Set<FileChange> filesChanged = new HashSet<>();

    final List<String> filesModifiedOrDeleted = new ArrayList<>();

    String beforePath = _path + File.separator + BEFORE_FOLDER;
    File beforeFile = new File(beforePath);
    if(!beforeFile.exists()) {
      ConstructSet.log.error("Path - {} does not exists", beforePath);
      return Collections.emptySet();
    }

    Collection<File> beforeFiles = FileUtils.listFiles(beforeFile, null, true);
    for(File file: beforeFiles) {
      if(file.isFile()) {
        String filePathWithDir = file.getAbsolutePath();
        String filePath = filePathWithDir.split(File.separator + BEFORE_FOLDER)[1];
        String afterFilePath = _path + File.separator + AFTER_FOLDER +filePath;
        if(Files.exists(Paths.get(afterFilePath))) {
          filesChanged.add(new FileChange(_url, filePath, new File(filePathWithDir), new File(afterFilePath)));
        }else {
          filesChanged.add(new FileChange(_url, filePath, new File(filePathWithDir), null));
        }
        filesModifiedOrDeleted.add(filePath);
      }
    }

    String afterPath = _path + File.separator + AFTER_FOLDER;
    File afterFile = new File(afterPath);
    if(!afterFile.exists()) {
      ConstructSet.log.error("Path - {} does not exists", afterPath);
      return Collections.emptySet();
    }

    Collection<File> afterFiles = FileUtils.listFiles(afterFile, null, true);
    for(File file: afterFiles) {
      if(file.isFile()) {
        String filePathWithDir = file.getAbsolutePath();
        String filePath = filePathWithDir.split(File.separator + AFTER_FOLDER)[1];
        if(!filesModifiedOrDeleted.contains(filePath)) {
          filesChanged.add(new FileChange(_url, filePath, null, new File(filePathWithDir)));
        }
      }
    }

    return filesChanged;
  }

  /**
   * Returns the union of changes for the given revisions. If null is passed as argument, all revisions will be returned.
   * If an empty array is passed, an empty set will be returned.
   * @return a {@link java.util.Set} object.
   */
  public static Set<ConstructChange> getConsolidatedChanges(List<Commit> _revs, Map<String, Set<ConstructChange>> _changes) {
    final Set<ConstructChange> ch = new TreeSet<ConstructChange>();
    for(Entry<String, Set<ConstructChange>> entry: _changes.entrySet()) {
      if(_revs==null)
        ch.addAll(_changes.get(entry.getKey()));
      else {
        for(Commit rev: _revs) {
          String commitAndBranch = rev.getCommitId()+rev.getBranch();
          if(entry.getKey().equals(commitAndBranch))
            ch.addAll(_changes.get(entry.getKey()));
        }
      }
    }
    return ch;
  }
}
