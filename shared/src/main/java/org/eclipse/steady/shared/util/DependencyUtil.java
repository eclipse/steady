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
package org.eclipse.steady.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.json.model.Library;

/**
 * <p>DependencyUtil class.</p>
 */
public class DependencyUtil {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(DependencyUtil.class);

  /**
   * Returns a set of dependencies such that every {@link Dependency} points to a different {@link Library}.
   * This is needed because {@link Dependency#equals(Object)} considers all kinds of members of {@link Dependency}, while
   * the relational database table storing dependencies does not.
   *
   * @param _deps a {@link java.util.Collection} object.
   * @return a {@link java.util.Set} object.
   */
  public static Set<Dependency> removeDuplicateLibraryDependencies(Collection<Dependency> _deps) {
    final Set<Dependency> clean_set = new HashSet<Dependency>();
    if (_deps != null) {
      for (Dependency d : _deps) {
        final Dependency existing_dep = DependencyUtil.getLibraryDependency(clean_set, d.getLib());
        if (existing_dep == null) {
          clean_set.add(d);
        } else {
          log.warn(
              "Dependency "
                  + d
                  + " removed from set, one on the same library already exists: "
                  + existing_dep);
        }
      }
    }
    return clean_set;
  }

  /**
   * Returns true of the given set of dependencies already contains a {@link Dependency} for the given {@link Library}, false otherwise.
   *
   * @param _deps a {@link java.util.Set} object.
   * @param _lib a {@link org.eclipse.steady.shared.json.model.Library} object.
   * @return a boolean.
   */
  public static boolean containsLibraryDependency(Set<Dependency> _deps, Library _lib) {
    return DependencyUtil.getLibraryDependency(_deps, _lib) != null;
  }

  /**
   * Returns the {@link Dependency} for the given {@link Library}, null if no such dependency exists.
   *
   * @param _deps a {@link java.util.Set} object.
   * @param _lib a {@link org.eclipse.steady.shared.json.model.Library} object.
   * @return a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   */
  public static Dependency getLibraryDependency(Set<Dependency> _deps, Library _lib) {
    for (Dependency d : _deps) {
      if (d.getLib().equals(_lib)) {
        return d;
      }
    }
    return null;
  }

  /**
   * Returns true of the given set of dependencies already contains a {@link Dependency} with the same library' digest, parent and relativePath, false otherwise.
   *
   * @param _deps a {@link java.util.Set} object.
   * @param _dep a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   * @return a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   */
  public static Dependency getDependency(Set<Dependency> _deps, Dependency _dep) {
    for (Dependency d : _deps) {
      if (d.getLib().equals(_dep.getLib())
          && d.getParent().equalLibParentRelPath(_dep.getParent())
          && d.getRelativePath().equals(_dep.getRelativePath())) {
        return d;
      }
    }
    return null;
  }

  /**
   * Checks whether the set of dependencies is valid:
   * - every {@link Dependency} tuple (sha1, parent and relativePath) appears only once.
   * - every {@link Dependency} appearing as parent also appear in the _deps set
   *
   * @param _app an {@link Application} whose dependencies are checked.
   * @return a boolean.
   */
  public static boolean isValidDependencyCollection(Application _app) {
    Collection<Dependency> _deps = _app.getDependencies();
    final Set<Dependency> main_set = new HashSet<Dependency>();
    final Set<Dependency> parent_set = new HashSet<Dependency>();
    final List<String> errs = new ArrayList<String>();
    if (_deps != null) {
      for (Dependency d : _deps) {
        d.setAppRecursively(_app);
        final Dependency existing_dep = DependencyUtil.getDependency(main_set, d);
        if (existing_dep == null) {
          main_set.add(d);
          if (d.getParent() != null) parent_set.add(d.getParent());
        } else {
          errs.add(
              "Dependency "
                  + d
                  + " occurs multiple times in the set, one on the same library already exists: "
                  + existing_dep);
        }
      }
      for (Dependency d : parent_set) {
        if (!main_set.contains(d)) {
          errs.add("Dependency parent " + d + " is not declared as dependency itself");
        }
      }
    }
    if (!errs.isEmpty()) {
      log.error("The parent-child relationships of application dependencies have inconsistencies:");
      for (String err : errs) log.error("    " + err);
    }
    return errs.isEmpty();
  }
}
