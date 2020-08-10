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

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;

public class PythonArchiveAnalyzerTest {

    /*@Test
    public void testPythonArchiveWithNestedGZ() throws FileAnalysisException {
    	final FileAnalyzer f5 = FileAnalyzerFactory.buildFileAnalyzer(new File("src/test/resources/pandas-0.22.0-cp36-cp36m-win_amd64.whl"));
    	final Map<ConstructId, Construct> c5 = f5.getConstructs();
    	System.out.println(c5.size());
    	//assertEquals(7, c5.size());
    }*/

    @Test
    public void testNestedArchive() {
        try {
            final FileAnalyzer outer_paa =
                    FileAnalyzerFactory.buildFileAnalyzer(
                            new File("src/test/resources/itsdangerous-0.24-monotonic-1.3.tar.gz"));

            // Trigger analysis
            outer_paa.getConstructs();

            // There should be exactly one nested PythonArchiveAnalyzer
            assertTrue(outer_paa.hasChilds());
            final Set<FileAnalyzer> childs = outer_paa.getChilds(false);
            assertEquals(1, childs.size());
            final PythonArchiveAnalyzer inner_paa =
                    (PythonArchiveAnalyzer) childs.iterator().next();

            // Non-nested monotonic
            final FileAnalyzer mono_paa =
                    FileAnalyzerFactory.buildFileAnalyzer(
                            new File("src/test/resources/monotonic-1.3-py2.py3-none-any.whl"));
            final Map<ConstructId, Construct> mono_constructs = mono_paa.getConstructs();

            // Compare constructs
            final Map<ConstructId, Construct> inner_constructs = inner_paa.getConstructs();
            assertEquals(mono_constructs, inner_constructs);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (FileAnalysisException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Python parser creates a couple of errors for the following files ggevent.py, _gaiohttp.py and geventlet.py (see VULAS-1434).
     * @throws FileAnalysisException
     */
    @Test
    public void testGunicorn() throws FileAnalysisException {
        final FileAnalyzer f3 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/gunicorn-19.7.1-py2.py3-none-any.whl"));
        final Map<ConstructId, Construct> c3 = f3.getConstructs();

        /*for(ConstructId cid: c3.keySet())
        System.out.println(cid);*/

        // 1. parse errors happen in the following method. Check it is nevertheless among the
        // identified constructs
        // PY METH [gunicorn.workers._gaiohttp.AiohttpWorker(base.Worker).run(self)]
        final PythonId pack = new PythonId(null, PythonId.Type.PACKAGE, "gunicorn.workers");
        PythonId modu = new PythonId(pack, PythonId.Type.MODULE, "_gaiohttp");
        final PythonId clas = new PythonId(modu, PythonId.Type.CLASS, "AiohttpWorker(base.Worker)");
        final PythonId meth = new PythonId(clas, PythonId.Type.METHOD, "run(self)");
        assertTrue(c3.containsKey(pack));
        assertTrue(c3.containsKey(modu));
        assertTrue(c3.containsKey(clas));
        assertTrue(c3.containsKey(meth));

        // 2. parse errors happen in the following module. Check it is nevertheless among the
        // identified constructs
        //   PY MODU [gunicorn.workers.geventlet]
        modu = new PythonId(pack, PythonId.Type.MODULE, "geventlet");
        assertTrue(c3.containsKey(modu));

        // 3. parse errors happen in the following module. Check it is nevertheless among the
        // identified constructs
        //   PY MODU [gunicorn.workers.ggevent]
        modu = new PythonId(pack, PythonId.Type.MODULE, "ggevent");
        assertTrue(c3.containsKey(modu));
    }

    @Test
    public void testPythonArchiveAnalyzer() {
        try {
            final FileAnalyzer f4 =
                    FileAnalyzerFactory.buildFileAnalyzer(
                            new File("src/test/resources/itsdangerous-0.24.tar.gz"));
            assertTrue(f4 instanceof PythonArchiveAnalyzer);
            final PythonArchiveAnalyzer paa = (PythonArchiveAnalyzer) f4;
            final Map<ConstructId, Construct> c4 = paa.getConstructs();
            assertEquals(128, c4.size());
            assertEquals("a3d55aa79369aef5345c036a8a26307f", paa.getDigest().toLowerCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (FileAnalysisException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testPythonArchiveWithAsyncAnalyzer() {
        try {
            final FileAnalyzer f4 =
                    FileAnalyzerFactory.buildFileAnalyzer(
                            new File("src/test/resources/gevent-1.2.2-cp35-cp35m-win_amd64.whl"));
            assertTrue(f4 instanceof PythonArchiveAnalyzer);
            final PythonArchiveAnalyzer paa = (PythonArchiveAnalyzer) f4;
            final Map<ConstructId, Construct> c4 = paa.getConstructs();
            //			assertEquals(128, c4.size());
            //			assertEquals("a3d55aa79369aef5345c036a8a26307f", paa.getDigest().toLowerCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (FileAnalysisException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testPythonArchiveFormats() throws FileAnalysisException {
        final FileAnalyzer f1 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/monotonic-1.3-py2.py3-none-any.whl"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();
        assertEquals(7, c1.size());

        final FileAnalyzer f2 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/setuptools_git-1.2-py3.6.egg"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();
        assertEquals(58, c2.size());

        final FileAnalyzer f4 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("src/test/resources/itsdangerous-0.24.tar.gz"));
        final Map<ConstructId, Construct> c4 = f4.getConstructs();
        assertEquals(128, c4.size());
    }

    @Test
    public void testFlaskOIDCConstructs() throws FileAnalysisException {
        final FileAnalyzer f1 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("./src/test/resources/flask-oidc_e4ce5/flask_oidc/__init__.py"));
        final Map<ConstructId, Construct> c1 = f1.getConstructs();

        final FileAnalyzer f2 =
                FileAnalyzerFactory.buildFileAnalyzer(
                        new File("./src/test/resources/flask-oidc_f2ef8/flask_oidc/__init__.py"));
        final Map<ConstructId, Construct> c2 = f2.getConstructs();

        assertEquals(c1.size(), c2.size());
    }
}
