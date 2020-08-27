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
package org.eclipse.steady.cia.rest;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.cia.util.ClassDownloader;
import org.eclipse.steady.java.JavaFileAnalyzer2;
import org.eclipse.steady.java.JavaId;
import org.junit.Test;

public class IT04_ConstructControllerTest {

  @Test
  public void getConstructforGavTest() {

    JavaId jid =
        JavaId.parseMethodQName(
            "org.apache.cxf.jaxrs.provider.atom.AbstractAtomProvider.readFrom(Class,Type,Annotation[],MediaType,MultivaluedMap,InputStream)");
    JavaId ctx = (JavaId) jid.getDefinitionContext();
    Path file =
        ClassDownloader.getInstance()
            .getClass(
                "org.apache.cxf",
                "cxf-rt-rs-extension-providers",
                "2.6.2-sap-02",
                ctx.getQualifiedName(),
                ClassDownloader.Format.JAVA);

    if (file == null) {
      System.out.println("Cannot retrieve class");

    } else {
      // Use ANTLR to parse the Java file
      JavaFileAnalyzer2 jfa = new JavaFileAnalyzer2();
      try {
        jfa.analyze(file.toFile());
        assertTrue(jfa.containsConstruct(jid));
      } catch (FileAnalysisException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
