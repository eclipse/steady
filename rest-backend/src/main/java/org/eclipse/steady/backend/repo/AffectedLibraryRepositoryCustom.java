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
package org.eclipse.steady.backend.repo;

import java.util.List;
import java.util.TreeSet;

import org.eclipse.steady.backend.model.AffectedLibrary;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.VulnerableDependency;
import org.eclipse.steady.shared.enums.AffectedVersionSource;

/**
 * Specifies additional methods of the {@link AffectedLibraryRepository}.
 */
public interface AffectedLibraryRepositoryCustom {

  /**
   * <p>customSave.</p>
   *
   * @param _bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param _aff_libs an array of {@link org.eclipse.steady.backend.model.AffectedLibrary} objects.
   * @return a {@link java.util.List} object.
   */
  public List<AffectedLibrary> customSave(Bug _bug, AffectedLibrary[] _aff_libs);

  /**
   * <p>computeAffectedLib.</p>
   *
   * @param _vdList a {@link java.util.TreeSet} object.
   */
  public void computeAffectedLib(TreeSet<VulnerableDependency> _vdList);

  /**
   * <p>computeAffectedLib.</p>
   *
   * @param _vd a {@link org.eclipse.steady.backend.model.VulnerableDependency} object.
   * @param _lib a {@link org.eclipse.steady.backend.model.Library} object.
   */
  public void computeAffectedLib(VulnerableDependency _vd, Library _lib);

  /**
   * <p>Get affected libraries.</p>
   *
   * @param _bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @param _source a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   * @param _onlyWellknown a {@link java.lang.Boolean} object.
   * @return a {@link java.util.List} object.
   */
  public List<AffectedLibrary> getAffectedLibraries(
      Bug _bug, AffectedVersionSource _source, Boolean _onlyWellknown);

  
  /**
   * <p>Get affected libraries with highest priority for each {@link LibraryId}.</p>
   * 
 * @param bug
 * @param onlyWellknown
 * @return
 */
public List<AffectedLibrary> getResolvedAffectedLibraries(Bug bug, Boolean onlyWellknown);
}
