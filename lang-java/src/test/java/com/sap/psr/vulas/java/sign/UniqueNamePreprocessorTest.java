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
package com.sap.psr.vulas.java.sign;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.java.JarAnalyzer;

public class UniqueNamePreprocessorTest {

    /**
     * Example statement from BZip2CompressorOutputStream(final OutputStream out, final int blockSize), revision 1333522.
     */
    private static final int BASEBLOCKSIZE = 100000;

    private static final String BZIP2_BEFORE =
            "this.allowableBlockSize = ((this.blockSize100k * BZip2Constants.BASEBLOCKSIZE) - 20);";
    private static final String BZIP2_NORMALIZED =
            "allowableBlockSize = ((blockSize100k * 100000) - 20);";

    /**
     * String example.
     */
    private static final String BAR = "BAR";

    private static final String EXAMPLE_BEFORE = "this.foo = UniqueNamePreprocessorTest.BAR;";
    private static final String EXAMPLE_NORMALIZED = "foo = \"BAR\";";

    /**
     * Nested class example.
     */
    static class InnerClass {
        private static final String FOO = "FOO";
    }

    private static final String IC_BEFORE = "this.foo = UniqueNamePreprocessorTest.InnerClass.FOO;";
    private static final String IC_NORMALIZED = "foo = \"FOO\";";

    /**
     * Tests the normalization of Java statement, including a constant of type int.
     * Make sure to have commons-compress as test dependency.
     * @throws IOException
     */
    @Test
    public void testUniqueNamePreprocessorWithInt() throws FileAnalysisException {
        // Get all the class names from the Java archive (the archive must also be included as test
        // scope dependency so that it can be loaded)
        final JarAnalyzer ja = new JarAnalyzer();
        ja.analyze(new File("./src/test/resources/commons-compress-1.10.jar"));
        final UniqueNameNormalizer prep = UniqueNameNormalizer.getInstance();
        prep.addStrings(ja.getClassNames());

        // Normalize and check
        final String normalized = prep.normalizeUniqueName(BZIP2_BEFORE);
        assertEquals(BZIP2_NORMALIZED, normalized);
    }

    /**
     * Tests the normalization of Java statement, including a constant of type String.
     * @throws IOException
     */
    @Test
    public void testUniqueNamePreprocessorWithString() throws IOException {
        // Add the class name of this Junit test class (which is anyways in the classpath, no hazzle
        // with dependencies necessary)
        final UniqueNameNormalizer prep = UniqueNameNormalizer.getInstance();
        prep.addStrings(new String[] {"com.sap.psr.vulas.java.sign.UniqueNamePreprocessorTest"});

        // Normalize and check
        final String normalized = prep.normalizeUniqueName(EXAMPLE_BEFORE);
        assertEquals(EXAMPLE_NORMALIZED, normalized);
    }

    /**
     * Tests the normalization of Java statement, including a constant of type String contained in an inner class.
     * @throws IOException
     */
    @Test
    public void testUniqueNamePreprocessorWithInnerClass() throws IOException {
        // Add the class name of this Junit test class (which is anyways in the classpath, no hazzle
        // with dependencies necessary)
        final UniqueNameNormalizer prep = UniqueNameNormalizer.getInstance();
        prep.addStrings(
                new String[] {
                    "com.sap.psr.vulas.java.sign.UniqueNamePreprocessorTest",
                    "com.sap.psr.vulas.java.sign.UniqueNamePreprocessorTest$InnerClass"
                });

        // Normalize and check
        final String normalized = prep.normalizeUniqueName(IC_BEFORE);
        assertEquals(IC_NORMALIZED, normalized);
    }
}
