package com.sap.psr.vulas.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaConstructorId;
import com.sap.psr.vulas.java.JavaId;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class ClassVisitorTest {

	/**
	 * Test class.
	 */
	private class NonStaticInner {
		NonStaticInner(Map<String,Object> _arg) {}
	};
	
	@Test
	public void testPrettyPrint() {
		final String src = "try {if(!VUL_TRC_XGETALIGNMENT_635905){Class vul_cls = null;if(vul_cls==null) { try { vul_cls=$0.getClass(); } catch(Exception e) {} }if(vul_cls==null) { try { vul_cls=java.lang.invoke.MethodHandles.lookup().lookupClass(); } catch(Exception e) {} }if(vul_cls==null) { try { vul_cls=$class; } catch(Exception e) {} }final ClassLoader vul_cls_ldr=vul_cls.getClassLoader();java.net.URL vul_cls_res = null;if(vul_cls_ldr!=null)vul_cls_res=vul_cls_ldr.getResource(vul_cls.getName().replace('.', '/') + \".class\");java.util.Map params = new java.util.HashMap();params.put(\"junit\", \"false\");params.put(\"counter\", new Integer(1));VUL_TRC_XGETALIGNMENT_635905=com.sap.psr.vulas.monitor.trace.TraceCollector.callback(\"METHOD\",\"org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPTabImpl.xgetAlignment()\",vul_cls_ldr,vul_cls_res,\"BF0D37E25A643FD4527731790F174BE26AB74A07\",\"com.acme\",\"vulas-testapp\",\"2.1.0-SNAPSHOT\",params);}} catch(Throwable e) { System.err.println(e.getClass().getName() + \" occured during execution of instrumentation code in JAVA METH [org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPTabImpl.xgetAlignment()]: \" + e.getMessage()); }";
		final String pretty = ClassVisitor.prettyPrint(src);
		assertTrue(true);
	}
	
	/**
	 * Test the handling of (1) the first constructor of non-static inner classes, (2) the handling of generics as method parameters.
	 */
	@Test 
	public void testNonStaticInnerClassConstructor () {
		try {
			// The test class
			final JavaClassId cid = JavaId.parseClassQName("com.sap.psr.vulas.monitor.ClassVisitorTest$NonStaticInner");
			
			// Get a CtClass and identify its constructors
			final ClassPool cp = ClassPool.getDefault();
			final CtClass ctclass = cp.get(cid.getQualifiedName());
			final ClassVisitor cv = new ClassVisitor(ctclass);
			final Set<ConstructId> constructors = cv.visitConstructors(false);
					
			// Expected result (no first parameter of type outer class, no diamond for type parameters)
			final JavaConstructorId expected = JavaId.parseConstructorQName("com.sap.psr.vulas.monitor.ClassVisitorTest$NonStaticInner(Map)");
			
			// Check that certain constructs have been found
			assertEquals(expected, constructors.iterator().next());
		} catch (NotFoundException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		} catch (CannotCompileException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	@Test
	public void testFixQName() {
		assertTrue(ClassVisitor.removeParameterQualification("a.b.c.Class()").equals("a.b.c.Class()"));
		assertTrue(ClassVisitor.removeParameterQualification("a.b.c.Class(int)").equals("a.b.c.Class(int)"));
		assertTrue(ClassVisitor.removeParameterQualification("a.b.c.Class(int,a.b.C)").equals("a.b.c.Class(int,C)"));
		assertTrue(ClassVisitor.removeParameterQualification("a.b.c.Class(int,C,boolean,a.b.c.ddd.Test)").equals("a.b.c.Class(int,C,boolean,Test)"));
	}
}
