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
package com.sap.psr.vulas.kb.context;

import java.util.List;

/**
 * Context for the kb-hub flow
 */
public class Context {
  private String vcs;
  private String bugId;
  private String maturity;
  private String origin;
  private String descriptionAlt;
  private List<String> reference;
  private List<String> constructChanges; // TODO: construct changes

  public String getVcs() {
    return vcs;
  }

  public void setVcs(String vcs) {
    this.vcs = vcs;
  }

  public String getBugId() {
    return bugId;
  }

  public void setBugId(String bugId) {
    this.bugId = bugId;
  }

  public String getMaturity() {
    return maturity;
  }

  public void setMaturity(String maturity) {
    this.maturity = maturity;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getDescriptionAlt() {
    return descriptionAlt;
  }

  public void setDescriptionAlt(String descriptionAlt) {
    this.descriptionAlt = descriptionAlt;
  }

  public List<String> getReference() {
    return reference;
  }

  public void setReference(List<String> reference) {
    this.reference = reference;
  }

  public List<String> getConstructChanges() {
    return constructChanges;
  }

  public void setConstructChanges(List<String> constructChanges) {
    this.constructChanges = constructChanges;
  }
}
