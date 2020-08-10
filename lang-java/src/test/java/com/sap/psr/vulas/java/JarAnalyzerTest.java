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
package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.LibraryId;

public class JarAnalyzerTest {

    @Test
    public void testGetFqClassname() {
        String je = "Foo.class";
        String fqn = JarAnalyzer.getFqClassname(je);
        assertEquals("Foo", fqn);

        je = "1.0/com/sun/tools/xjc/grammar/IgnoreItem.class";
        fqn = JarAnalyzer.getFqClassname(je);
        assertEquals(null, fqn);

        je = "com/sun/tools/xjc/grammar/IgnoreItem.class";
        fqn = JarAnalyzer.getFqClassname(je);
        assertEquals("com.sun.tools.xjc.grammar.IgnoreItem", fqn);
    }

    @Test
    public void testIsJavaIdentifier() {
        assertEquals(false, JarAnalyzer.isJavaIdentifier("1.0"));
        assertEquals(true, JarAnalyzer.isJavaIdentifier("Foo"));
        assertEquals(false, JarAnalyzer.isJavaIdentifier("{Foo"));
    }

    /**
     * Test the analysis of Junit 4.12, which caused a NPE in Vulas releases before 1.0.2 and 1.1.0.
     */
    @Test
    public void testJunitAnalysis() {
        try {
            final JarAnalyzer ja = new JarAnalyzer();
            ja.analyze(new File("./src/test/resources/junit-4.12.jar"));
            ja.setWorkDir(Paths.get("./target"));
            ja.setRename(true);
            JarAnalyzer.setAppContext(
                    new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
            ja.call();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * The archive "org.apache.servicemix.bundles.jaxb-xjc-2.2.4_1.jar" contains 932 class files below directory 1.0 (900 have been deleted).
     * Those cannot be transformed into {@link JavaClassId}s, and corresponding warning messages are printed.
     *
     * @see Jira VULAS-737
     */
    @Test
    public void testInvalidClassEntries() {
        try {
            final JarAnalyzer ja = new JarAnalyzer();
            ja.setWorkDir(Paths.get("./target"));
            ja.setRename(true);
            JarAnalyzer.setAppContext(
                    new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
            ja.analyze(
                    new File(
                            "./src/test/resources/org.apache.servicemix.bundles.jaxb-xjc-2.2.4_1.jar"));
            ja.call();
            assertEquals(8984, ja.getConstructIds().size());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testCommonsFileUploadAnalysis() {
        try {
            final JarAnalyzer ja = new JarAnalyzer();
            ja.analyze(new File("./src/test/resources/commons-fileupload-1.3.1.jar"));
            ja.setWorkDir(Paths.get("./target"));
            ja.setRename(true);
            JarAnalyzer.setAppContext(
                    new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
            ja.call();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void jarAnalyzerTest() {
        try {
            final JarAnalyzer ja = new JarAnalyzer();
            ja.analyze(new File("./src/test/resources/examples.jar"));
            ja.setWorkDir(Paths.get("./target"));
            ja.setRename(true);
            JarAnalyzer.setAppContext(
                    new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
            ja.call();

            // Check bundled library IDs
            final Collection<LibraryId> blibids = ja.getLibrary().getBundledLibraryIds();
            assertEquals(2, blibids.size());
            assertTrue(
                    blibids.contains(
                            new LibraryId("org.apache.commons", "commons-compress", "1.10")));
            assertTrue(
                    blibids.contains(
                            new LibraryId("commons-fileupload", "commons-fileupload", "1.3.1")));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testParamNormalization() {
        String t11 = "a.b.Class.method(foo.bar.Test, String, int)";
        String t12 = "a.b.Class.method(Test, String, int)";
        assertEquals(ClassVisitor.removeParameterQualification(t11), t12);

        String t21 = "method(Set<java.lang.Object, foo.bar.Test$Uber>,String,java.lang.Boolean)";
        String t22 = "method(Set<Object, Uber>,String,Boolean)";
        assertEquals(ClassVisitor.removeParameterQualification(t21), t22);

        String qname1 =
                "foo.bar.Test(Map< Object,  Test>,String , Boolean,Map<String,Map <String"
                        + " ,String>>)";
        JavaConstructorId cid = JavaId.parseConstructorQName(qname1);
        String qname2 = cid.getQualifiedName();
        String qname3 =
                "foo.bar.Test(Map<Object,Test>,String,Boolean,Map<String,Map<String,String>>)";
        assertEquals(qname2, qname3);
    }

    @Test
    public void testNonStaticMemberClass() {
        try {
            final JarAnalyzer ja = new JarAnalyzer();
            ja.analyze(new File("./src/test/resources/commons-fileupload-1.3.1.jar"));
            final Set<ConstructId> constructs = ja.getConstructIds();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
