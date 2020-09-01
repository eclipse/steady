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
package org.eclipse.steady.cia.dependencyfinder;

import java.util.Iterator;

import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.diff.JarDiffResult;

import com.jeantessier.classreader.Class_info;
import com.jeantessier.classreader.Classfile;
import com.jeantessier.diff.ClassDifferences;
import com.jeantessier.diff.Differences;
import com.jeantessier.diff.InterfaceDifferences;
import com.jeantessier.diff.PackageDifferences;
import com.jeantessier.diff.ProjectDifferences;
import com.jeantessier.diff.VisitorBase;

/**
 * Visits all kinds of changes and creates instances of the classes contained in package
 * {@link com.sap.psr.vulas.cia.dependencyfinder.model}. Inspired from {@link com.jeantessier.diff.Report}.
 */
public class JarDiffVisitor extends VisitorBase {

  private JarDiffResult jarDiffResult = new JarDiffResult();

  /**
   * <p>Constructor for JarDiffVisitor.</p>
   *
   * @param _old a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   * @param _new a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   */
  public JarDiffVisitor(Artifact _old, Artifact _new) {
    this.jarDiffResult.setOldLibId(_old.getLibId());
    this.jarDiffResult.setNewLibId(_new.getLibId());
  }

  /**
   * <p>Getter for the field <code>jarDiffResult</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.diff.JarDiffResult} object.
   */
  public JarDiffResult getJarDiffResult() {
    return jarDiffResult;
  }

  /**
   * <p>Setter for the field <code>jarDiffResult</code>.</p>
   *
   * @param jarDiffResult a {@link org.eclipse.steady.shared.json.model.diff.JarDiffResult} object.
   */
  public void setJarDiffResult(JarDiffResult jarDiffResult) {
    this.jarDiffResult = jarDiffResult;
  }

  /** {@inheritDoc} */
  public void visitProjectDifferences(ProjectDifferences differences) {
    for (Differences packageDifference : differences.getPackageDifferences()) {
      packageDifference.accept(this);
    }
  }

  /** {@inheritDoc} */
  public void visitPackageDifferences(PackageDifferences differences) {
    if (differences.isRemoved()) {
      jarDiffResult.addDeletedPackage(
          new org.eclipse.steady.shared.json.model.ConstructId(
              ProgrammingLanguage.JAVA, ConstructType.PACK, differences.getName()));
    }
    for (Differences classDiffenrence : differences.getClassDifferences()) {
      classDiffenrence.accept(this);
    }
    if (differences.isNew()) {
      jarDiffResult.addNewPackage(
          new org.eclipse.steady.shared.json.model.ConstructId(
              ProgrammingLanguage.JAVA, ConstructType.PACK, differences.getName()));
    }
  }

  private org.eclipse.steady.shared.json.model.ConstructId getClass(Classfile _classfile) {
    org.eclipse.steady.shared.json.model.ConstructId c =
        new org.eclipse.steady.shared.json.model.ConstructId();

    c.setLang(ProgrammingLanguage.JAVA);
    c.setQname(_classfile.getClassName());

    if (_classfile.isDeprecated())
      c.addRelates(
          "annotation",
          new ConstructId(ProgrammingLanguage.JAVA, ConstructType.INTF, "java.lang.Deprecated"));

    c.addAttribute("final", _classfile.isFinal());
    c.addAttribute("synthetic", _classfile.isSynthetic());
    c.addAttribute("super", _classfile.isSuper());

    // Visibility
    if (_classfile.isPublic()) c.addAttribute("visibility", "public");
    else if (_classfile.isPackage()) c.addAttribute("visibility", "package");

    // Extends and Implements
    if (_classfile.isInterface()) {
      c.setType(ConstructType.INTF);
      Iterator<? extends Class_info> i = _classfile.getAllInterfaces().iterator();
      while (i.hasNext()) {
        c.addRelates(
            "extends",
            new ConstructId(ProgrammingLanguage.JAVA, ConstructType.INTF, i.next().toString()));
      }
    } else {
      c.setType(ConstructType.CLAS);
      c.addRelates(
          "extends",
          new ConstructId(
              ProgrammingLanguage.JAVA, ConstructType.CLAS, _classfile.getSuperclassName()));
      c.addAttribute("abstract", _classfile.isAbstract());
      Iterator<? extends Class_info> i = _classfile.getAllInterfaces().iterator();
      while (i.hasNext()) {
        c.addRelates(
            "implements",
            new ConstructId(ProgrammingLanguage.JAVA, ConstructType.INTF, i.next().toString()));
      }
    }

    return c;
  }

  /** {@inheritDoc} */
  public void visitClassDifferences(ClassDifferences differences) {
    if (differences.isRemoved()) {
      jarDiffResult.addDeletedClass(this.getClass(differences.getOldClass()));
    }

    if (differences.isModified()) {
      ClassDiffVisitor visitor = new ClassDiffVisitor(this.getClass(differences.getNewClass()));
      differences.accept(visitor);
      jarDiffResult.addModifiedClass(visitor.getClassDiffResult());
    }

    if (differences.isNew()) {
      jarDiffResult.addNewClass(this.getClass(differences.getNewClass()));
    }

    if (isDeprecated()) {
      jarDiffResult.addDeprecatedClass(this.getClass(differences.getNewClass()));
    }

    if (isUndeprecated()) {
      jarDiffResult.addUndeprecatedClass(this.getClass(differences.getNewClass()));
    }
  }

  /** {@inheritDoc} */
  public void visitInterfaceDifferences(InterfaceDifferences differences) {
    if (differences.isRemoved()) {
      jarDiffResult.addDeletedInterface(this.getClass(differences.getOldClass()));
    }

    if (differences.isModified()) {
      ClassDiffVisitor visitor = new ClassDiffVisitor(this.getClass(differences.getNewClass()));
      differences.accept(visitor);
      jarDiffResult.addModifiedInterface(visitor.getClassDiffResult());
    }

    if (differences.isNew()) {
      jarDiffResult.addNewInterface(this.getClass(differences.getNewClass()));
    }

    if (isDeprecated()) {
      jarDiffResult.addDeprecatedInterface(this.getClass(differences.getNewClass()));
    }

    if (isUndeprecated()) {
      jarDiffResult.addUndeprecatedInterface(this.getClass(differences.getNewClass()));
    }
  }
}
