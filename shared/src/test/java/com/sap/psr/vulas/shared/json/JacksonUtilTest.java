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
package com.sap.psr.vulas.shared.json;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.Property;
import com.sap.psr.vulas.shared.json.model.view.Views;

public class JacksonUtilTest {
	
	static class TestViews {
		public static class DefaultView {} // Not used in member annotations, will return only non-annotated fields when used with JacksonUtil
		public static class TestView {}
	}

	/** Test class used for serialization. */
	private static final class Foo {
		public final String bar = "bar";
		
		@JsonView(TestViews.TestView.class)
		public final String test = "test";
	}
	
	@Test
	public void testAsJsonString() {
		final Foo f = new Foo();
		
		// All properties
		final String json_all = JacksonUtil.asJsonString(f);
		assertEquals("{\"bar\":\"bar\",\"test\":\"test\"}", json_all);
	}
	
	@Test
	public void testAsJsonStringWithView() {
		final Foo f = new Foo();
		
		// All properties
		final String json_all = JacksonUtil.asJsonString(f, null, null);
		assertEquals("{\"bar\":\"bar\",\"test\":\"test\"}", json_all);
		
		// Only bar property
		final String json_bar = JacksonUtil.asJsonString(f, null, TestViews.DefaultView.class);
		assertEquals("{\"bar\":\"bar\"}", json_bar);
		
		// All properties
		final String json_test = JacksonUtil.asJsonString(f, null, TestViews.TestView.class);
		assertEquals("{\"bar\":\"bar\",\"test\":\"test\"}", json_all);
	}
	
	@Test
	public void testAppAsJsonStringWithView() {
		final Application app = new Application("com.acme", "foo", "0.1");
		
		final Library lib = new Library("digest-123");
		lib.setDigestAlgorithm(DigestAlgorithm.SHA256);
		
		final Set<Property> props = new HashSet<Property>();
		props.add(new Property(PropertySource.JAVA_MANIFEST, "foo-prop", "bar-prop"));
		lib.setProperties(props);
		
		final Set<ConstructId> cons = new HashSet<ConstructId>();
		cons.add(new ConstructId(ProgrammingLanguage.JAVA, ConstructType.METH, "com.foo.Bar"));
		lib.setConstructs(cons);
		
		app.addDependency(new Dependency(app, lib, Scope.COMPILE, false, "fo0-lib.jar", "/tmp/"));
		
		// All properties (as no view is given)
		final String json_no_view = JacksonUtil.asJsonString(app, null, null);
		//assertEquals("{\"artifact\":\"foo\",\"version\":\"0.1\",\"createdAt\":null,\"modifiedAt\":null,\"lastScan\":null,\"lastVulnChange\":null,\"constructs\":[],\"dependencies\":[{\"lib\":{\"digest\":\"digest-123\",\"digestAlgorithm\":\"SHA256\",\"createdAt\":null,\"properties\":[{\"source\":\"JAVA_MANIFEST\",\"name\":\"foo-prop\",\"value\":\"bar-prop\"}],\"constructs\":[{\"lang\":\"JAVA\",\"type\":\"METH\",\"qname\":\"com.foo.Bar\",\"relates\":null,\"attributes\":null}],\"libraryId\":null,\"wellknownDigest\":null,\"digestVerificationUrl\":null,\"constructCounter\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":1,\"MODU\":0,\"FUNC\":0}},\"declared\":true,\"traced\":false,\"scope\":\"COMPILE\",\"transitive\":false,\"filename\":\"fo0-lib.jar\",\"path\":\"/tmp/\",\"reachableConstructIds\":null,\"touchPoints\":null,\"parent\":null,\"origin\":null,\"relativePath\":null}],\"empty\":false,\"complete\":true,\"group\":\"com.acme\",\"constructCounter\":0,\"countDependencies\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":0,\"MODU\":0,\"FUNC\":0}}", json_no_view);
		
		// All properties without view and those with view Default
		final String json_default_view = JacksonUtil.asJsonString(app, null, Views.Default.class);
		//assertEquals("{\"artifact\":\"foo\",\"version\":\"0.1\",\"createdAt\":null,\"modifiedAt\":null,\"lastScan\":null,\"lastVulnChange\":null,\"constructs\":[],\"dependencies\":[{\"lib\":{\"digest\":\"digest-123\",\"digestAlgorithm\":\"SHA256\",\"createdAt\":null,\"libraryId\":null,\"wellknownDigest\":null,\"digestVerificationUrl\":null,\"constructCounter\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":1,\"MODU\":0,\"FUNC\":0}},\"declared\":true,\"traced\":false,\"scope\":\"COMPILE\",\"transitive\":false,\"filename\":\"fo0-lib.jar\",\"path\":\"/tmp/\",\"reachableConstructIds\":null,\"touchPoints\":null,\"parent\":null,\"origin\":null,\"relativePath\":null}],\"empty\":false,\"complete\":true,\"group\":\"com.acme\",\"constructCounter\":0,\"countDependencies\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":0,\"MODU\":0,\"FUNC\":0}}", json_default_view);
		
		// All properties without view and those with view LibDetails
		final String json_lib_view = JacksonUtil.asJsonString(app, null, Views.LibDetails.class);
		//assertEquals("{\"artifact\":\"foo\",\"version\":\"0.1\",\"createdAt\":null,\"modifiedAt\":null,\"lastScan\":null,\"lastVulnChange\":null,\"constructs\":[],\"dependencies\":[{\"lib\":{\"digest\":\"digest-123\",\"digestAlgorithm\":\"SHA256\",\"createdAt\":null,\"properties\":[{\"source\":\"JAVA_MANIFEST\",\"name\":\"foo-prop\",\"value\":\"bar-prop\"}],\"constructs\":[{\"lang\":\"JAVA\",\"type\":\"METH\",\"qname\":\"com.foo.Bar\",\"relates\":null,\"attributes\":null}],\"libraryId\":null,\"wellknownDigest\":null,\"digestVerificationUrl\":null,\"constructCounter\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":1,\"MODU\":0,\"FUNC\":0}},\"declared\":true,\"traced\":false,\"scope\":\"COMPILE\",\"transitive\":false,\"filename\":\"fo0-lib.jar\",\"path\":\"/tmp/\",\"reachableConstructIds\":null,\"touchPoints\":null,\"parent\":null,\"origin\":null,\"relativePath\":null}],\"empty\":false,\"complete\":true,\"group\":\"com.acme\",\"constructCounter\":0,\"countDependencies\":1,\"constructTypeCounters\":{\"PACK\":0,\"ENUM\":0,\"CONS\":0,\"CLASS\":0,\"INIT\":0,\"METH\":0,\"MODU\":0,\"FUNC\":0}}", json_lib_view);
	}
}
