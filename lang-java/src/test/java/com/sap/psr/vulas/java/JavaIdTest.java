package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.java.JavaPackageId;

/**
 * Basic tests related to JavaId and its subclasses.
 *
 */
public class JavaIdTest {

	@Rule public ExpectedException thrown = ExpectedException.none();

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

	@Test
	public void typeFromStringInputNotNullOutputIllegalArgumentException() {
  
	  // Arrange
	  final String _t = "";
  
	  // Act
	  thrown.expect(IllegalArgumentException.class);
	  JavaId.typeFromString(_t);
  
	  // Method is not expected to return due to exception thrown
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull() {
  
	  // Arrange
	  final String _t = "ENUM";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.ENUM, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull2() {
  
	  // Arrange
	  final String _t = "pACK";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.PACKAGE, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull3() {
  
	  // Arrange
	  final String _t = "iNIT";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.CLASSINIT, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull4() {
  
	  // Arrange
	  final String _t = "CLAS";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.CLASS, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull5() {
  
	  // Arrange
	  final String _t = "InTF";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.INTERFACE, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull6() {
  
	  // Arrange
	  final String _t = "CoNs";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.CONSTRUCTOR, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull7() {
  
	  // Arrange
	  final String _t = "NCla";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.NESTED_CLASS, retval);
	}

	@Test
	public void typeFromStringInputNotNullOutputNotNull8() {
  
	  // Arrange
	  final String _t = "MEtH";
  
	  // Act
	  final JavaId.Type retval = JavaId.typeFromString(_t);
  
	  // Assert result
	  assertEquals(JavaId.Type.METHOD, retval);
	}

	@Test
	public void filterInputNotNull0Output0() {
  
	  // Arrange
	  final HashSet _set = new HashSet();
	  final JavaId.Type[] _filter = {};
  
	  // Act
	  final Set<ConstructId> retval = JavaId.filter(_set, _filter);
  
	  // Assert result
	  final HashSet<ConstructId> hashSet = new HashSet<ConstructId>();
	  assertEquals(hashSet, retval);
	}

	@Test
	public void filterInputNotNullNotNullOutput0() {
  
	  // Arrange
	  final HashSet _set = new HashSet();
	  final String _filter = "";
  
	  // Act
	  final Set<ConstructId> retval = JavaId.filter(_set, _filter);
  
	  // Assert result
	  final HashSet<ConstructId> hashSet = new HashSet<ConstructId>();
	  assertEquals(hashSet, retval);
	}

}
