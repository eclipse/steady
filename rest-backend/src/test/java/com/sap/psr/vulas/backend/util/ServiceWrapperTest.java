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
package com.sap.psr.vulas.backend.util;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;

import java.io.IOException;
import java.nio.file.Paths;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.xebialabs.restito.server.StubServer;

public class ServiceWrapperTest {
	
	/** Mocks the NVD REST service. */
	protected StubServer cveService;
	
	@Before
	public void setup() throws Exception {
		cveService = new StubServer().run();
		RestAssured.port = cveService.getPort();
		StringBuffer b = new StringBuffer();
		b.append("http://localhost:").append(cveService.getPort()).append("/cves/");
		VulasConfiguration.getGlobal().setProperty(VulasConfiguration.getServiceUrlKey(Service.CVE), b.toString());
	}
	
	@After
	public void stop() {
		cveService.stop();
	}

	/**
	 * Mocks the CVE classifier for the given vulnerabilities.
	 * @param _cves
	 * @throws IOException
	 */
	private void setupMockServices(String... _cves) throws IOException {
		for(String s: _cves) {
			whenHttp(cveService).
				match(get("/cves/" + s)).
				then(
					stringContent(FileUtil.readFile(Paths.get("./src/test/resources/cves/" + s + "-new.json"))),
					contentType("application/json"),
					charset("UTF-8"),
					status(HttpStatus.OK_200));
			
			expect()
				.statusCode(200).
				when()
				.get("/cves/" + s);
		}
	}

	/**
	 * Calls {@link ServiceWrapper#classify(String)} for a number of vulnerabilities.
	 */
	@Test
	public void testClassifier() throws ServiceConnectionException, IOException {
		this.setupMockServices("CVE-2014-0050");
	}
}