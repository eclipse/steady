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
package org.eclipse.steady.cia.model.nexus;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusNGData class.</p>
 *
 */
@XmlRootElement(name = "data")
public class NexusNGData {

  @XmlElement(name = "artifact")
  private List<NexusArtifact> artifact = new ArrayList<NexusArtifact>();

  /**
   * <p>getArtifactList.</p>
   *
   * @return a {@link java.util.List} object.
   */
  public List<NexusArtifact> getArtifactList() {
    return artifact;
  }

  /**
   * <p>setArtifactList.</p>
   *
   * @param _l a {@link java.util.List} object.
   */
  public void setArtifactList(List<NexusArtifact> _l) {
    this.artifact = _l;
  }
}
