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
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.patcheval.representation.ConstructPathLibResult2;
import com.sap.psr.vulas.patcheval.representation.OverallConstructChange;
import com.sap.psr.vulas.patcheval.utils.PEConfiguration;
import com.sap.psr.vulas.patcheval2.BugLibAnalyzer;
import com.sap.psr.vulas.patcheval2.LibraryAnalyzerThread2;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class CiaTest {

    @Ignore
    @Test
    public void getConstructsTest() throws BackendConnectionException {
        for (int i = 0; i < 1000; i++) {
            ConstructId[] c;
            try {

                c =
                        BackendConnector.getInstance()
                                .getArtifactConstructs(
                                        "org.apache.tomcat", "tomcat-catalina", "7.0.6");
                System.out.println("Size is [" + c.length + "]");

                List<ConstructId> cids = Arrays.asList(c);

                assertEquals(
                        true,
                        cids.contains(
                                new ConstructId(
                                        ProgrammingLanguage.JAVA,
                                        ConstructType.METH,
                                        "org.apache.catalina.startup.ContextConfig.createWebXmlDigester(boolean,boolean)")));
            } catch (BackendConnectionException e) {
                throw e;
            }
        }
    }

    // @Test
    public void getConstructsThreadTest() throws BackendConnectionException {
        // VulasConfiguration.getGlobal().setProperty(CoreConfiguration.UPLOAD_ENABLED, true);
        VulasConfiguration.getGlobal()
                .setProperty(
                        CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_ONLY);
        ConstructId a =
                new ConstructId(
                        ProgrammingLanguage.JAVA,
                        ConstructType.METH,
                        "org.apache.catalina.startup.ContextConfig.createWebXmlDigester(boolean,boolean)");
        OverallConstructChange cc = new OverallConstructChange(null, null, null, null, a);
        LinkedList<OverallConstructChange> l = new LinkedList<OverallConstructChange>();
        l.add(cc);

        Future<List<ConstructPathLibResult2>> f = null;
        Set<Future<List<ConstructPathLibResult2>>> set =
                new HashSet<Future<List<ConstructPathLibResult2>>>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (Integer i = 2; i < 6; i++) {
            String v = new String("8.5.").concat(i.toString());
            // List<LibraryId> libs = new ArrayList<LibraryId>();
            // libs.add(new LibraryId("org.apache.tomcat","tomcat-catalina",v));
            Artifact lib = new Artifact("org.apache.tomcat", "tomcat-catalina", v);
            Callable<List<ConstructPathLibResult2>> thread =
                    new LibraryAnalyzerThread2(
                            i,
                            l,
                            new LinkedList<OverallConstructChange>(),
                            lib,
                            ProgrammingLanguage.JAVA);

            f = executorService.submit(thread);
            set.add(f);
        }
        executorService.shutdown();

        for (Future<List<ConstructPathLibResult2>> future : set) {
            try {
                List<ConstructPathLibResult2> res = future.get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // @Test
    public void getLibToCheckTest() {
        PEConfiguration.getGlobal()
                .setProperty("vulas.patchEval.excludedGa", "com.sap.customercheckout:ENV");
        BugLibAnalyzer b = new BugLibAnalyzer();
        Bug id = new Bug();
        id.setBugId("SPR-7779");
        b.setBug(id);
        try {
            LinkedList<Artifact> list = b.getLibToCheck();
            //	for(Artifact a : list)
            //		System.out.println(a + "\n");
        } catch (BackendConnectionException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // @Test
    public void getLibToCheck2Test() {
        PEConfiguration.getGlobal()
                .setProperty("vulas.patchEval.gav", "org.springframework:spring-tx:4.3.16.RELEASE");
        BugLibAnalyzer b = new BugLibAnalyzer();
        Bug id = new Bug();
        id.setBugId("CVE-2014-1904");
        b.setBug(id);
        try {
            LinkedList<Artifact> list = b.getLibToCheck();
            //	for(Artifact a : list)
            //		System.out.println(a + "\n");
        } catch (BackendConnectionException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
