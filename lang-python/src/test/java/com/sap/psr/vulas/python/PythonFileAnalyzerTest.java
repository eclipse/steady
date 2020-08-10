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
package com.sap.psr.vulas.python;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;

public class PythonFileAnalyzerTest {

    @Test
    public void testPythonFilesInTestapp() throws FileAnalysisException {
        final FileAnalyzer f1 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/testapp/foo/hello_world.py"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();
        assertEquals(2, c1.size()); // pack, module

        final FileAnalyzer f2 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/testapp/foo/func_hw.py"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();
        assertEquals(4, c2.size()); // pack, module, 2 functions

        final FileAnalyzer f3 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/testapp/foo/class_hw.py"));
        final Map<ConstructId, Construct> c3 = f3.getConstructs();
        assertEquals(12, c3.size()); // pack, module, 2 functions, 1 class, 1 constructor, 1 method

        // class_hw.py contains Python 2 syntax. This is to check whether the resp. method is still
        // added when using the Python 3 parser
        final PythonId pack = new PythonId(null, PythonId.Type.PACKAGE, "foo");
        final PythonId modu = new PythonId(pack, PythonId.Type.MODULE, "class_hw");
        final PythonId func = new PythonId(modu, PythonId.Type.FUNCTION, "func_with_class(arg)");
        final PythonId clas = new PythonId(func, PythonId.Type.CLASS, "class_in_func()");
        final PythonId meth =
                new PythonId(clas, PythonId.Type.METHOD, "method_of_class_in_func(self,something)");
        assertTrue(c3.containsKey(pack));
        assertTrue(c3.containsKey(modu));
        assertTrue(c3.containsKey(func));
        assertTrue(c3.containsKey(clas));
        assertTrue(c3.containsKey(meth));
    }

    @Test
    public void testPython2() throws FileAnalysisException {
        final FileAnalyzer fa =
                PythonFileAnalyzer.createAnalyzer(new File("src/test/resources/python2_syntax.py"));
        assertTrue(fa instanceof com.sap.psr.vulas.python.Python335FileAnalyzer);
    }

    @Test
    public void testAsync() throws FileAnalysisException {
        final FileAnalyzer fa =
                PythonFileAnalyzer.createAnalyzer(new File("src/test/resources/async.py"));
        assertTrue(fa instanceof com.sap.psr.vulas.python.Python3FileAnalyzer);
    }

    @Test
    public void testPythonFilesInTinypy() throws FileAnalysisException {
        final FileAnalyzer f3 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File(
                                "src/test/resources/tiny_py_interpreter/tinypy/runtime/Errors.py"));
        final Map<ConstructId, Construct> c3 = f3.getConstructs();
        assertEquals(15, c3.size());
    }

    /**
     * Make sure that there is no function "corecffi.send(self)", which is present when using the Python 3.6 grammar.
     * @throws FileAnalysisException
     */
    @Test
    public void testPythonFileWithAsync() throws FileAnalysisException {
        final FileAnalyzer f3 =
                FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/corecffi.py"));
        final Map<ConstructId, Construct> c3 = f3.getConstructs();

        // Due to the corecffi.send(self)
        final PythonId modu = new PythonId(null, PythonId.Type.MODULE, "corecffi");
        final PythonId func = new PythonId(modu, PythonId.Type.FUNCTION, "send(self)");
        assertFalse(c3.containsKey(func));
    }

    /*public void testPythonFiles() throws FileAnalysisException {
    	PythonUtils.setVirtualEnv(false);

    	final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp/testapp/testinput/test.py"));
    	final Map<ConstructId, Construct> c1 = f1.getConstructs();
    	assertEquals(13, c1.size());

    	final FileAnalyzer f2 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp/testapp/views.py"));
    	final Map<ConstructId, Construct> c2 = f2.getConstructs();
    	assertEquals(1, c2.size());

    	final FileAnalyzer f4 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp/testapp/testinput/test2.py"));
    	final Map<ConstructId, Construct> c4 = f4.getConstructs();
    	assertEquals(12, c4.size());

    	final FileAnalyzer f5 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp/testapp/testinput/subdir/test3.py"));
    	final Map<ConstructId, Construct> c5 = f5.getConstructs();
    	assertEquals(4, c5.size());
    }


    public void testDirWithPythonFiles() throws FileAnalysisException {
    	PythonUtils.setVirtualEnv(false);

        final FileAnalyzer f1 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp"));
        assertEquals(36, f1.getConstructs().size());

        final FileAnalyzer f2 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/vulas_python_testapp/testapp/testinput"));
        assertEquals(29, f2.getConstructs().size());
    }

    public void testDependencies() {
    	PythonUtils.setVirtualEnv(true);

        try {
          PythonCli.scanProject("src\\test\\resources\\vulas_python_testapp");
        } catch (IOException | FileAnalysisException | BackendConnectionException | InterruptedException e) {
          e.printStackTrace();
        }
      }*/
}
