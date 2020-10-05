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
package org.eclipse.steady.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.steady.backend.model.LibraryId;
import org.junit.Test;

public class ArtifactMapsTest {

  /**
   * Checks whether the configuration setting ARTIFACT_MAPS contains valid JSON.
   */
  @Test
  public void testsBuildMaps() {
    // Read the JSON as String from the configuration and deserialize it
    final ArtifactMaps maps = ArtifactMaps.buildMaps();

    // Must not be empty
    assertTrue(!maps.getMaps().isEmpty());

    // For Tomcat, there should be 2 other artifacts after tomcat:catalina
    final LibraryId tomcat = new LibraryId();
    tomcat.setMvnGroup("tomcat");
    tomcat.setArtifact("catalina");
    final List<LibraryId> gt_tomcat_catalina = maps.getGreaterArtifacts(tomcat);
    assertEquals(2, gt_tomcat_catalina.size());

    // Get latest versions of synonyms from Maven Central
    /*try {
    	final Collection<LibraryId> all_libids = ServiceWrapper.getInstance().getAllArtifactVersions(tomcat.getMvnGroup(), tomcat.getArtifact(), true, null);
    	for(LibraryId syn: gt_tomcat_catalina) {
    		all_libids.addAll(ServiceWrapper.getInstance().getAllArtifactVersions(syn.getMvnGroup(), syn.getArtifact(), true, null));
    	}
    } catch (ServiceConnectionException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    } catch (Exception e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
    }*/
  }
}
