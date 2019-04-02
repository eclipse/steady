package com.sap.psr.vulas.shared.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;

public class JacksonUtilTest {
	
	static class Views {
		public static class DefaultView {} // Not used in member annotations, will return only non-annotated fields when used with JacksonUtil
		public static class TestView {}
	}

	/** Test class used for serialization. */
	private static final class Foo {
		public final String bar = "bar";
		
		@JsonView(Views.TestView.class)
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
		final String json_bar = JacksonUtil.asJsonString(f, null, Views.DefaultView.class);
		assertEquals("{\"bar\":\"bar\"}", json_bar);
		
		// All properties
		final String json_test = JacksonUtil.asJsonString(f, null, Views.TestView.class);
		assertEquals("{\"bar\":\"bar\",\"test\":\"test\"}", json_all);
	}
}
