package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.java.JavaPackageId;

/**
 * Basic tests related to JavaId and its subclasses.
 *
 */
public class JavaIdTest {

	@Test 
	public void packageIdTest() {
		final JavaPackageId p = new JavaPackageId("com.sap.research");
		assertEquals("com.sap.research", p.getQualifiedName());
		assertEquals("com.sap.research", p.getName());
		assertEquals("com.sap.research", p.getSimpleName());
		assertEquals(null, p.getDefinitionContext());
		assertEquals(null, p.getJavaPackageId());
	}
	
	@Test 
	public void classIdTest() {
		final JavaPackageId p = new JavaPackageId("com.sap.research");
		final JavaClassId c   = new JavaClassId(p, "TestClass");
		assertEquals("com.sap.research.TestClass", c.getQualifiedName());
		assertEquals("TestClass", c.getName());
		assertEquals("TestClass", c.getSimpleName());
		assertEquals(p, c.getDefinitionContext());
		assertEquals(p, c.getJavaPackageId());
	}
	
	@Test 
	public void nestedClassIdTest() {
		final JavaPackageId p = new JavaPackageId("com.sap.research");
		final JavaClassId c   = new JavaClassId(p, "TestClass");
		final JavaClassId nc  = new JavaClassId(c, "NestedClass");
		assertEquals("com.sap.research.TestClass$NestedClass", nc.getQualifiedName());
		assertEquals("TestClass$NestedClass", nc.getName());
		assertEquals("NestedClass", nc.getSimpleName());
		assertEquals(c, nc.getDefinitionContext());
		assertEquals(p, nc.getJavaPackageId());
	}
	
	@Test 
	public void methodIdTest() {
		final JavaPackageId p = new JavaPackageId("com.sap.research");
		final JavaClassId c   = new JavaClassId(p, "TestClass");
		final JavaClassId nc  = new JavaClassId(c, "NestedClass");
		final JavaMethodId m  = new JavaMethodId(nc, "foo", Arrays.asList("java.lang.String", "int"));
		assertEquals("com.sap.research.TestClass$NestedClass.foo(String,int)", m.getQualifiedName());
		assertEquals("foo(String,int)", m.getName());
		assertEquals("foo", m.getSimpleName());
		assertEquals(nc, m.getDefinitionContext());
		assertEquals(p, m.getJavaPackageId());
	}
	
	@Test 
	public void testMethodParser() {
		final JavaPackageId p = new JavaPackageId("com.sap.research");
		final JavaClassId c   = new JavaClassId(p, "TestClass");
		final JavaClassId nc  = new JavaClassId(c, "NestedClass");
		final JavaMethodId m1 = new JavaMethodId(nc, "foo", Arrays.asList("java.lang.String", "int"));
		final JavaMethodId m2 = JavaId.parseMethodQName("com.sap.research.TestClass$NestedClass.foo(String,int)");
		assertEquals(m1, m2);
	}
	
	@Test
	public void testRemovePackageContext() {
		assertTrue(JavaId.removePackageContext("a.b.c.Class").equals("Class"));
		assertTrue(JavaId.removePackageContext("a.b.c.Class$NestedClass").equals("NestedClass"));
		assertTrue(JavaId.removePackageContext("Class").equals("Class"));
	}

}
