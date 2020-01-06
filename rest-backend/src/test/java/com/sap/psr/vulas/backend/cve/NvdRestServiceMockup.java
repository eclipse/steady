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
package com.sap.psr.vulas.backend.cve;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;

import java.io.IOException;
import java.nio.file.Paths;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

import com.jayway.restassured.RestAssured;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.xebialabs.restito.server.StubServer;

/**
 * Singleton mocking the NVD REST service, which is used by different JUnit tests. Sets the system property {@link CveReader2#CVE_SERVICE_URL}.
 * To add a new vulnerability FOO, store the respective JSON as ./src/test/resources/cves/FOO-new.json and update the array {@link NvdRestServiceMockup#CVES}.
 */
public class NvdRestServiceMockup {

	private static  final String[] CVES = new String[] { "CVE-2018-1000865", "CVE-2019-17531", "CVE-2018-0123", "CVE-2014-0050", "CVE-2019-0047" };
	
	private static NvdRestServiceMockup instance = null;
	
	private StubServer server;

	/**
	 * Starts the server and registers URIs for the different vulnerabilities in {@link NvdRestServiceMockup#CVES}.
	 * @throws IOException
	 */
	public NvdRestServiceMockup() {
		server = new StubServer().run();
		RestAssured.port = server.getPort();
		System.setProperty(VulasConfiguration.getServiceUrlKey(Service.CVE), "http://localhost:" + server.getPort() + "/nvdrest/vulnerabilities/<ID>");
		int cves_registered = 0;
		for(String cve: CVES) {
			try {
				whenHttp(server).
				match(composite(method(Method.GET), uri("/nvdrest/vulnerabilities/" + cve))).
				then(
						stringContent(FileUtil.readFile(Paths.get("./src/test/resources/cves/" + cve + "-new.json"))),
						contentType("application/json"),
						charset("UTF-8"),
						status(HttpStatus.OK_200));
				cves_registered++;
			} catch (IOException e) {
				System.err.println("Could not register URI for cve [" + cve + "]: " + e.getMessage());
			}
		}
		if(cves_registered==0) {
			throw new IllegalStateException("None of the CVEs could be registered");
		}
	}
	
	/**
	 * Creates the singleton (if necessary).
	 */
	public static synchronized final void create() {
		if(instance==null) {
			instance = new NvdRestServiceMockup();
		}
	}
}
